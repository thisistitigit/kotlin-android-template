package com.ncorti.kotlin.template.library.domain.usecase

import com.ncorti.kotlin.template.library.data.repository.GameRepository

class CreateGameUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(
        playerIds: List<Int>,
        impostorCount: Int,
        mrWhiteCount: Int = 0
    ): Long {
        require(playerIds.size >= MINIMUM_PLAYERS) { "The game requires at least 3 players." }
        require(playerIds.distinct().size == playerIds.size) { "Players cannot be duplicated." }
        val adversaryCount = impostorCount + mrWhiteCount
        require(mrWhiteCount >= 0) { "The Mr. White count cannot be negative." }
        require(impostorCount > 0 && adversaryCount < playerIds.size) {
            "At least one civilian and one impostor are required."
        }
        return repository.createGame(playerIds, impostorCount, mrWhiteCount)
    }

    private companion object {
        const val MINIMUM_PLAYERS = 3
    }
}
