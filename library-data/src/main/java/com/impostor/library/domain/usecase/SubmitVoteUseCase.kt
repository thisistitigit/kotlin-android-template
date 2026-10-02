package com.impostor.library.domain.usecase

import com.impostor.library.data.local.entity.VoteEntity
import com.impostor.library.data.repository.GameRepository

class SubmitVoteUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(roundId: Int, phaseId: Int, voterId: Int, votedPlayerId: Int) {
        require(voterId != votedPlayerId) { "A player cannot vote for themselves." }
        repository.submitVote(
            VoteEntity(
                roundId = roundId,
                phaseId = phaseId,
                voterGamePlayerId = voterId,
                votedGamePlayerId = votedPlayerId
            )
        )
    }
}
