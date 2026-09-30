package com.ncorti.kotlin.template.library.data.local.entity


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "round_assignments",
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
        ),
        ForeignKey(
            entity = RoleEntity::class,
            parentColumns = ["role_id"],
            childColumns = ["role_id"]
        ),
        ForeignKey(
            entity = ContentEntity::class,
            parentColumns = ["content_id"],
            childColumns = ["content_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("round_id"),
        Index("game_player_id"),
        Index("role_id"),
        Index("content_id"),
        Index(value = ["round_id", "game_player_id"], unique = true)
    ]
)
data class RoundAssignmentEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "assignment_id")
    val id: Int = 0,

    @ColumnInfo(name = "round_id")
    val roundId: Int,

    @ColumnInfo(name = "game_player_id")
    val gamePlayerId: Int,

    @ColumnInfo(name = "role_id")
    val roleId: Int,

    @ColumnInfo(name = "content_id")
    val contentId: Int? = null
)
