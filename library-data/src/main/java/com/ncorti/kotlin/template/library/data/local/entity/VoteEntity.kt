package com.ncorti.kotlin.template.library.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "votes",
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
            childColumns = ["voter_game_player_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GamePlayerEntity::class,
            parentColumns = ["game_player_id"],
            childColumns = ["voted_game_player_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("round_id"),
        Index("voter_game_player_id"),
        Index("voted_game_player_id"),
        Index(value = ["round_id", "voter_game_player_id"], unique = true)
    ]
)
data class VoteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "vote_id")
    val id: Int = 0,

    @ColumnInfo(name = "round_id")
    val roundId: Int,

    @ColumnInfo(name = "voter_game_player_id")
    val voterGamePlayerId: Int,

    @ColumnInfo(name = "voted_game_player_id")
    val votedGamePlayerId: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
