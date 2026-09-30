package com.ncorti.kotlin.template.library.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ncorti.kotlin.template.library.domain.enums.RoleType

@Entity(tableName = "roles", indices = [Index(value = ["type"], unique = true)])
data class RoleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "role_id")
    val id: Int = 0,

    val name: String,
    val type: RoleType,
    val description: String? = null
)
