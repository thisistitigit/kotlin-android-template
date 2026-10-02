package com.impostor.library.domain.usecase

import com.impostor.library.data.local.dao.PlayerScore
import com.impostor.library.data.repository.GameRepository

/** Scores accumulate across every round, even when the selected mode changes. */
class GetClassificationUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(gameId: Int): List<PlayerScore> = repository.classification(gameId)
}
