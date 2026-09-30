package com.ncorti.kotlin.template.library.data.local

import android.content.Context
import androidx.room.Room

object DatabaseFactory {
    fun create(context: Context, name: String = DATABASE_NAME): AppDatabase =
        Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, name).build()

    private const val DATABASE_NAME = "impostor.db"
}
