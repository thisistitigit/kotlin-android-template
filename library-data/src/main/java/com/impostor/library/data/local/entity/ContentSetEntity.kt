package com.impostor.library.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.impostor.library.domain.enums.GameModeType

@Entity(
    tableName = "content_sets",
    foreignKeys = [
        ForeignKey(
            entity = ContentPackEntity::class,
            parentColumns = ["pack_id"],
            childColumns = ["pack_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pack_id")]
)
data class ContentSetEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "content_set_id")
    val id: Int = 0,

    @ColumnInfo(name = "pack_id")
    val packId: Int,

    val description: String? = null,

    @ColumnInfo(name = "game_mode_type")
    val gameModeType: GameModeType,

    val category: String? = null
)
