package com.impostor.library.data.repository

import androidx.room.withTransaction
import com.impostor.library.data.local.AppDatabase
import com.impostor.library.data.local.dao.PlayerScore
import com.impostor.library.data.local.entity.AnswerEntity
import com.impostor.library.data.local.entity.GameEntity
import com.impostor.library.data.local.entity.GameModeEntity
import com.impostor.library.data.local.entity.GamePlayerEntity
import com.impostor.library.data.local.entity.RoleEntity
import com.impostor.library.data.local.entity.RoundAssignmentEntity
import com.impostor.library.data.local.entity.RoundEntity
import com.impostor.library.data.local.entity.ScoreEventEntity
import com.impostor.library.data.local.entity.VoteEntity
import com.impostor.library.data.local.relation.GameWithPlayers
import com.impostor.library.data.local.relation.RoundAssignmentDetails
import com.impostor.library.domain.enums.GameModeType
import com.impostor.library.domain.enums.GameStatus
import com.impostor.library.domain.enums.RoleType
import com.impostor.library.domain.enums.RoundStatus

@Suppress("TooManyFunctions")
class GameRepository(private val database: AppDatabase) {
    private val voting = VotingStore(database)

    suspend fun beginVoting(roundId: Int) = voting.begin(roundId)
    suspend fun votingSnapshot(roundId: Int) = voting.snapshot(roundId)
    suspend fun acknowledgeVote(roundId: Int) = voting.acknowledge(roundId)
    suspend fun revealVotedRole(roundId: Int) = voting.reveal(roundId)
    suspend fun continueVoting(roundId: Int) = voting.continuePhase(roundId)
    suspend fun getVotingResult(roundId: Int) = voting.finishedResult(roundId)
    suspend fun submitMrWhiteGuess(roundId: Int, playerId: Int, text: String) = voting.submitGuess(roundId, playerId, text)
    suspend fun <T> transaction(block: suspend () -> T): T = database.withTransaction { block() }
    private val gameDao = database.gameDao()
    private val roundDao = database.roundDao()
    private val voteDao = database.voteDao()
    private val answerDao = database.answerDao()
    private val scoreDao = database.scoreDao()

    suspend fun prepareDefaults() = database.withTransaction {
        gameDao.insertModes(
            listOf(
                GameModeEntity(type = GameModeType.CLASSIC, description = "Classic"),
                GameModeEntity(type = GameModeType.SIMILAR_WORD, description = "Similar words"),
                GameModeEntity(type = GameModeType.QUESTION, description = "Questions")
            )
        )
        roundDao.insertRoles(
            listOf(
                RoleEntity(name = "Civilian", type = RoleType.CIVILIAN),
                RoleEntity(name = "Impostor", type = RoleType.IMPOSTOR),
                RoleEntity(name = "Mr. White", type = RoleType.MR_WHITE)
            )
        )
    }

    suspend fun createGame(
        playerIds: List<Int>,
        impostorCount: Int,
        mrWhiteCount: Int
    ): Long = database.withTransaction {
        val gameId = gameDao.insertGame(
            GameEntity(
                playerCount = playerIds.size,
                impostorCount = impostorCount,
                mrWhiteCount = mrWhiteCount
            )
        ).toInt()
        gameDao.insertGamePlayers(
            playerIds.mapIndexed { index, playerId ->
                GamePlayerEntity(gameId = gameId, playerId = playerId, seatOrder = index)
            }
        )
        gameId.toLong()
    }

    suspend fun getGame(gameId: Int): GameWithPlayers? = gameDao.getGameWithPlayers(gameId)

    suspend fun getMode(type: GameModeType): GameModeEntity? = gameDao.getMode(type)

    suspend fun getRole(type: RoleType): RoleEntity? = roundDao.getRole(type)

    suspend fun startNextRound(
        gameId: Int,
        modeId: Int,
        contentSetId: Int? = null
    ): Long = database.withTransaction {
        val game = requireNotNull(gameDao.getGame(gameId)) { "Game not found." }
        require(game.status != GameStatus.FINISHED && game.status != GameStatus.CANCELLED) {
            "A finished or cancelled game cannot start another round."
        }
        val roundNumber = roundDao.getNextRoundNumber(gameId)
        val roundId = roundDao.insertRound(
            RoundEntity(
                gameId = gameId,
                gameModeId = modeId,
                contentSetId = contentSetId,
                roundNumber = roundNumber
            )
        )
        gameDao.updateStatus(gameId, GameStatus.IN_PROGRESS)
        roundId
    }

    suspend fun saveAssignments(roundId: Int, assignments: List<RoundAssignmentEntity>) =
        database.withTransaction {
            require(roundDao.getRound(roundId)?.status == RoundStatus.CREATED) {
                "Roles can only be assigned to a newly created round."
            }
            roundDao.insertAssignments(assignments)
            roundDao.updateStatus(roundId, RoundStatus.REVEALING_ROLES)
        }

    suspend fun isLatestFinishedRound(gameId: Int, roundId: Int): Boolean {
        val round = roundDao.getRound(roundId)
        return round?.gameId == gameId && round.status == RoundStatus.FINISHED &&
            roundDao.getNextRoundNumber(gameId) == round.roundNumber + 1
    }

    suspend fun getAssignments(roundId: Int): List<RoundAssignmentDetails> =
        roundDao.getAssignments(roundId)

    suspend fun getVotes(roundId: Int): List<VoteEntity> = voteDao.getVotes(roundId)

    suspend fun submitVote(vote: VoteEntity) = database.withTransaction {
        voting.submit(vote)
    }

    suspend fun submitAnswer(answer: AnswerEntity) = database.withTransaction {
        require(roundDao.isPlayerInRound(answer.roundId, answer.gamePlayerId)) { "Invalid player." }
        require(answer.answerText.isNotBlank()) { "The answer cannot be blank." }
        answerDao.insert(answer.copy(answerText = answer.answerText.trim()))
    }

    suspend fun finishRound(roundId: Int, events: List<ScoreEventEntity>) = database.withTransaction {
        require(scoreDao.countRoundScores(roundId) == 0) { "This round has already been scored." }
        scoreDao.insertAll(events)
        roundDao.finishRound(roundId)
    }

    suspend fun classification(gameId: Int): List<PlayerScore> = scoreDao.getGameClassification(gameId)

    suspend fun finishGame(gameId: Int) = gameDao.finishGame(gameId)
}
