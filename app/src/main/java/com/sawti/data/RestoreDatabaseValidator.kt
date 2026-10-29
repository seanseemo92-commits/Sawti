package com.sawti.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.RoomDatabase
import com.sawti.data.db.AppDatabase
import java.io.File

/** Only use on a staged copy, or on an installed restore whose rollback is still retained. */
internal object RestoreDatabaseValidator {
    fun validateAndMigrate(context: Context, file: File) {
        require(file.isFile && file.length() > 0) { "قاعدة بيانات النسخة مفقودة" }
        SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE).use { sqlite ->
            require(sqlite.version in 3..6) { "إصدار قاعدة البيانات غير مدعوم" }
            sqlite.rawQuery("PRAGMA integrity_check", null).use {
                require(it.moveToFirst() && it.getString(0) == "ok") { "قاعدة البيانات تالفة" }
            }
            // Imported SQL must not run triggers during migrations or attachment remapping.
            // Sawti has no persistent application triggers or views.
            sqlite.rawQuery("SELECT 1 FROM sqlite_master WHERE type IN ('trigger', 'view') LIMIT 1", null).use {
                require(!it.moveToFirst()) { "تحتوي القاعدة على عناصر غير مدعومة" }
            }
            // A matching identity hash in an untrusted archive is NOT schema validation.
            // Without it Room validates every entity, column, index and foreign key.
            sqlite.execSQL("DROP TABLE IF EXISTS room_master_table")
        }
        val room = AppDatabase.builder(context, file.absolutePath)
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            // Startup recovery runs before any app consumers on the main thread.
            // This permission is confined to this short-lived validation instance.
            .allowMainThreadQueries()
            .build()
        try {
            val db = room.openHelper.writableDatabase
            db.query("PRAGMA foreign_key_check").use {
                require(!it.moveToFirst()) { "مراجع قاعدة البيانات غير سليمة" }
            }
        } finally {
            room.close()
        }
    }
}
