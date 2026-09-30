package com.ncorti.kotlin.template.library.domain.model

import com.ncorti.kotlin.template.library.data.local.entity.ContentEntity
import com.ncorti.kotlin.template.library.data.local.entity.GamePlayerEntity
import com.ncorti.kotlin.template.library.data.local.entity.RoleEntity

data class RoundSetup(
    val roundId: Int,
    val players: List<GamePlayerEntity>,
    val impostorCount: Int,
    val includeMrWhite: Boolean,
    val civilianRole: RoleEntity,
    val impostorRole: RoleEntity,
    val mrWhiteRole: RoleEntity?,
    val civilianContent: ContentEntity?,
    val impostorContent: ContentEntity?
)
