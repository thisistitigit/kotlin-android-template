package com.impostor.app.ui.screens

import com.impostor.library.domain.enums.RoleType

data class PlayerUi(val id: Int, val number: Int, val avatar: TurnPlayerUi)
data class VoteRoleRevealUiState(val player: PlayerUi, val role: RoleType, val roundFinished: Boolean)
enum class VotingDestination { VOTE_INTRO, SELECT_CANDIDATE, VOTE_LOCKED, VOTE_RESULTS, REVEAL_ROLE }

data class VoteResultUiState(
    val selectedPlayer: PlayerUi?,
    val voteCount: Int,
    val voters: List<PlayerUi>,
    val isTie: Boolean,
    val remainingAdversaries: Int,
    val tiedPlayers: List<PlayerUi> = emptyList(),
    val allCounts: Map<Int, Int> = emptyMap(),
    val totalVotes: Int = 0,
    val candidates: List<PlayerUi> = emptyList()
)

data class VotingUiState(
    val destination: VotingDestination = VotingDestination.VOTE_INTRO,
    val currentPlayer: PlayerUi? = null,
    val nextPlayer: PlayerUi? = null,
    val candidates: List<PlayerUi> = emptyList(),
    val selectedId: Int? = null,
    val target: RoleType = RoleType.IMPOSTOR,
    val result: VoteResultUiState? = null,
    val revealedRole: RoleType? = null,
    val roundFinished: Boolean = false,
    val busy: Boolean = false,
    val hasError: Boolean = false
)

sealed interface VotingEvent {
    data object OpenSelection : VotingEvent
    data object Back : VotingEvent
    data class ToggleSelection(val playerId: Int) : VotingEvent
    data object Confirm : VotingEvent
    data object Acknowledge : VotingEvent
    data object Reveal : VotingEvent
    data object Continue : VotingEvent
    data object Retry : VotingEvent
}
