package com.impostor.library.domain.usecase

import com.impostor.library.data.local.entity.AnswerEntity
import com.impostor.library.data.repository.GameRepository

class SubmitAnswerUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(answer: AnswerEntity) = repository.submitAnswer(answer)
}
