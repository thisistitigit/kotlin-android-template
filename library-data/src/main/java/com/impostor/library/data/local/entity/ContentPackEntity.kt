package com.impostor.library.data.local.entity


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "content_packs")
data class ContentPackEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pack_id")
    val id: Int = 0,

    val name: String,
    val description: String? = null,
    val language: String = "pt",

    @ColumnInfo(name = "is_custom")
    val isCustom: Boolean = false
)
