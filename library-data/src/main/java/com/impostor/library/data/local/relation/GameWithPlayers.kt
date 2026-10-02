package com.impostor.library.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.impostor.library.data.local.entity.GameEntity
import com.impostor.library.data.local.entity.GamePlayerEntity

data class GameWithPlayers(
    @Embedded
    val game: GameEntity,

    @Relation(
        entity = GamePlayerEntity::class,
        parentColumn = "game_id",
        entityColumn = "game_id"
    )
    val players: List<GamePlayerWithPlayer>
)
