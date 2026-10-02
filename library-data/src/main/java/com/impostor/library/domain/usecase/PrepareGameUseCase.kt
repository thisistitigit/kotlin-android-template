package com.impostor.library.domain.usecase

import com.impostor.library.data.repository.ContentRepository
import com.impostor.library.data.repository.GameRepository
import com.impostor.library.data.repository.PlayerRepository
import com.impostor.library.domain.enums.GameModeType
import com.impostor.library.domain.model.PlayerRound

data class PreparedGame(val gameId: Int, val roundId: Int, val players: List<PlayerRound>)
data class PlayerRegistration(val name: String, val photoUri: String?)
data class RoundContentInput(val normal: String, val adversary: String,
    val category: com.impostor.library.domain.enums.QuestionCategory? = null)

/** User-supplied content, player creation and role assignment commit together. */
class PrepareGameUseCase(
    private val gameRepository: GameRepository,
    private val playerRepository: PlayerRepository,
    private val contentRepository: ContentRepository,
    private val createGame: CreateGameUseCase,
    private val startConfiguredRound: StartConfiguredRoundUseCase
) {
    suspend operator fun invoke(
        players: List<PlayerRegistration>, mode: GameModeType, impostors: Int, whites: Int,
        contentInput: RoundContentInput
    ): PreparedGame = gameRepository.transaction {
        require(mode == GameModeType.CLASSIC || whites == 0)
        val content = contentRepository.saveCustomContent(mode, contentInput.normal, contentInput.adversary, contentInput.category)
        val ids = players.map { playerRepository.createPlayer(it.name, it.photoUri).toInt() }
        val gameId = createGame(ids, impostors, whites).toInt()
        val roundId = startConfiguredRound(gameId, mode, content.first, content.second).toInt()
        val gamePlayers = requireNotNull(gameRepository.getGame(gameId)).players
        val assignments = gameRepository.getAssignments(roundId).associateBy { it.gamePlayer.id }
        PreparedGame(gameId, roundId, gamePlayers.sortedBy { it.gamePlayer.seatOrder }.map {
            val assignment = requireNotNull(assignments[it.gamePlayer.id])
            PlayerRound(it.gamePlayer.id, it.player.name, it.player.photoUri,
                assignment.role.type, assignment.content?.text)
        })
    }
}
