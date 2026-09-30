package com.ncorti.kotlin.template.library.domain.usecase

import com.ncorti.kotlin.template.library.data.repository.GameRepository

class FinishGameUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(gameId: Int) = repository.finishGame(gameId)
}
