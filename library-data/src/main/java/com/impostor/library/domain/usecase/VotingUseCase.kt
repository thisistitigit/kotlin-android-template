package com.impostor.library.domain.usecase

import com.impostor.library.data.repository.GameRepository

/** Application operations expose domain snapshots, never Room entities to voting UI. */
class VotingUseCase(private val repository: GameRepository) {
    suspend fun begin(roundId: Int) = repository.beginVoting(roundId)
    suspend fun get(roundId: Int) = repository.votingSnapshot(roundId)
    suspend fun acknowledge(roundId: Int) = repository.acknowledgeVote(roundId)
    suspend fun reveal(roundId: Int) = repository.revealVotedRole(roundId)
    suspend fun continuePhase(roundId: Int) = repository.continueVoting(roundId)
}
