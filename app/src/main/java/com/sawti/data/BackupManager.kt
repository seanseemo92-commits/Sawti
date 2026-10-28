package com.sawti.data

import android.content.Context
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.sawti.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/** Offline, user-selected ZIP backups. No broad storage permission or network access. */
class BackupManager(private val context: Context, private val database: AppDatabase) {
    private val dbName = "my_words.db"
    private val folders = listOf("photos", "custom_audio")
    private val maxBytes = 512L * 1024 * 1024
    private val maxEntries = 2_000

    data class ExportReceipt(val sizeBytes: Long, val payloadFiles: Int)

    /**
     * SAF creates the destination document BEFORE this callback starts. Build and verify the
     * complete archive in private cache first: failed SQLite reads must never be reported as
     * a successful zero-byte backup. Afterwards verify the bytes written by the document provider.
     */
    suspend fun export(uri: Uri): ExportReceipt = withContext(Dispatchers.IO) {
        val stage = File(context.cacheDir, "sawti_export_${System.nanoTime()}")
        require(stage.mkdirs()) { "تعذّر تجهيز مساحة للنسخ الاحتياطي" }
        try {
            val db = File(stage, dbName)
            createDatabaseSnapshot(db)
            validateDatabaseSnapshot(db)

            val entries = linkedMapOf<String, File>("database/$dbName" to db)
            // Force the most recent preference changes to disk before reading the XML.
            require(context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().commit()) {
                "تعذّر حفظ الإعدادات"
            }
            val prefs = File(context.applicationInfo.dataDir, "shared_prefs/settings.xml")
            if (prefs.isFile) entries["preferences/settings.xml"] = prefs
            folders.forEach { folder ->
                File(context.filesDir, folder).listFiles()?.filter { it.isFile }?.forEach { file ->
                    require(validMediaName(file.name)) { "اسم مرفق غير صالح" }
                    entries["files/$folder/${file.name}"] = file
                }
            }
            require(entries.size + 1 <= maxEntries) { "عدد ملفات النسخة كبير جداً" }
            val hashes = JSONObject()
            var bytes = 0L
            entries.forEach { (name, file) ->
                bytes += file.length()
                require(bytes <= maxBytes) { "النسخة أكبر من الحد المسموح" }
                hashes.put(name, sha256(file))
            }
            val manifest = JSONObject()
                .put("format", "sawti-backup")
                .put("version", 1)
                .put("package", context.packageName)
                .put("createdAt", System.currentTimeMillis())
                .put("sha256", hashes)
            require(manifest.toString().length <= 1024 * 1024) { "فهرس النسخة كبير جداً" }

            val archive = File(stage, "backup.zip")
            ZipOutputStream(FileOutputStream(archive)).use { zip ->
                zip.putNextEntry(ZipEntry("manifest.json"))
                zip.write(manifest.toString().toByteArray(Charsets.UTF_8))
                zip.closeEntry()
                entries.forEach { (name, file) ->
                    zip.putNextEntry(ZipEntry(name))
                    file.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }
            require(archive.isFile && archive.length() > 256L) { "لم يكتمل إنشاء ملف النسخة" }
            // Detect source-file changes mid-export, truncated ZIPs, and malformed database entry.
            ZipFile(archive).use { zip ->
                require(zip.size() == entries.size + 1) { "النسخة غير مكتملة" }
                require(zip.getEntry("manifest.json") != null) { "فهرس النسخة مفقود" }
                for ((name, _) in entries) {
                    val zipped = zip.getEntry(name) ?: error("ملف مفقود من النسخة")
                    val actual = zip.getInputStream(zipped).use { sha256(it) }
                    require(actual == hashes.getString(name)) { "تغيّر ملف أثناء النسخ؛ أعد المحاولة" }
                }
            }

            // "wt" explicitly truncates providers that support it, "w" is a compatible fallback.
            val output = try {
                context.contentResolver.openOutputStream(uri, "wt")
            } catch (_: IllegalArgumentException) {
                context.contentResolver.openOutputStream(uri, "w")
            } ?: error("تعذّر فتح مكان الحفظ. جرّب مجلد التنزيلات")
            val copied = output.use { out -> archive.inputStream().use { it.copyTo(out) } }
            require(copied == archive.length() && copied > 256L) { "لم تُكتب النسخة كاملة" }

            // Some document providers accept a stream but discard or truncate bytes. Read it
            // back, so the UI cannot show success if the saved document is zero bytes.
            val destination = context.contentResolver.openInputStream(uri)
                ?: error("تعذّر التأكد من النسخة المحفوظة")
            val actual = destination.use { sha256(it) }
            require(actual == sha256(archive)) { "فشل التحقق من الملف المحفوظ؛ جرّب مجلد التنزيلات" }
            ExportReceipt(copied, entries.size)
        } catch (error: Exception) {
            // CreateDocument can leave an empty placeholder when an earlier step fails.
            // Only the freshly-created destination URI is passed to export().
            try { context.contentResolver.delete(uri, null, null) } catch (_: Exception) { }
            throw error
        } finally {
            stage.deleteRecursively()
        }
    }

    private fun createDatabaseSnapshot(destination: File) {
        val live = context.getDatabasePath(dbName)
        // This also opens Room on first use if the database has not yet been initialised.
        val room = database.openHelper.writableDatabase
        require(live.isFile) { "قاعدة البيانات غير موجودة" }
        try {
            // SQLite VACUUM INTO takes a consistent, online snapshot even if WAL is active.
            // Supported by SQLite >=3.27 (e.g. Android 10+).
            room.execSQL("VACUUM INTO ?", arrayOf(destination.absolutePath))
        } catch (error: SQLiteException) {
            val cause = error.message.orEmpty().lowercase()
            if ("syntax" !in cause && "near \"into\"" !in cause) {
                throw IllegalStateException("تعذّر أخذ نسخة من قاعدة البيانات: ${error.message}", error)
            }
            // Older SQLite versions have no VACUUM INTO. Checkpoint WAL and reject an
            // incomplete checkpoint; verify the copied snapshot's integrity afterwards.
            room.query("PRAGMA wal_checkpoint(FULL)").use { cursor ->
                require(cursor.moveToFirst() && cursor.getInt(0) == 0) {
                    "قاعدة البيانات مشغولة؛ حاول مرة أخرى"
                }
            }
            live.copyTo(destination)
        }
    }

    private fun validateDatabaseSnapshot(snapshot: File) {
        require(snapshot.isFile && snapshot.length() > 4096) { "قاعدة البيانات غير مكتملة" }
        SQLiteDatabase.openDatabase(snapshot.path, null, SQLiteDatabase.OPEN_READONLY).use { sqlite ->
            sqlite.rawQuery("PRAGMA integrity_check", null).use { cursor ->
                require(cursor.moveToFirst() && cursor.getString(0) == "ok") {
                    "قاعدة البيانات غير سليمة"
                }
            }
            require(sqlite.version == 5) { "إصدار قاعدة البيانات غير مدعوم" }
        }
    }

    /**
     * Restore replaces existing data only after validating the whole archive. A durable
     * recovery journal permits rollback if the process/device stops halfway through.
     * Caller should restart the app after success, to reload Room and preferences.
     */
    suspend fun restore(uri: Uri) = withContext(Dispatchers.IO) {
        recoverIfInterrupted(context)
        val stage = File(context.cacheDir, "restore_${System.nanoTime()}").apply {
            require(mkdirs()) { "تعذّر تجهيز مساحة الاسترجاع" }
        }
        try {
            val seen = mutableSetOf<String>()
            var total = 0L
            context.contentResolver.openInputStream(uri)?.use { input ->
                ZipInputStream(input).use { zip ->
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        val name = entry.name
                        require(!entry.isDirectory && isAllowed(name) && seen.add(name)) { "ملف النسخة غير صالح" }
                        require(seen.size <= maxEntries) { "عدد ملفات النسخة كبير جداً" }
                        val target = File(stage, name)
                        require(target.canonicalPath.startsWith(stage.canonicalPath + File.separator)) {
                            "مسار ملف غير آمن"
                        }
                        require(target.parentFile?.mkdirs() == true || target.parentFile?.isDirectory == true)
                        FileOutputStream(target).use { output ->
                            val buffer = ByteArray(8192)
                            while (true) {
                                val n = zip.read(buffer)
                                if (n < 0) break
                                total += n
                                require(total <= maxBytes) { "النسخة أكبر من الحد المسموح" }
                                if (name == "manifest.json") require(target.length() + n <= 1024 * 1024) {
                                    "فهرس النسخة كبير جداً"
                                }
                                output.write(buffer, 0, n)
                            }
                        }
                        zip.closeEntry()
                    }
                }
            } ?: error("تعذّر فتح ملف النسخة")
            require("manifest.json" in seen) { "ملف النسخة يفتقد الفهرس" }
            val manifest = JSONObject(File(stage, "manifest.json").readText())
            require(manifest.getString("format") == "sawti-backup" && manifest.getInt("version") == 1)
            require(manifest.getString("package") == context.packageName)
            val hashes = manifest.getJSONObject("sha256")
            require(hashes.has("database/$dbName")) { "ملف النسخة يفتقد قاعدة البيانات" }
            val names = hashes.keys().asSequence().toSet()
            require(names + "manifest.json" == seen) { "ملف النسخة ناقص أو يحتوي ملفات غير متوقعة" }
            names.forEach { name ->
                require(isAllowed(name) && sha256(File(stage, name)) == hashes.getString(name)) {
                    "فشل التحقق من سلامة النسخة"
                }
            }
            val stagedDb = File(stage, "database/$dbName")
            SQLiteDatabase.openDatabase(stagedDb.path, null, SQLiteDatabase.OPEN_READONLY).use { sqlite ->
                sqlite.rawQuery("PRAGMA integrity_check", null).use { cursor ->
                    require(cursor.moveToFirst() && cursor.getString(0) == "ok") { "قاعدة البيانات تالفة" }
                }
                val version = sqlite.version
                require(version in 3..5) { "إصدار قاعدة البيانات غير مدعوم" }
                val expectedTables = listOf("categories", "word_items", "progress_records")
                expectedTables.forEach { name ->
                    sqlite.rawQuery("SELECT 1 FROM sqlite_master WHERE type='table' AND name=?", arrayOf(name)).use {
                        require(it.moveToFirst()) { "بنية قاعدة البيانات غير متوافقة" }
                    }
                }
            }
            SQLiteDatabase.openDatabase(stagedDb.path, null, SQLiteDatabase.OPEN_READWRITE).use { sqlite ->
                if (sqlite.version >= 2) {
                    for (folder in folders) {
                        val column = if (folder == "photos") "photoPath" else "audioPath"
                        sqlite.rawQuery("SELECT id, $column FROM word_items WHERE $column IS NOT NULL", null).use { cursor ->
                            while (cursor.moveToNext()) {
                                val oldPath = cursor.getString(1)
                                val filename = File(oldPath).name
                                val newPath = File(context.filesDir, "$folder/$filename").absolutePath
                                if (validMediaName(filename) && File(stage, "files/$folder/$filename").isFile) {
                                    sqlite.execSQL("UPDATE word_items SET $column=? WHERE id=?", arrayOf(newPath, cursor.getLong(0)))
                                } else {
                                    sqlite.execSQL("UPDATE word_items SET $column=NULL WHERE id=?", arrayOf(cursor.getLong(0)))
                                }
                            }
                        }
                    }
                }
            }
            // The incoming replacement must be prepared completely before any live file moves.
            val liveDb = context.getDatabasePath(dbName)
            val live = livePaths(context)
            val state = stateDir(context)
            val previous = File(state, "previous")
            val incoming = File(state, "incoming")
            require(state.mkdirs() && previous.mkdirs() && incoming.mkdirs()) { "تعذّر تجهيز الاسترجاع الآمن" }
            try {
                stagedDb.copyTo(File(incoming, "0"))
                val stagedPrefs = File(stage, "preferences/settings.xml")
                if (stagedPrefs.isFile) stagedPrefs.copyTo(File(incoming, "5"))
                folders.forEachIndexed { index, folder ->
                    val source = File(stage, "files/$folder")
                    if (source.isDirectory) require(source.copyRecursively(File(incoming, "${index + 3}")))
                }
                // Record which paths existed BEFORE changing anything. Atomic marker rename avoids
                // an incomplete journal being interpreted as a valid recovery transaction.
                val existing = live.indices.filter { live[it].exists() }.joinToString(",")
                val temp = File(state, "journal.tmp")
                FileOutputStream(temp).use { stream ->
                    stream.write(existing.toByteArray(Charsets.US_ASCII))
                    stream.fd.sync()
                }
                require(temp.renameTo(File(state, "journal"))) { "تعذّر بدء استرجاع آمن" }
                database.close()
                live.forEachIndexed { index, file ->
                    if (file.exists()) {
                        require(file.renameTo(File(previous, "$index"))) { "تعذّر نقل البيانات القديمة بأمان" }
                    }
                }
                incoming.listFiles()?.forEach { replacement ->
                    val index = replacement.name.toInt()
                    val destination = live[index]
                    destination.parentFile?.mkdirs()
                    require(replacement.renameTo(destination)) { "تعذّر تثبيت البيانات المسترجعة" }
                }
                // Commit marker lives OUTSIDE the rollback directory; cleanup interruption
                // cannot accidentally turn a successful restore back into a rollback.
                FileOutputStream(committedMarker(context)).use { output ->
                    output.write(1)
                    output.fd.sync()
                }
            } catch (error: Exception) {
                // If a failure occurs, restore old data. If rollback itself fails, retain
                // the durable journal for recovery on next app launch.
                recoverIfInterrupted(context)
                throw error
            }
        } finally {
            stage.deleteRecursively()
        }
    }

    private fun isAllowed(name: String): Boolean {
        if (name == "manifest.json" || name == "database/my_words.db" || name == "preferences/settings.xml") return true
        val parts = name.split('/')
        return parts.size == 3 && parts[0] == "files" && parts[1] in folders && validMediaName(parts[2])
    }

    private fun validMediaName(name: String): Boolean =
        name.isNotBlank() && name != "." && name != ".." && name.length <= 240 &&
            '/' !in name && '\\' !in name && name.none { it.isISOControl() }

    private fun sha256(stream: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)
        while (true) {
            val count = stream.read(buffer)
            if (count < 0) break
            digest.update(buffer, 0, count)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { stream ->
            val bytes = ByteArray(8192)
            while (true) {
                val count = stream.read(bytes)
                if (count < 0) break
                digest.update(bytes, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    companion object {
        private fun stateDir(context: Context) = File(context.noBackupFilesDir, "sawti_restore_journal")
        private fun committedMarker(context: Context) = File(context.noBackupFilesDir, "sawti_restore_committed")
        private fun livePaths(context: Context): List<File> {
            val db = context.getDatabasePath("my_words.db")
            return listOf(db, File(db.path + "-wal"), File(db.path + "-shm"),
                File(context.filesDir, "photos"), File(context.filesDir, "custom_audio"),
                File(context.applicationInfo.dataDir, "shared_prefs/settings.xml"))
        }

        /** Call synchronously from Application.onCreate BEFORE opening Room/SharedPreferences. */
        @Synchronized
        fun recoverIfInterrupted(context: Context) {
            val state = stateDir(context)
            val committed = committedMarker(context)
            if (committed.exists()) {
                if (state.exists()) require(state.deleteRecursively()) { "تعذّر تنظيف سجل الاسترجاع" }
                require(committed.delete()) { "تعذّر إغلاق سجل الاسترجاع" }
                return
            }
            if (!state.exists()) return
            val journal = File(state, "journal")
            if (!journal.exists()) {
                require(state.deleteRecursively()) { "تعذّر تنظيف ملفات الاسترجاع المؤقتة" }
                return
            }
            val existed = journal.readText().split(',').filter { it.isNotBlank() }.map { it.toInt() }.toSet()
            val previous = File(state, "previous")
            val live = livePaths(context)
            for (index in live.indices) {
                val old = File(previous, "$index")
                val current = live[index]
                if (old.exists()) {
                    if (current.isDirectory) require(current.deleteRecursively())
                    else if (current.exists()) require(current.delete())
                    current.parentFile?.mkdirs()
                    require(old.renameTo(current)) { "تعذّر إعادة البيانات السابقة" }
                } else if (index !in existed) {
                    // This path was absent before the restore; remove a newly installed copy.
                    if (current.isDirectory) require(current.deleteRecursively())
                    else if (current.exists()) require(current.delete())
                }
            }
            require(state.deleteRecursively()) { "تعذّر تنظيف سجل الاسترجاع" }
        }
    }
}
