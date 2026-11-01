package com.sawti.data

import com.sawti.data.db.AppDatabase

/** Archive identity is distinct from all pre-foundation backups, even when their version is 1. */
internal object BackupFormat {
    const val NAME = "sawti-foundation-backup"
    const val VERSION = 1
    const val DATABASE_ENTRY = "database/" + AppDatabase.NAME
}
