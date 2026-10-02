package com.impostor.library.domain.usecase

import com.impostor.library.data.repository.GameRepository

class FinishGameUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(gameId: Int) = repository.finishGame(gameId)
}
