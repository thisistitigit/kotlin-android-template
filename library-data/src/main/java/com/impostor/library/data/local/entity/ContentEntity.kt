package com.impostor.library.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.impostor.library.domain.enums.ContentType

@Entity(
    tableName = "contents",
    foreignKeys = [
        ForeignKey(
            entity = ContentSetEntity::class,
            parentColumns = ["content_set_id"],
            childColumns = ["content_set_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("content_set_id")]
)
data class ContentEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "content_id")
    val id: Int = 0,

    @ColumnInfo(name = "content_set_id")
    val contentSetId: Int,

    val text: String,
    val type: ContentType,
    val category: String? = null
)
