package com.impostor.library.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.impostor.library.domain.enums.RoundStatus

@Entity(
    tableName = "rounds",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["game_id"],
            childColumns = ["game_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GameModeEntity::class,
            parentColumns = ["game_mode_id"],
            childColumns = ["game_mode_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = ContentSetEntity::class,
            parentColumns = ["content_set_id"],
            childColumns = ["content_set_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("game_id"),
        Index("game_mode_id"),
        Index("content_set_id"),
        Index(value = ["game_id", "round_number"], unique = true)
    ]
)
data class RoundEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "round_id")
    val id: Int = 0,
    @ColumnInfo(name = "game_id")
    val gameId: Int,
    @ColumnInfo(name = "game_mode_id")
    val gameModeId: Int,
    @ColumnInfo(name = "content_set_id")
    val contentSetId: Int? = null,
    @ColumnInfo(name = "round_number")
    val roundNumber: Int,
    val status: RoundStatus = RoundStatus.CREATED,
    @ColumnInfo(name = "started_at")
    val startedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "finished_at")
    val finishedAt: Long? = null
)
