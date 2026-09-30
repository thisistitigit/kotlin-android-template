package com.ncorti.kotlin.template.library.domain.usecase

import com.ncorti.kotlin.template.library.data.local.entity.ContentEntity
import com.ncorti.kotlin.template.library.data.repository.GameRepository
import com.ncorti.kotlin.template.library.domain.enums.GameModeType
import com.ncorti.kotlin.template.library.domain.enums.RoleType
import com.ncorti.kotlin.template.library.domain.model.RoundSetup

/** Creates and fully configures the next round in one application-level operation. */
class StartConfiguredRoundUseCase(
    private val repository: GameRepository,
    private val assignRoles: AssignRolesUseCase
) {
    suspend operator fun invoke(
        gameId: Int,
        mode: GameModeType,
        civilianContent: ContentEntity?,
        impostorContent: ContentEntity?,
        contentSetId: Int? = civilianContent?.contentSetId
    ): Long {
        repository.prepareDefaults()
        val game = requireNotNull(repository.getGame(gameId)) { "Game not found." }
        val modeId = requireNotNull(repository.getMode(mode)?.id)
        val roundId = repository.startNextRound(gameId, modeId, contentSetId).toInt()
        val setup = RoundSetup(
            roundId = roundId,
            players = game.players.map { it.gamePlayer },
            impostorCount = game.game.impostorCount,
            includeMrWhite = game.game.includeMrWhite,
            civilianRole = requireNotNull(repository.getRole(RoleType.CIVILIAN)),
            impostorRole = requireNotNull(repository.getRole(RoleType.IMPOSTOR)),
            mrWhiteRole = repository.getRole(RoleType.MR_WHITE),
            civilianContent = civilianContent,
            impostorContent = impostorContent
        )
        repository.saveAssignments(roundId, assignRoles(setup))
        return roundId.toLong()
    }
}
