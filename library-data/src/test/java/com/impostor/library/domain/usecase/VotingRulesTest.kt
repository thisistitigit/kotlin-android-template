package com.impostor.library.domain.usecase

import com.impostor.library.domain.enums.RoleType
import com.impostor.library.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class VotingRulesTest {
    private val state = VotingSnapshot(1, 1, RoleType.IMPOSTOR, VotingPhaseStatus.OPEN,
        listOf(10, 20, 30), listOf(10, 20, 30), emptyList(), emptyList(), false, null, 1, false)

    @Test fun tieRetainsEveryLeaderIndependentOfBallotOrder() {
        val ballots = listOf(Ballot(10, 20), Ballot(20, 30))
        assertEquals(listOf(20, 30), VotingRules.leaders(ballots))
        assertEquals(listOf(20, 30), VotingRules.leaders(ballots.reversed()))
    }

    @Test fun acceptsEligibleCandidateForCurrentVoter() { VotingRules.validate(10, 20, state) }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsSelfVote() { VotingRules.validate(10, 10, state) }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsWrongTurn() { VotingRules.validate(20, 30, state) }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsEliminatedOrNonRunoffCandidate() { VotingRules.validate(10, 30, state.copy(candidateIds = listOf(20))) }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsVoteDuringHandoff() { VotingRules.validate(10, 20, state.copy(pendingHandoff = true)) }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsChangingSubmittedVote() {
        VotingRules.validate(10, 30, state.copy(ballots = listOf(Ballot(10, 20))))
    }

    @Test fun scoresAllResolvedPhasesWithoutOverwritingSameVoter() {
        val result = CalculateScoreUseCase().sequential(1,
            mapOf(10 to RoleType.IMPOSTOR, 20 to RoleType.MR_WHITE, 30 to RoleType.CIVILIAN),
            setOf(10, 20), listOf(Ballot(30, 10), Ballot(30, 20))).result
        assertEquals(setOf(RoleType.CIVILIAN), result.winningRoles)
        assertEquals(7, result.scores.getValue(30))
    }
}
