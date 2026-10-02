package com.impostor.library.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.impostor.library.domain.enums.ScoreReason

@Entity(
    tableName = "score_events",
    foreignKeys = [
        ForeignKey(
            entity = RoundEntity::class,
            parentColumns = ["round_id"],
            childColumns = ["round_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GamePlayerEntity::class,
            parentColumns = ["game_player_id"],
            childColumns = ["game_player_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("round_id"),
        Index("game_player_id")
    ]
)
data class ScoreEventEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "score_event_id")
    val id: Int = 0,

    @ColumnInfo(name = "round_id")
    val roundId: Int,

    @ColumnInfo(name = "game_player_id")
    val gamePlayerId: Int,

    val points: Int,
    val reason: ScoreReason,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
