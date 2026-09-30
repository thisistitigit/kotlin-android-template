package com.ncorti.kotlin.template.library.domain.usecase

import com.ncorti.kotlin.template.library.data.local.entity.ScoreEventEntity
import com.ncorti.kotlin.template.library.domain.enums.RoleType
import com.ncorti.kotlin.template.library.domain.enums.ScoreReason
import com.ncorti.kotlin.template.library.domain.model.RoundResult
import com.ncorti.kotlin.template.library.domain.model.ScoreCalculation

class CalculateScoreUseCase {
    @Suppress("LongParameterList")
    operator fun invoke(
        roundId: Int,
        playerIds: Set<Int>,
        impostorIds: Set<Int>,
        mrWhiteId: Int?,
        votes: Map<Int, Int>,
        mrWhiteGuessedSecret: Boolean = false
    ): ScoreCalculation {
        validate(playerIds, impostorIds, mrWhiteId, votes)
        val eliminated = mostVoted(votes)
        val mrWhiteWins = mrWhiteId in eliminated && mrWhiteGuessedSecret
        val adversaryDiscovered = eliminated.any { it in impostorIds || it == mrWhiteId }
        val winners = when {
            mrWhiteWins -> setOf(RoleType.MR_WHITE)
            adversaryDiscovered -> setOf(RoleType.CIVILIAN)
            else -> buildSet {
                add(RoleType.IMPOSTOR)
                if (mrWhiteId != null) add(RoleType.MR_WHITE)
            }
        }
        val events = voteEvents(roundId, votes, impostorIds, mrWhiteId).toMutableList()
        events += winnerEvents(roundId, playerIds, impostorIds, mrWhiteId, winners)
        val scores = events.groupBy { it.gamePlayerId }.mapValues { (_, values) -> values.sumOf { it.points } }
        return ScoreCalculation(
            result = RoundResult(roundId, eliminated, impostorIds, mrWhiteId, winners, scores),
            events = events
        )
    }

    private fun validate(
        playerIds: Set<Int>,
        impostorIds: Set<Int>,
        mrWhiteId: Int?,
        votes: Map<Int, Int>
    ) {
        require(impostorIds.isNotEmpty() && impostorIds.all { it in playerIds })
        require(mrWhiteId == null || mrWhiteId in playerIds && mrWhiteId !in impostorIds)
        require(votes.keys.all { it in playerIds } && votes.values.all { it in playerIds })
        require(votes.none { (voter, target) -> voter == target })
    }

    private fun mostVoted(votes: Map<Int, Int>): Set<Int> {
        val counts = votes.values.groupingBy { it }.eachCount()
        val maximum = counts.values.maxOrNull() ?: return emptySet()
        return counts.filterValues { it == maximum }.keys
    }

    private fun voteEvents(
        roundId: Int,
        votes: Map<Int, Int>,
        impostorIds: Set<Int>,
        mrWhiteId: Int?
    ): List<ScoreEventEntity> = votes.mapNotNull { (voter, target) ->
        when {
            target in impostorIds -> scoreEvent(roundId, voter, ScoreReason.CORRECT_IMPOSTOR_VOTE, CORRECT_VOTE_POINTS)
            target == mrWhiteId -> scoreEvent(roundId, voter, ScoreReason.CORRECT_MR_WHITE_VOTE, CORRECT_VOTE_POINTS)
            else -> null
        }
    }

    private fun winnerEvents(
        roundId: Int,
        playerIds: Set<Int>,
        impostorIds: Set<Int>,
        mrWhiteId: Int?,
        winners: Set<RoleType>
    ): List<ScoreEventEntity> {
        val winningPlayers = when {
            RoleType.MR_WHITE in winners && winners.size == 1 -> setOfNotNull(mrWhiteId)
            RoleType.CIVILIAN in winners -> playerIds - impostorIds - setOfNotNull(mrWhiteId)
            else -> impostorIds + setOfNotNull(mrWhiteId)
        }
        return winningPlayers.map { playerId ->
            val reason = when {
                playerId == mrWhiteId -> ScoreReason.MR_WHITE_WIN
                playerId in impostorIds -> ScoreReason.IMPOSTOR_WIN
                else -> ScoreReason.CIVILIAN_WIN
            }
            val points = if (reason == ScoreReason.MR_WHITE_WIN && winners.size == 1) {
                MR_WHITE_GUESS_POINTS
            } else {
                WIN_POINTS
            }
            scoreEvent(roundId, playerId, reason, points)
        }
    }

    private fun scoreEvent(roundId: Int, playerId: Int, reason: ScoreReason, points: Int) =
        ScoreEventEntity(roundId = roundId, gamePlayerId = playerId, points = points, reason = reason)

    private companion object {
        const val CORRECT_VOTE_POINTS = 2
        const val WIN_POINTS = 3
        const val MR_WHITE_GUESS_POINTS = 7
    }
}
