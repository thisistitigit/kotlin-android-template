package com.impostor.library.domain

import android.content.Context
import com.impostor.library.data.local.AppDatabase
import com.impostor.library.data.local.DatabaseFactory
import com.impostor.library.data.repository.ContentRepository
import com.impostor.library.data.repository.GameRepository
import com.impostor.library.data.repository.PlayerRepository
import com.impostor.library.domain.usecase.AssignRolesUseCase
import com.impostor.library.domain.usecase.CalculateScoreUseCase
import com.impostor.library.domain.usecase.CreateGameUseCase
import com.impostor.library.domain.usecase.FinishRoundUseCase
import com.impostor.library.domain.usecase.FinishGameUseCase
import com.impostor.library.domain.usecase.GetClassificationUseCase
import com.impostor.library.domain.usecase.StartConfiguredRoundUseCase
import com.impostor.library.domain.usecase.StartRoundUseCase
import com.impostor.library.domain.usecase.SubmitVoteUseCase
import com.impostor.library.domain.usecase.SubmitAnswerUseCase

/** Lightweight composition root. Replace with a DI module when the application grows. */
class GameBackend private constructor(private val database: AppDatabase) {
    val gameRepository = GameRepository(database)
    val playerRepository = PlayerRepository(database.playerDao())
    val contentRepository = ContentRepository(database.contentDao())

    val createGame = CreateGameUseCase(gameRepository)
    val startRound = StartRoundUseCase(gameRepository)
    val startConfiguredRound = StartConfiguredRoundUseCase(gameRepository, AssignRolesUseCase())
    val prepareGame = com.impostor.library.domain.usecase.PrepareGameUseCase(
        gameRepository, playerRepository, contentRepository, createGame, startConfiguredRound
    )
    val prepareNextRound = com.impostor.library.domain.usecase.PrepareNextRoundUseCase(
        gameRepository, contentRepository, startConfiguredRound
    )
    val submitMrWhiteGuess = com.impostor.library.domain.usecase.SubmitMrWhiteGuessUseCase(gameRepository)
    val submitVote = SubmitVoteUseCase(gameRepository)
    val voting = com.impostor.library.domain.usecase.VotingUseCase(gameRepository)
    val submitAnswer = SubmitAnswerUseCase(gameRepository)
    val finishRound = FinishRoundUseCase(gameRepository, CalculateScoreUseCase())
    val finishGame = FinishGameUseCase(gameRepository)
    val getClassification = GetClassificationUseCase(gameRepository)

    suspend fun prepare() = gameRepository.prepareDefaults()
    fun close() = database.close()

    companion object {
        fun create(context: Context): GameBackend = GameBackend(DatabaseFactory.create(context))
    }
}
