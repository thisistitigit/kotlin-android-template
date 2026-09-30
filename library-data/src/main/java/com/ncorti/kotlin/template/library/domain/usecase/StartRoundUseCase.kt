package com.ncorti.kotlin.template.library.domain.usecase

import com.ncorti.kotlin.template.library.data.repository.GameRepository
import com.ncorti.kotlin.template.library.domain.enums.GameModeType

/** Starts the next round; there is deliberately no configured round limit. */
class StartRoundUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(
        gameId: Int,
        mode: GameModeType,
        contentSetId: Int? = null
    ): Long {
        val modeId = requireNotNull(repository.getMode(mode)?.id) {
            "Game modes have not been prepared."
        }
        return repository.startNextRound(gameId, modeId, contentSetId)
    }
}
