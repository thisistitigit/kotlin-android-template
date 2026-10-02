package com.impostor.library.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "player_id")
    val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "photo_uri")
    val photoUri: String? = null,
    @ColumnInfo(name = "created_at_epoch_millis")
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)
