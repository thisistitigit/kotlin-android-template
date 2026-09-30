package com.ncorti.kotlin.template.library.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "answers",
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
        Index("game_player_id"),
        Index(value = ["round_id", "game_player_id"], unique = true)
    ]
)
data class AnswerEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "answer_id")
    val id: Int = 0,

    @ColumnInfo(name = "round_id")
    val roundId: Int,

    @ColumnInfo(name = "game_player_id")
    val gamePlayerId: Int,

    @ColumnInfo(name = "answer_text")
    val answerText: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
