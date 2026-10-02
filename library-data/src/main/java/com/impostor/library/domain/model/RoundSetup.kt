package com.impostor.library.domain.model

import com.impostor.library.data.local.entity.ContentEntity
import com.impostor.library.data.local.entity.GamePlayerEntity
import com.impostor.library.data.local.entity.RoleEntity

data class RoundSetup(
    val roundId: Int,
    val players: List<GamePlayerEntity>,
    val impostorCount: Int,
    val mrWhiteCount: Int,
    val civilianRole: RoleEntity,
    val impostorRole: RoleEntity,
    val mrWhiteRole: RoleEntity?,
    val civilianContent: ContentEntity?,
    val impostorContent: ContentEntity?
)
