package com.impostor.library.data.repository

import androidx.room.withTransaction
import com.impostor.library.data.local.AppDatabase
import com.impostor.library.data.local.entity.VoteEntity
import com.impostor.library.data.local.entity.VotingPhaseEntity
import com.impostor.library.domain.enums.RoleType
import com.impostor.library.domain.enums.RoundStatus
import com.impostor.library.domain.model.Ballot
import com.impostor.library.domain.model.VotingPhaseStatus
import com.impostor.library.domain.model.VotingRules
import com.impostor.library.domain.model.VotingSnapshot
import com.impostor.library.domain.usecase.CalculateScoreUseCase

/** All transitions and validation share a transaction with their writes. */
@Suppress("TooManyFunctions")
internal class VotingStore(private val db: AppDatabase) {
    private val dao = db.voteDao()

    suspend fun begin(roundId: Int): VotingSnapshot = db.withTransaction {
        val round = requireNotNull(db.roundDao().getRound(roundId))
        require(round.status != RoundStatus.FINISHED)
        if (dao.getPhases(roundId).isEmpty()) createPhase(roundId)
        db.roundDao().updateStatus(roundId, RoundStatus.VOTING)
        snapshot(roundId)
    }

    suspend fun snapshot(roundId: Int): VotingSnapshot = db.withTransaction {
        val phase = requireNotNull(dao.getPhases(roundId).lastOrNull())
        val ballots = dao.getPhaseVotes(phase.id).map { Ballot(it.voterGamePlayerId, it.votedGamePlayerId) }
        val leaders = VotingRules.leaders(ballots)
        val roles = roles(roundId)
        val eliminated = eliminated(roundId)
        val status = if (phase.status == "OPEN" && ballots.size == ids(phase.voters).size) {
            if (leaders.size > 1) VotingPhaseStatus.TIE else VotingPhaseStatus.RESULTS
        } else VotingPhaseStatus.valueOf(phase.status)
        VotingSnapshot(
            phase.id, roundId, RoleType.valueOf(phase.target), status,
            ids(phase.voters), ids(phase.candidates), ballots, leaders, phase.pendingHandoff,
            if (status in setOf(VotingPhaseStatus.REVEALED, VotingPhaseStatus.GUESS_PENDING)) roles[phase.selectedPlayerId] else null,
            roles.count { (id, role) -> id !in eliminated && role != RoleType.CIVILIAN },
            db.roundDao().getRound(roundId)?.status == RoundStatus.FINISHED
        )
    }

    suspend fun submit(vote: VoteEntity) = db.withTransaction {
        val state = snapshot(vote.roundId)
        require(state.phaseId == vote.phaseId)
        VotingRules.validate(vote.voterGamePlayerId, vote.votedGamePlayerId, state)
        dao.insert(vote)
        val phase = requireNotNull(dao.getPhase(vote.phaseId))
        dao.updatePhase(phase.copy(pendingHandoff = true))
    }

    suspend fun acknowledge(roundId: Int): VotingSnapshot = db.withTransaction {
        val state = snapshot(roundId)
        require(state.pendingHandoff)
        val phase = requireNotNull(dao.getPhase(state.phaseId))
        dao.updatePhase(phase.copy(pendingHandoff = false, status = state.status.name))
        snapshot(roundId)
    }

    suspend fun reveal(roundId: Int): VotingSnapshot = db.withTransaction {
        val state = snapshot(roundId)
        require(state.status == VotingPhaseStatus.RESULTS && !state.pendingHandoff)
        val phase = requireNotNull(dao.getPhase(state.phaseId))
        val selected = state.leaderIds.single()
        val pendingGuess = roles(roundId)[selected] == RoleType.MR_WHITE
        dao.updatePhase(phase.copy(status = if (pendingGuess) "GUESS_PENDING" else "REVEALED",
            selectedPlayerId = selected))
        if (!pendingGuess) finishIfNecessary(roundId)
        snapshot(roundId)
    }

    suspend fun continuePhase(roundId: Int): VotingSnapshot = db.withTransaction {
        val state = snapshot(roundId)
        require(!state.pendingHandoff && !state.roundFinished)
        when (state.status) {
            VotingPhaseStatus.TIE -> createPhase(roundId, state.leaderIds, state.target)
            VotingPhaseStatus.REVEALED -> createPhase(roundId)
            else -> error("Results must be acknowledged before another election.")
        }
        snapshot(roundId)
    }

    suspend fun finishedResult(roundId: Int): com.impostor.library.domain.model.RoundResult? = db.withTransaction {
        val phases = dao.getPhases(roundId)
        if (phases.isEmpty()) return@withTransaction null
        require(snapshot(roundId).roundFinished) { "Resolve the voting phases before finishing the round." }
        calculation(roundId).result
    }

    suspend fun submitGuess(roundId: Int, playerId: Int, text: String): VotingSnapshot = db.withTransaction {
        val state = snapshot(roundId)
        val phase = requireNotNull(dao.getPhase(state.phaseId))
        require(state.status == VotingPhaseStatus.GUESS_PENDING && !state.roundFinished)
        require(phase.selectedPlayerId == playerId && text.isNotBlank())
        val secret = requireNotNull(db.roundDao().getAssignments(roundId)
            .firstOrNull { it.role.type == RoleType.CIVILIAN }?.content?.text)
        val correct = com.impostor.library.domain.usecase.GuessWordRules.matches(text, secret)
        dao.updatePhase(phase.copy(status = "REVEALED", guessText = text.trim(), guessCorrect = correct))
        finishIfNecessary(roundId)
        snapshot(roundId)
    }

    private suspend fun calculation(roundId: Int): com.impostor.library.domain.model.ScoreCalculation {
        val phases = dao.getPhases(roundId).filter { it.status == "REVEALED" }
        val ballots = phases.flatMap { dao.getPhaseVotes(it.id) }
            .map { Ballot(it.voterGamePlayerId, it.votedGamePlayerId) }
        val winner = phases.firstOrNull { it.guessCorrect == true }?.selectedPlayerId
        return CalculateScoreUseCase().sequential(roundId, roles(roundId), eliminated(roundId), ballots, winner)
    }

    private suspend fun finishIfNecessary(roundId: Int) {
        val active = roles(roundId).filterKeys { it !in eliminated(roundId) }
        val guessed = dao.getPhases(roundId).any { it.guessCorrect == true }
        if (guessed || active.values.none { it != RoleType.CIVILIAN } || active.values.none { it == RoleType.CIVILIAN }) {
            require(db.scoreDao().countRoundScores(roundId) == 0)
            db.scoreDao().insertAll(calculation(roundId).events)
            db.roundDao().finishRound(roundId)
        }
    }

    private suspend fun createPhase(roundId: Int, runoff: List<Int>? = null, target: RoleType? = null) {
        val active = roles(roundId).filterKeys { it !in eliminated(roundId) }
        require(active.size >= 2)
        val nextTarget = target ?: if (RoleType.IMPOSTOR in active.values) RoleType.IMPOSTOR else RoleType.MR_WHITE
        dao.insertPhase(VotingPhaseEntity(
            roundId = roundId, target = nextTarget.name,
            voters = active.keys.joinToString(","), candidates = (runoff ?: active.keys.toList()).joinToString(",")
        ))
    }

    private suspend fun roles(roundId: Int) = db.roundDao().getAssignments(roundId)
        .sortedBy { it.gamePlayer.seatOrder }.associate { it.gamePlayer.id to it.role.type }

    private suspend fun eliminated(roundId: Int) = dao.getPhases(roundId)
        .filter { it.status in setOf("REVEALED", "GUESS_PENDING") }.mapNotNull { it.selectedPlayerId }.toSet()

    private fun ids(value: String) = value.split(',').filter { it.isNotBlank() }.map(String::toInt)
}
