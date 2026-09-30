package com.ncorti.kotlin.template.library.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.ncorti.kotlin.template.library.data.local.entity.ContentEntity
import com.ncorti.kotlin.template.library.data.local.entity.GamePlayerEntity
import com.ncorti.kotlin.template.library.data.local.entity.RoleEntity
import com.ncorti.kotlin.template.library.data.local.entity.RoundAssignmentEntity

data class RoundAssignmentDetails(
    @Embedded
    val assignment: RoundAssignmentEntity,

    @Relation(
        parentColumn = "game_player_id",
        entityColumn = "game_player_id"
    )
    val gamePlayer: GamePlayerEntity,

    @Relation(
        parentColumn = "role_id",
        entityColumn = "role_id"
    )
    val role: RoleEntity,

    @Relation(
        parentColumn = "content_id",
        entityColumn = "content_id"
    )
    val content: ContentEntity?
)
