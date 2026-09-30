package com.ncorti.kotlin.template.library.domain.usecase

import com.ncorti.kotlin.template.library.data.repository.GameRepository

class CreateGameUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(
        playerIds: List<Int>,
        impostorCount: Int,
        includeMrWhite: Boolean = false
    ): Long {
        require(playerIds.size >= MINIMUM_PLAYERS) { "The game requires at least 3 players." }
        require(playerIds.distinct().size == playerIds.size) { "Players cannot be duplicated." }
        val adversaryCount = impostorCount + if (includeMrWhite) 1 else 0
        require(impostorCount > 0 && adversaryCount < playerIds.size) {
            "At least one civilian and one impostor are required."
        }
        return repository.createGame(playerIds, impostorCount, includeMrWhite)
    }

    private companion object {
        const val MINIMUM_PLAYERS = 3
    }
}
