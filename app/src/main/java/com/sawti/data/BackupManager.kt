package com.sawti.data

import android.content.Context
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Xml
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

/**
 * Restoring the old files after a failed swap does not reopen a previously closed
 * Room instance. The caller must block further database use until process restart.
 */
class RestoreRequiresRestartException(message: String, cause: Throwable) :
    IllegalStateException(message, cause)

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
            val entries = database.storage.access {
                createDatabaseSnapshot(db)
                validateDatabaseSnapshot(db)
                val staged = linkedMapOf<String, File>("database/$dbName" to db)
                // getAll() is one in-memory settings snapshot; never zip a live XML file
                // while SharedPreferences may atomically replace it on another thread.
                val values = context.getSharedPreferences("settings", Context.MODE_PRIVATE).all
                val prefs = File(stage, "settings.xml")
                prefs.outputStream().use { output ->
                    val xml = Xml.newSerializer()
                    xml.setOutput(output, "UTF-8")
                    xml.startDocument("UTF-8", true)
                    xml.startTag(null, "map")
                    values.forEach { (key, value) ->
                        when (value) {
                            is Boolean -> xml.startTag(null, "boolean").attribute(null, "name", key)
                                .attribute(null, "value", value.toString()).endTag(null, "boolean")
                            is String -> xml.startTag(null, "string").attribute(null, "name", key)
                                .text(value).endTag(null, "string")
                            else -> error("نوع إعداد غير مدعوم في النسخة")
                        }
                    }
                    xml.endTag(null, "map")
                    xml.endDocument()
                }
                prefs.writeText(RestoreSettingsValidator.canonicalXml(prefs))
                staged["preferences/settings.xml"] = prefs
                BackupMediaSnapshot.stage(context, db, stage, staged, maxBytes, maxEntries)
                staged
            }
            // All subsequent hashing/ZIP/SAF work uses only immutable staging files.
            // Repository writes may resume without changing this backup's contents.
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
                .put("version", 2)
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
            createCheckpointSnapshot(destination)
        }
    }

    /** Older SQLite fallback. Caller must hold database.storage for the complete copy. */
    internal fun createCheckpointSnapshot(destination: File) {
        val room = database.openHelper.writableDatabase
        room.query("PRAGMA wal_checkpoint(FULL)").use { cursor ->
            require(cursor.moveToFirst() && cursor.getInt(0) == 0) {
                "قاعدة البيانات مشغولة؛ حاول مرة أخرى"
            }
        }
        context.getDatabasePath(dbName).copyTo(destination)
        validateDatabaseSnapshot(destination)
    }

    private fun validateDatabaseSnapshot(snapshot: File) {
        require(snapshot.isFile && snapshot.length() > 4096) { "قاعدة البيانات غير مكتملة" }
        SQLiteDatabase.openDatabase(snapshot.path, null, SQLiteDatabase.OPEN_READONLY).use { sqlite ->
            sqlite.rawQuery("PRAGMA integrity_check", null).use { cursor ->
                require(cursor.moveToFirst() && cursor.getString(0) == "ok") {
                    "قاعدة البيانات غير سليمة"
                }
            }
            require(sqlite.version == 7) { "إصدار قاعدة البيانات غير مدعوم" }
        }
    }

    /**
     * Restore replaces existing data only after validating the whole archive. A durable
     * recovery journal permits rollback if the process/device stops halfway through.
     * Caller should restart the app after success, to reload Room and preferences.
     */
    suspend fun restore(uri: Uri) = database.storage.access {
        withContext(Dispatchers.IO) { restoreLocked(uri) }
    }

    private fun restoreLocked(uri: Uri) {
        // Startup recovery happens before application stores open. Never finalize another
        // restore from a process that still has old Room/preferences instances.
        require(!stateDir(context).exists() && !committedMarker(context).exists()) {
            "أعد تشغيل التطبيق لإكمال الاسترجاع السابق"
        }
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
                                if (name == "preferences/settings.xml") {
                                    require(target.length() + n <= RestoreSettingsValidator.MAX_BYTES) {
                                        "ملف الإعدادات كبير جداً"
                                    }
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
            val formatVersion = manifest.getInt("version")
            require(manifest.getString("format") == "sawti-backup" && formatVersion in 1..2)
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
            // Validate actual Room schema (including migrations), never trust an imported identity hash.
            RestoreDatabaseValidator.validateAndMigrate(context, stagedDb)
            val stagedPrefs = File(stage, "preferences/settings.xml")
            val validatedPrefs = if (stagedPrefs.isFile) RestoreSettingsValidator.canonicalXml(stagedPrefs) else null
            SQLiteDatabase.openDatabase(stagedDb.path, null, SQLiteDatabase.OPEN_READWRITE).use { sqlite ->
                for ((column, oldFolder) in listOf("photoPath" to "photos", "audioPath" to "custom_audio")) {
                    sqlite.rawQuery("SELECT id, $column FROM word_items WHERE $column IS NOT NULL", null).use { cursor ->
                        while (cursor.moveToNext()) {
                            val oldPath = cursor.getString(1)
                            val relative = if (formatVersion == 1) "$oldFolder/${File(oldPath).name}" else oldPath
                            val parts = relative.split('/')
                            require(parts.size == 2 && parts[0] in folders && validMediaName(parts[1])) {
                                "مسار مرفق غير صالح في النسخة"
                            }
                            require(File(stage, "files/$relative").isFile) {
                                "النسخة تفتقد صورة أو تسجيلاً مرتبطاً ببطاقة؛ لم تتغير بياناتك الحالية"
                            }
                            val newPath = File(context.filesDir, relative).absolutePath
                            sqlite.execSQL("UPDATE word_items SET $column=? WHERE id=?", arrayOf(newPath, cursor.getLong(0)))
                        }
                    }
                }
            }
            // The incoming replacement must be prepared completely before any live file moves.
            val live = livePaths(context)
            val state = stateDir(context)
            val previous = File(state, "previous")
            val incoming = File(state, "incoming")
            require(state.mkdirs() && previous.mkdirs() && incoming.mkdirs()) { "تعذّر تجهيز الاسترجاع الآمن" }
            var closeAttempted = false
            try {
                stagedDb.copyTo(File(incoming, "0"))
                if (validatedPrefs != null) File(incoming, "5").writeText(validatedPrefs)
                // Wait for pending apply() writes before moving the old preference files.
                require(context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().commit()) {
                    "تعذّر حفظ الإعدادات الحالية"
                }
                folders.forEachIndexed { index, folder ->
                    val source = File(stage, "files/$folder")
                    if (source.isDirectory) require(source.copyRecursively(File(incoming, "${index + 3}")))
                }
                // Close Room first: SQLite may checkpoint and remove WAL/SHM files.
                // Journal only paths that actually remain available for moving.
                closeAttempted = true
                database.storage.retire()
                database.close()
                val existing = live.indices.filter { live[it].exists() }.joinToString(",")
                val temp = File(state, "journal.tmp")
                FileOutputStream(temp).use { stream ->
                    stream.write(existing.toByteArray(Charsets.US_ASCII))
                    stream.fd.sync()
                }
                require(temp.renameTo(File(state, "journal"))) { "تعذّر بدء استرجاع آمن" }
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
                // Keep previous files until a NEW process successfully opens the installed
                // database and validates settings. Installation alone is not a commit.
                FileOutputStream(File(state, "installed")).use { output ->
                    output.write(1)
                    output.fd.sync()
                }
            } catch (error: Exception) {
                // Roll back the files, but never reuse Room if closing it was attempted.
                // A failed rollback leaves the journal for recovery on next launch.
                val recoveryError = runCatching { rollback(context) }.exceptionOrNull()
                if (recoveryError != null) error.addSuppressed(recoveryError)
                if (closeAttempted) {
                    throw RestoreRequiresRestartException(
                        if (recoveryError == null)
                            "تعذّر الاسترجاع؛ تمت محاولة إعادة البيانات السابقة. أغلق التطبيق وافتحه مجدداً."
                        else
                            "تعذّر الاسترجاع وإعادة البيانات السابقة بالكامل. أغلق التطبيق لبدء الاسترداد الآمن عند التشغيل.",
                        error,
                    )
                }
                if (recoveryError != null) throw recoveryError
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
                File(context.applicationInfo.dataDir, "shared_prefs/settings.xml"),
                // Append only: older journals use the first six indices.
                File(context.applicationInfo.dataDir, "shared_prefs/settings.xml.bak"),
                File(db.path + "-journal"))
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
            if (File(state, "installed").exists()) {
                try {
                    // This is called before any application Room/SharedPreferences consumer.
                    RestoreDatabaseValidator.validateAndMigrate(context, context.getDatabasePath("my_words.db"))
                    val settings = File(context.applicationInfo.dataDir, "shared_prefs/settings.xml")
                    if (settings.exists()) RestoreSettingsValidator.canonicalXml(settings)
                } catch (_: Exception) {
                    // Bad installed data never destroys the retained original. No app stores
                    // have been opened yet, so the normal startup can safely use the originals.
                    rollback(context)
                    return
                }
                // A restored database may contain older seed content than the current install.
                // Reset the independent seed marker so the next launch reconciles missing words.
                require(context.getSharedPreferences("seed", Context.MODE_PRIVATE).edit()
                    .remove("seed_version").commit()) { "تعذّر إعادة تهيئة إصدار المفردات" }
                // Only after a real Room open has succeeded may cleanup become irreversible.
                FileOutputStream(committed).use { it.write(1); it.fd.sync() }
                require(state.deleteRecursively()) { "تعذّر تنظيف سجل الاسترجاع" }
                require(committed.delete()) { "تعذّر إغلاق سجل الاسترجاع" }
                return
            }
            rollback(context)
        }

        private fun rollback(context: Context) {
            val state = stateDir(context)
            if (!state.exists()) return
            // A retry after interruption must resume rollback, never validate a mixture of
            // restored originals and incoming files as a successful new installation.
            val installed = File(state, "installed")
            if (installed.exists()) require(installed.delete()) { "تعذّر بدء إعادة البيانات السابقة" }
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

