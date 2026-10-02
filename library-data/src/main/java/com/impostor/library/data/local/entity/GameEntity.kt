package com.impostor.library.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.impostor.library.domain.enums.GameStatus

/** An open-ended game session. Each round chooses its own mode. */
@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "game_id")
    val id: Int = 0,
    val status: GameStatus = GameStatus.CREATED,
    @ColumnInfo(name = "player_count")
    val playerCount: Int,
    @ColumnInfo(name = "impostor_count")
    val impostorCount: Int,
    @ColumnInfo(name = "mr_white_count")
    val mrWhiteCount: Int = 0,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "finished_at")
    val finishedAt: Long? = null
)
