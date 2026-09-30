package com.ncorti.kotlin.template.library.domain.usecase

import com.ncorti.kotlin.template.library.data.local.entity.VoteEntity
import com.ncorti.kotlin.template.library.data.repository.GameRepository

class SubmitVoteUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(roundId: Int, voterId: Int, votedPlayerId: Int) {
        require(voterId != votedPlayerId) { "A player cannot vote for themselves." }
        repository.submitVote(
            VoteEntity(
                roundId = roundId,
                voterGamePlayerId = voterId,
                votedGamePlayerId = votedPlayerId
            )
        )
    }
}
