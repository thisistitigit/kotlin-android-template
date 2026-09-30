package com.ncorti.kotlin.template.library.domain.usecase

import com.ncorti.kotlin.template.library.data.local.entity.AnswerEntity
import com.ncorti.kotlin.template.library.data.repository.GameRepository

class SubmitAnswerUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(answer: AnswerEntity) = repository.submitAnswer(answer)
}
