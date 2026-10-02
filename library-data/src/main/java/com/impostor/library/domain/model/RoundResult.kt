package com.impostor.library.domain.model

import com.impostor.library.domain.enums.RoleType

data class RoundResult(
    val roundId: Int,
    val eliminatedPlayerIds: Set<Int>,
    val impostorIds: Set<Int>,
    val mrWhiteIds: Set<Int>,
    val mrWhiteWinnerId: Int?,
    val winningRoles: Set<RoleType>,
    val scores: Map<Int, Int>
)
