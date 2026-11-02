package com.sawti.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** New Sawti foundation. Future releases must migrate this schema without deleting user data. */
@Database(
    entities = [CategoryEntity::class, WordItemEntity::class, ProgressRecordEntity::class],
    version = AppDatabase.VERSION,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    internal val storage = com.sawti.data.StorageCoordinator()
    abstract fun wordDao(): WordDao
    abstract fun progressDao(): ProgressDao

    companion object {
        const val VERSION = 1
        const val NAME = "sawti.db"
        const val IDENTITY = "sawti-foundation"

        fun build(context: Context): AppDatabase = builder(context, NAME).build()

        /** No historical migrations or destructive fallback, including during staged validation. */
        internal fun builder(context: Context, name: String): RoomDatabase.Builder<AppDatabase> =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, name)
    }
}
