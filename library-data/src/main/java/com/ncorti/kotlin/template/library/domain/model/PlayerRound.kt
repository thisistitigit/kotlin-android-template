package com.ncorti.kotlin.template.library.domain.model

import com.ncorti.kotlin.template.library.domain.enums.RoleType

data class PlayerRound(
    val gamePlayerId: Int,
    val playerName: String,
    val photoUri: String?,
    val role: RoleType,
    val secretContent: String?
)
