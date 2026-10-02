package com.impostor.library.domain.model

import com.impostor.library.domain.enums.RoleType

enum class VotingPhaseStatus { OPEN, RESULTS, TIE, GUESS_PENDING, REVEALED }

data class Ballot(val voterId: Int, val candidateId: Int)

data class VotingSnapshot(
    val phaseId: Int,
    val roundId: Int,
    val target: RoleType,
    val status: VotingPhaseStatus,
    val voterIds: List<Int>,
    val candidateIds: List<Int>,
    val ballots: List<Ballot>,
    val leaderIds: List<Int>,
    val pendingHandoff: Boolean,
    val revealedRole: RoleType?,
    val remainingAdversaries: Int,
    val roundFinished: Boolean
) {
    val nextVoterId: Int? get() = voterIds.firstOrNull { id -> ballots.none { it.voterId == id } }
}

/** Pure rules, also used by the transactional persistence boundary. */
object VotingRules {
    fun leaders(ballots: List<Ballot>): List<Int> {
        val counts = ballots.groupingBy { it.candidateId }.eachCount()
        val maximum = counts.values.maxOrNull() ?: return emptyList()
        return counts.filterValues { it == maximum }.keys.sorted()
    }

    fun validate(voterId: Int, candidateId: Int, snapshot: VotingSnapshot) {
        require(snapshot.status == VotingPhaseStatus.OPEN && !snapshot.pendingHandoff)
        require(snapshot.nextVoterId == voterId) { "It is another player's turn." }
        require(candidateId in snapshot.candidateIds && voterId != candidateId) { "Invalid candidate." }
        require(snapshot.ballots.none { it.voterId == voterId }) { "Vote already submitted." }
    }
}
