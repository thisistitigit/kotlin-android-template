package com.impostor.library.domain.usecase

import com.impostor.library.data.local.entity.ScoreEventEntity
import com.impostor.library.domain.enums.RoleType
import com.impostor.library.domain.enums.ScoreReason
import com.impostor.library.domain.model.RoundResult
import com.impostor.library.domain.model.ScoreCalculation

class CalculateScoreUseCase {
    @Suppress("LongParameterList")
    operator fun invoke(
        roundId: Int,
        playerIds: Set<Int>,
        impostorIds: Set<Int>,
        mrWhiteIds: Set<Int>,
        votes: Map<Int, Int>,
        mrWhiteGuesserId: Int? = null
    ): ScoreCalculation {
        val roles = RoundRoles(playerIds, impostorIds, mrWhiteIds)
        validate(roles, votes, mrWhiteGuesserId)
        val eliminated = mostVoted(votes)
        val mrWhiteWinnerId = mrWhiteGuesserId?.takeIf { it in eliminated }
        val adversaryDiscovered = eliminated.any { it in impostorIds || it in mrWhiteIds }
        val winners = when {
            mrWhiteWinnerId != null -> setOf(RoleType.MR_WHITE)
            adversaryDiscovered -> setOf(RoleType.CIVILIAN)
            else -> buildSet {
                add(RoleType.IMPOSTOR)
                if (mrWhiteIds.isNotEmpty()) add(RoleType.MR_WHITE)
            }
        }
        val events = voteEvents(roundId, votes, roles).toMutableList()
        events += winnerEvents(roundId, roles, mrWhiteWinnerId, winners)
        val scores = events.groupBy { it.gamePlayerId }.mapValues { (_, values) -> values.sumOf { it.points } }
        return ScoreCalculation(
            result = RoundResult(roundId, eliminated, impostorIds, mrWhiteIds, mrWhiteWinnerId, winners, scores),
            events = events
        )
    }

    private fun validate(
        roles: RoundRoles,
        votes: Map<Int, Int>,
        mrWhiteGuesserId: Int?
    ) {
        require(roles.impostorIds.isNotEmpty() && roles.impostorIds.all { it in roles.playerIds })
        require(roles.mrWhiteIds.all { it in roles.playerIds })
        require(roles.mrWhiteIds.intersect(roles.impostorIds).isEmpty())
        require(mrWhiteGuesserId == null || mrWhiteGuesserId in roles.mrWhiteIds)
        require(votes.keys.all { it in roles.playerIds } && votes.values.all { it in roles.playerIds })
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
        roles: RoundRoles
    ): List<ScoreEventEntity> = votes.mapNotNull { (voter, target) ->
        when {
            target in roles.impostorIds -> scoreEvent(
                roundId,
                voter,
                ScoreReason.CORRECT_IMPOSTOR_VOTE,
                CORRECT_VOTE_POINTS
            )
            target in roles.mrWhiteIds -> scoreEvent(
                roundId,
                voter,
                ScoreReason.CORRECT_MR_WHITE_VOTE,
                CORRECT_VOTE_POINTS
            )
            else -> null
        }
    }

    private fun winnerEvents(
        roundId: Int,
        roles: RoundRoles,
        mrWhiteWinnerId: Int?,
        winners: Set<RoleType>
    ): List<ScoreEventEntity> {
        val winningPlayers = when {
            mrWhiteWinnerId != null -> setOf(mrWhiteWinnerId)
            RoleType.CIVILIAN in winners -> roles.playerIds - roles.impostorIds - roles.mrWhiteIds
            else -> roles.impostorIds + roles.mrWhiteIds
        }
        return winningPlayers.map { playerId ->
            val reason = when {
                playerId in roles.mrWhiteIds -> ScoreReason.MR_WHITE_WIN
                playerId in roles.impostorIds -> ScoreReason.IMPOSTOR_WIN
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

    private data class RoundRoles(
        val playerIds: Set<Int>,
        val impostorIds: Set<Int>,
        val mrWhiteIds: Set<Int>
    )
}
