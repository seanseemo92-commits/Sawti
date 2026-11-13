package com.sawti.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.util.UUID

/** Build an immutable, referenced-only media set while the shared storage gate is held. */
internal object BackupMediaSnapshot {
    fun stage(
        context: Context, snapshot: File, directory: File,
        entries: MutableMap<String, File>, maxBytes: Long, maxEntries: Int,
    ) {
        var bytes = entries.values.sumOf { it.length() }
        require(bytes <= maxBytes) { "النسخة أكبر من الحد المسموح" }
        val copied = mutableMapOf<String, String>()
        val root = context.filesDir.canonicalFile
        SQLiteDatabase.openDatabase(snapshot.path, null, SQLiteDatabase.OPEN_READWRITE).use { sqlite ->
            sqlite.rawQuery("PRAGMA journal_mode=DELETE", null).use { check(it.moveToFirst()) }
            sqlite.beginTransaction()
            try {
                for (column in listOf("photoPath", "audioPath")) {
                    sqlite.rawQuery("SELECT id, $column FROM word_items WHERE $column IS NOT NULL", null).use { cursor ->
                        while (cursor.moveToNext()) {
                            // A missing/private media reference is removed ONLY from the
                            // snapshot database. Live Room records remain untouched.
                            val rawPath = cursor.getString(1)
                            val existing = File(rawPath).canonicalFile
                            // An external or malformed path is still rejected: never
                            // silently make a valid but unsafe reference disappear.
                            require(existing.parentFile?.parentFile == root &&
                                existing.parentFile?.name in setOf("photos", "custom_audio")) {
                                "ملف مرتبط خارج مساحة التطبيق الخاصة"
                            }
                            if (!existing.isFile || existing.length() <= 0L) {
                                sqlite.execSQL("UPDATE word_items SET $column=NULL WHERE id=?", arrayOf(cursor.getLong(0)))
                                continue
                            }
                            val relative = copied[existing.path] ?: run {
                                val folder = if (existing.parentFile?.name == "photos") "photos" else "custom_audio"
                                val extension = existing.extension.takeIf { it.matches(Regex("[A-Za-z0-9]{1,10}")) } ?: "bin"
                                val path = "$folder/media_${UUID.randomUUID()}.$extension"
                                require(entries.size + 2 <= maxEntries) { "عدد ملفات النسخة كبير جداً" }
                                val remaining = maxBytes - bytes
                                require(remaining > 0 && existing.length() <= remaining) { "النسخة أكبر من الحد المسموح" }
                                val target = File(directory, "files/$path")
                                require(target.parentFile!!.isDirectory || target.parentFile!!.mkdirs())
                                existing.inputStream().use { SafeMediaImport.copy(it, target, remaining) }
                                bytes += target.length()
                                entries["files/$path"] = target
                                copied[existing.path] = path
                                path
                            }
                            // Only the copied database is rewritten. Live paths and files stay intact.
                            sqlite.execSQL("UPDATE word_items SET $column=? WHERE id=?", arrayOf(relative, cursor.getLong(0)))
                        }
                    }
                }
                sqlite.setTransactionSuccessful()
            } finally {
                sqlite.endTransaction()
            }
        }
        require(entries.values.sumOf { it.length() } <= maxBytes) { "النسخة أكبر من الحد المسموح" }
    }
}
