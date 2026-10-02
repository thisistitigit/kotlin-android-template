package com.impostor.library.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.impostor.library.data.local.entity.GamePlayerEntity
import com.impostor.library.data.local.entity.PlayerEntity

data class GamePlayerWithPlayer(
    @Embedded
    val gamePlayer: GamePlayerEntity,

    @Relation(
        parentColumn = "player_id",
        entityColumn = "player_id"
    )
    val player: PlayerEntity
)
