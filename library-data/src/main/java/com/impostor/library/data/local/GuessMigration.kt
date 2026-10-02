package com.impostor.library.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val GUESS_MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE voting_phases ADD COLUMN guess_text TEXT")
        db.execSQL("ALTER TABLE voting_phases ADD COLUMN guess_correct INTEGER")
    }
}
