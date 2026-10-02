package com.impostor.library.data.local

import android.content.Context
import androidx.room.Room

object DatabaseFactory {
    fun create(context: Context, name: String = DATABASE_NAME): AppDatabase =
        Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, name)
            .addMigrations(VOTING_MIGRATION_1_2, GUESS_MIGRATION_2_3).build()

    private const val DATABASE_NAME = "impostor.db"
}
