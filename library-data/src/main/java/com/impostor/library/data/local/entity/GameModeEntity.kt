package com.impostor.library.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.impostor.library.domain.enums.GameModeType

@Entity(tableName = "game_modes", indices = [Index(value = ["type"], unique = true)])
data class GameModeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "game_mode_id")
    val id: Int = 0,
    val type: GameModeType,
    val description: String
)
