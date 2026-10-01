package com.ncorti.kotlin.template.library.domain.usecase

import com.ncorti.kotlin.template.library.data.local.entity.RoundAssignmentEntity
import com.ncorti.kotlin.template.library.domain.enums.RoleType
import com.ncorti.kotlin.template.library.domain.model.RoundSetup

class AssignRolesUseCase(private val shuffle: (List<Int>) -> List<Int> = { it.shuffled() }) {
    operator fun invoke(setup: RoundSetup): List<RoundAssignmentEntity> {
        validate(setup)
        val shuffledIds = shuffle(setup.players.map { it.id })
        val impostorIds = shuffledIds.take(setup.impostorCount).toSet()
        val mrWhiteIds = shuffledIds
            .drop(setup.impostorCount)
            .take(setup.mrWhiteCount)
            .toSet()

        return setup.players.map { player ->
            val role = when (player.id) {
                in mrWhiteIds -> requireNotNull(setup.mrWhiteRole)
                in impostorIds -> setup.impostorRole
                else -> setup.civilianRole
            }
            RoundAssignmentEntity(
                roundId = setup.roundId,
                gamePlayerId = player.id,
                roleId = role.id,
                contentId = when (role.type) {
                    RoleType.CIVILIAN -> setup.civilianContent?.id
                    RoleType.IMPOSTOR -> setup.impostorContent?.id
                    RoleType.MR_WHITE -> null
                }
            )
        }
    }

    private fun validate(setup: RoundSetup) {
        require(setup.players.map { it.id }.distinct().size == setup.players.size)
        require(setup.impostorCount > 0)
        require(setup.mrWhiteCount >= 0)
        val adversaryCount = setup.impostorCount + setup.mrWhiteCount
        require(adversaryCount < setup.players.size)
        require(setup.civilianRole.type == RoleType.CIVILIAN)
        require(setup.impostorRole.type == RoleType.IMPOSTOR)
        require(setup.mrWhiteCount == 0 || setup.mrWhiteRole?.type == RoleType.MR_WHITE)
    }
}
