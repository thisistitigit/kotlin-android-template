package com.impostor.library.domain.usecase

import com.impostor.library.data.repository.ContentRepository
import com.impostor.library.data.repository.GameRepository
import com.impostor.library.domain.enums.GameModeType
import com.impostor.library.domain.model.PlayerRound

/** Keeps player identity and accumulated scores while changing content or mode. */
class PrepareNextRoundUseCase(
    private val repository: GameRepository,
    private val contentRepository: ContentRepository,
    private val startConfiguredRound: StartConfiguredRoundUseCase
) {
    suspend operator fun invoke(gameId: Int, previousRoundId: Int, mode: GameModeType,
        input: RoundContentInput): PreparedGame = repository.transaction {
        require(repository.isLatestFinishedRound(gameId, previousRoundId)) { "Finish the previous round first." }
        val content = contentRepository.saveCustomContent(mode, input.normal, input.adversary, input.category)
        val roundId = startConfiguredRound(gameId, mode, content.first, content.second).toInt()
        val players = requireNotNull(repository.getGame(gameId)).players.associateBy { it.gamePlayer.id }
        val assignments = repository.getAssignments(roundId).sortedBy { it.gamePlayer.seatOrder }
        PreparedGame(gameId, roundId, assignments.map {
            val player = requireNotNull(players[it.gamePlayer.id]).player
            PlayerRound(it.gamePlayer.id, player.name, player.photoUri, it.role.type, it.content?.text)
        })
    }
}
