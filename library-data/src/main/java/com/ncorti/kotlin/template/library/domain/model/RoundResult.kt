package com.ncorti.kotlin.template.library.domain.model

import com.ncorti.kotlin.template.library.domain.enums.RoleType

data class RoundResult(
    val roundId: Int,
    val eliminatedPlayerIds: Set<Int>,
    val impostorIds: Set<Int>,
    val mrWhiteId: Int?,
    val winningRoles: Set<RoleType>,
    val scores: Map<Int, Int>
)
