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
                            val source = File(cursor.getString(1)).canonicalFile
                            require(source.parentFile?.parentFile == root &&
                                source.parentFile?.name in setOf("photos", "custom_audio", "recordings") &&
                                source.isFile && source.length() > 0L) {
                                "صورة أو تسجيل مرتبط ببطاقة غير متاح في مساحة التطبيق؛ لم تُنشأ نسخة ناقصة"
                            }
                            val relative = copied[source.path] ?: run {
                                val folder = if (source.parentFile?.name == "photos") "photos" else "custom_audio"
                                val extension = source.extension.takeIf { it.matches(Regex("[A-Za-z0-9]{1,10}")) } ?: "bin"
                                val path = "$folder/media_${UUID.randomUUID()}.$extension"
                                require(entries.size + 2 <= maxEntries) { "عدد ملفات النسخة كبير جداً" }
                                val remaining = maxBytes - bytes
                                require(remaining > 0 && source.length() <= remaining) { "النسخة أكبر من الحد المسموح" }
                                val target = File(directory, "files/$path")
                                require(target.parentFile!!.isDirectory || target.parentFile!!.mkdirs())
                                source.inputStream().use { SafeMediaImport.copy(it, target, remaining) }
                                bytes += target.length()
                                entries["files/$path"] = target
                                copied[source.path] = path
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
