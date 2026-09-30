package com.ncorti.kotlin.template.library.domain

import android.content.Context
import com.ncorti.kotlin.template.library.data.local.AppDatabase
import com.ncorti.kotlin.template.library.data.local.DatabaseFactory
import com.ncorti.kotlin.template.library.data.repository.ContentRepository
import com.ncorti.kotlin.template.library.data.repository.GameRepository
import com.ncorti.kotlin.template.library.data.repository.PlayerRepository
import com.ncorti.kotlin.template.library.domain.usecase.AssignRolesUseCase
import com.ncorti.kotlin.template.library.domain.usecase.CalculateScoreUseCase
import com.ncorti.kotlin.template.library.domain.usecase.CreateGameUseCase
import com.ncorti.kotlin.template.library.domain.usecase.FinishRoundUseCase
import com.ncorti.kotlin.template.library.domain.usecase.FinishGameUseCase
import com.ncorti.kotlin.template.library.domain.usecase.GetClassificationUseCase
import com.ncorti.kotlin.template.library.domain.usecase.StartConfiguredRoundUseCase
import com.ncorti.kotlin.template.library.domain.usecase.StartRoundUseCase
import com.ncorti.kotlin.template.library.domain.usecase.SubmitVoteUseCase
import com.ncorti.kotlin.template.library.domain.usecase.SubmitAnswerUseCase

/** Lightweight composition root. Replace with a DI module when the application grows. */
class GameBackend private constructor(database: AppDatabase) {
    val gameRepository = GameRepository(database)
    val playerRepository = PlayerRepository(database.playerDao())
    val contentRepository = ContentRepository(database.contentDao())

    val createGame = CreateGameUseCase(gameRepository)
    val startRound = StartRoundUseCase(gameRepository)
    val startConfiguredRound = StartConfiguredRoundUseCase(gameRepository, AssignRolesUseCase())
    val submitVote = SubmitVoteUseCase(gameRepository)
    val submitAnswer = SubmitAnswerUseCase(gameRepository)
    val finishRound = FinishRoundUseCase(gameRepository, CalculateScoreUseCase())
    val finishGame = FinishGameUseCase(gameRepository)
    val getClassification = GetClassificationUseCase(gameRepository)

    suspend fun prepare() = gameRepository.prepareDefaults()

    companion object {
        fun create(context: Context): GameBackend = GameBackend(DatabaseFactory.create(context))
    }
}
