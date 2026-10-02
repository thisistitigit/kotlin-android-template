package com.impostor.library.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "game_players",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["game_id"],
            childColumns = ["game_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["player_id"],
            childColumns = ["player_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("game_id"),
        Index("player_id"),
        Index(value = ["game_id", "player_id"], unique = true)
    ]
)
data class GamePlayerEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "game_player_id")
    val id: Int = 0,

    @ColumnInfo(name = "game_id")
    val gameId: Int,

    @ColumnInfo(name = "player_id")
    val playerId: Int,

    @ColumnInfo(name = "seat_order")
    val seatOrder: Int
)
