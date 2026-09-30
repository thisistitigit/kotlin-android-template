package com.ncorti.kotlin.template.library.domain.usecase

import com.ncorti.kotlin.template.library.data.repository.GameRepository
import com.ncorti.kotlin.template.library.domain.enums.RoleType
import com.ncorti.kotlin.template.library.domain.model.RoundResult

class FinishRoundUseCase(
    private val repository: GameRepository,
    private val calculateScore: CalculateScoreUseCase
) {
    suspend operator fun invoke(roundId: Int, mrWhiteGuessedSecret: Boolean = false): RoundResult {
        val assignments = repository.getAssignments(roundId)
        require(assignments.isNotEmpty()) { "The round has no role assignments." }
        val votes = repository.getVotes(roundId).associate {
            it.voterGamePlayerId to it.votedGamePlayerId
        }
        val roles = assignments.associate { it.gamePlayer.id to it.role.type }
        val calculation = calculateScore(
            roundId = roundId,
            playerIds = roles.keys,
            impostorIds = roles.filterValues { it == RoleType.IMPOSTOR }.keys,
            mrWhiteId = roles.entries.singleOrNull { it.value == RoleType.MR_WHITE }?.key,
            votes = votes,
            mrWhiteGuessedSecret = mrWhiteGuessedSecret
        )
        repository.finishRound(roundId, calculation.events)
        return calculation.result
    }
}
