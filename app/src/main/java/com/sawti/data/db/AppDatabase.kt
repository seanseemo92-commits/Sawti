package com.sawti.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Schema history
 *  v1  family_members / voice_recordings / pain_reports
 *  v2  unified categories + word_items          (MIGRATION_1_2 copies the family members, photos, phones, recordings)
 *  v3  favourites, sentence prompt, progress    (MIGRATION_2_3 only ALTERs / CREATEs)
 *  v5  stable image keys                       (MIGRATION_4_5 only ALTERs)
 * There is NO destructive fallback: an unsupported path fails loudly instead of wiping data.
 * New vocabulary is added at startup by WordRepository.seedOrUpdateDefaults() because Room's onCreate
 * callback does not run for upgraded installs.
 */
@Database(
    entities = [CategoryEntity::class, WordItemEntity::class, ProgressRecordEntity::class],
    version = 6,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    internal val storage = com.sawti.data.StorageCoordinator()
    abstract fun wordDao(): WordDao
    abstract fun progressDao(): ProgressDao

    companion object {
        /** v1 → v2: keep every family member (ids, photos, phones) and their recordings. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS categories (" +
                        "id TEXT NOT NULL, title TEXT NOT NULL, palette INTEGER NOT NULL, " +
                        "previewOnTap INTEGER NOT NULL, sortOrder INTEGER NOT NULL, PRIMARY KEY(id))"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS word_items (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, categoryId TEXT NOT NULL, " +
                        "subgroup TEXT NOT NULL, displayText TEXT NOT NULL, ttsPrompt TEXT NOT NULL, " +
                        "phonemicCue TEXT NOT NULL, audioPath TEXT, photoPath TEXT, phone TEXT NOT NULL, " +
                        "emoji TEXT NOT NULL, sortOrder INTEGER NOT NULL, " +
                        "FOREIGN KEY(categoryId) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_word_items_categoryId ON word_items (categoryId)")
                db.execSQL("INSERT INTO categories (id, title, palette, previewOnTap, sortOrder) VALUES ('family', 'عائلتي', 0, 1, 0)")
                db.execSQL(
                    "INSERT INTO word_items (id, categoryId, subgroup, displayText, ttsPrompt, phonemicCue, " +
                        "audioPath, photoPath, phone, emoji, sortOrder) " +
                        "SELECT f.id, 'family', " +
                        "CASE f.relation WHEN 'HUSBAND' THEN 'الزوج' WHEN 'CHILDREN' THEN 'الأبناء' " +
                        "WHEN 'SISTERS' THEN 'الأخوات' ELSE 'الإخوة' END, " +
                        "f.name, f.name, '', " +
                        "(SELECT v.filePath FROM voice_recordings v WHERE v.recordingKey = 'family:' || f.id), " +
                        "f.photoPath, f.phone, '', f.sortOrder FROM family_members f"
                )
            }
        }

        /** v2 → v3: favourites, sentence prompt, progress records. Existing rows are untouched. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE word_items ADD COLUMN sentencePrompt TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE word_items ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS progress_records (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "wordId INTEGER NOT NULL, " +
                        "timestamp INTEGER NOT NULL, " +
                        "mode TEXT NOT NULL, " +
                        "outcome TEXT NOT NULL)"
                )
            }
        }

        /** v3 → v4: remove only unchanged historical sample people. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                LegacyFamilyCleanup.removeUntouchedOldSamples(db)
            }
        }

        /** Non-destructive v4→v5 upgrade: keep every card, photo, favourite and recording. */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE word_items ADD COLUMN semanticKey TEXT DEFAULT NULL")
            }
        }

        /** v5→v6: additive customization metadata; never rewrite existing card values. */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE word_items ADD COLUMN customTextFields INTEGER NOT NULL DEFAULT 0")
                val defaults = WordSeed.itemEntities()
                val byKey = defaults.filter { it.semanticKey != null }.associateBy { Triple(it.categoryId, it.subgroup, it.semanticKey) }
                val byName = defaults.associateBy { Triple(it.categoryId, it.subgroup, it.displayText) }
                db.query("SELECT id,categoryId,subgroup,displayText,ttsPrompt,phonemicCue,sentencePrompt,semanticKey FROM word_items").use { rows ->
                    while (rows.moveToNext()) {
                        val original = byKey[Triple(rows.getString(1), rows.getString(2), rows.getString(7))]
                            ?: byName[Triple(rows.getString(1), rows.getString(2), rows.getString(3))]
                        // Unknown/renamed historical cards are personal data: preserve all text.
                        var flags = if (original == null) 15 else 0
                        if (original != null) {
                            if (rows.getString(3) != original.displayText) flags = flags or 1
                            if (rows.getString(4) != original.ttsPrompt) flags = flags or 2
                            if (rows.getString(5) != original.phonemicCue) flags = flags or 4
                            if (rows.getString(6) != original.sentencePrompt) flags = flags or 8
                        }
                        db.execSQL("UPDATE word_items SET customTextFields=? WHERE id=?", arrayOf(flags, rows.getLong(0)))
                    }
                }
            }
        }

        fun build(context: Context): AppDatabase = builder(context, "my_words.db").build()

        /** Same migrations as production; isolated restore validation must never use a fallback. */
        internal fun builder(context: Context, name: String): RoomDatabase.Builder<AppDatabase> =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, name)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
    }
}

