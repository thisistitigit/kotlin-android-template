package com.impostor.app.ui.screens

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.impostor.library.domain.GameBackend
import com.impostor.library.domain.enums.GameModeType
import com.impostor.library.domain.enums.QuestionCategory
import com.impostor.library.domain.enums.RoleType
import com.impostor.library.domain.model.PlayerRound
import com.impostor.library.domain.model.VotingPhaseStatus
import com.impostor.library.domain.model.VotingSnapshot
import com.impostor.library.domain.usecase.PlayerRegistration
import com.impostor.library.domain.usecase.RoundContentInput
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class GameFlowSetup(val mode: GameModeType, val impostors: Int, val whites: Int,
    val players: List<TurnPlayerUi>)
enum class GameFlowDestination { CATEGORY_SELECTION, CONTENT_SETUP, TURN, CONTENT_REVEAL, READY, VOTING, MR_WHITE_GUESS, ROUND_CONTENT, FINISHED, SCORES }
data class GameFlowUiState(
    val destination: GameFlowDestination = GameFlowDestination.CONTENT_SETUP,
    val players: List<PlayerUi> = emptyList(),
    val currentPlayer: PlayerUi? = null,
    val viewedPlayers: List<PlayerUi> = emptyList(),
    val content: RevealContent? = null,
    val turnCatRes: Int = TURN_CAT_IMAGES.first(),
    val normalContent: String = "",
    val adversaryContent: String = "",
    val voting: VotingUiState = VotingUiState(),
    val guess: String = "",
    val roundContent: RevealContent? = null,
    val summary: RoundSummaryUiState? = null,
    val classification: List<PlayerScoreUi> = emptyList(),
    val mode: GameModeType = GameModeType.CLASSIC,
    val selectedCategory: QuestionCategory = QuestionCategory.NBA,
    val busy: Boolean = false,
    val hasError: Boolean = false
)

@Suppress("TooManyFunctions")
class GameFlowViewModel(private val backend: GameBackend, private val context: Context,
    private var setup: GameFlowSetup, private var sessionKey: String) : ViewModel() {
    private val mutableState = MutableStateFlow(initialGameState(setup.mode))
    val state = mutableState.asStateFlow()
    private var previousRoundId: Int? = null
    private var gameId: Int? = null
    private var roundId: Int? = null
    private var assignedPlayers: List<PlayerRound> = emptyList()
    private var snapshot: VotingSnapshot? = null
    private var lastOperation: (suspend () -> Unit)? = null

    override fun onCleared() { backend.close() }

    fun configureSession(key: String, configuration: GameFlowSetup) {
        if (key == sessionKey) return
        check(!state.value.busy)
        sessionKey = key
        setup = configuration
        gameId = null
        previousRoundId = null
        roundId = null
        assignedPlayers = emptyList()
        snapshot = null
        lastOperation = null
        mutableState.value = initialGameState(setup.mode)
    }

    fun updateContent(normal: String, adversary: String) {
        mutableState.value = state.value.copy(normalContent = normal, adversaryContent = adversary)
    }

    fun prepare() = operation {
        val input = state.value
        val existing = gameId
        val round = if (existing == null) {
            val registrations = withContext(Dispatchers.IO) {
                setup.players.map { player -> PlayerRegistration(player.name, player.photo?.let(::savePhoto)) }
            }
            val game = backend.prepareGame(registrations, setup.mode, setup.impostors, if (setup.mode == GameModeType.CLASSIC) setup.whites else 0,
                RoundContentInput(input.normalContent, input.adversaryContent,
                    if (setup.mode == GameModeType.QUESTION) input.selectedCategory else null))
            gameId = game.gameId
            assignedPlayers = game.players
            game.roundId
        } else {
            val game = backend.prepareNextRound(existing, requireNotNull(previousRoundId), setup.mode,
                RoundContentInput(input.normalContent, input.adversaryContent,
                    if (setup.mode == GameModeType.QUESTION) input.selectedCategory else null))
            assignedPlayers = game.players
            game.roundId
        }
        roundId = round
        val players = assignedPlayers.mapIndexed { seat, player ->
            PlayerUi(player.gamePlayerId, seat + 1, setup.players[seat])
        }
        val secret = requireNotNull(assignedPlayers.first { it.role == RoleType.CIVILIAN }.secretContent)
        mutableState.value = GameFlowUiState(destination = GameFlowDestination.TURN, mode = setup.mode,
            players = players, currentPlayer = players.first(), turnCatRes = TURN_CAT_IMAGES.random(),
            roundContent = if (setup.mode == GameModeType.QUESTION) RevealContent.Question(secret) else RevealContent.Word(secret))
    }

    fun setMode(mode: GameModeType) {
        if (state.value.busy || state.value.destination != GameFlowDestination.CONTENT_SETUP) return
        if (setup.mode == mode) return
        setup = setup.copy(mode = mode)
        mutableState.value = state.value.copy(mode = mode, destination =
            if (mode == GameModeType.QUESTION) GameFlowDestination.CATEGORY_SELECTION else GameFlowDestination.CONTENT_SETUP)
    }

    fun selectCategory(category: QuestionCategory) {
        if (!state.value.busy && state.value.destination == GameFlowDestination.CATEGORY_SELECTION)
            mutableState.value = state.value.copy(selectedCategory = category)
    }

    fun confirmCategory() {
        if (!state.value.busy && state.value.destination == GameFlowDestination.CATEGORY_SELECTION)
            mutableState.value = state.value.copy(destination = GameFlowDestination.CONTENT_SETUP)
    }

    fun editCategory() {
        if (!state.value.busy && setup.mode == GameModeType.QUESTION)
            mutableState.value = state.value.copy(destination = GameFlowDestination.CATEGORY_SELECTION)
    }

    fun updateGuess(text: String) {
        if (!state.value.busy) mutableState.value = state.value.copy(guess = text)
    }

    fun submitGuess() = operation {
        val phase = requireNotNull(snapshot)
        val persisted = backend.voting.get(phase.roundId)
        val next = if (persisted.status == VotingPhaseStatus.GUESS_PENDING)
            backend.submitMrWhiteGuess(phase.roundId, phase.leaderIds.single(), state.value.guess) else persisted
        applySnapshot(next)
        continueAfterReveal()
    }

    private suspend fun continueAfterReveal() {
        val phase = requireNotNull(snapshot)
        when {
            phase.status == VotingPhaseStatus.GUESS_PENDING -> mutableState.value = state.value.copy(
                destination = GameFlowDestination.MR_WHITE_GUESS, guess = "")
            phase.roundFinished -> {
                val result = backend.finishRound(phase.roundId)
                val rows = state.value.players.map { PlayerScoreUi(it, result.scores[it.id] ?: 0) }
                mutableState.value = state.value.copy(destination = GameFlowDestination.ROUND_CONTENT,
                    summary = RoundSummaryUiState(result.winningRoles, rows))
            }
            else -> applySnapshot(backend.voting.continuePhase(phase.roundId))
        }
    }

    fun showSummary() {
        mutableState.value = state.value.copy(destination = GameFlowDestination.FINISHED)
    }

    fun showScores() = operation {
        val players = state.value.players.associateBy { it.id }
        val rows = backend.getClassification(requireNotNull(gameId)).map {
            PlayerScoreUi(requireNotNull(players[it.gamePlayerId]), it.totalScore)
        }
        mutableState.value = state.value.copy(destination = GameFlowDestination.SCORES, classification = rows)
    }

    fun playAgain() {
        if (state.value.busy) return
        snapshot = null
        previousRoundId = roundId
        roundId = null
        assignedPlayers = emptyList()
        mutableState.value = initialGameState(setup.mode)
    }

    fun showContent() {
        val player = requireNotNull(state.value.currentPlayer)
        val assignment = assignedPlayers.single { it.gamePlayerId == player.id }
        val content = when {
            assignment.role == RoleType.MR_WHITE -> RevealContent.MrWhite
            setup.mode == GameModeType.QUESTION -> RevealContent.Question(requireNotNull(assignment.secretContent))
            else -> RevealContent.Word(requireNotNull(assignment.secretContent))
        }
        mutableState.value = state.value.copy(destination = GameFlowDestination.CONTENT_REVEAL, content = content)
    }

    fun hideContent() {
        mutableState.value = state.value.copy(destination = GameFlowDestination.TURN, content = null)
    }

    fun finishReveal() {
        val viewed = state.value.viewedPlayers + requireNotNull(state.value.currentPlayer)
        val next = state.value.players.firstOrNull { player -> viewed.none { it.id == player.id } }
        val destination = if (next == null) GameFlowDestination.READY else GameFlowDestination.TURN
        mutableState.value = state.value.copy(destination = destination,
            currentPlayer = next, viewedPlayers = viewed, content = null, turnCatRes = TURN_CAT_IMAGES.random())
    }

    fun readyBack() {
        snapshot?.let { applySnapshot(it); return }
        val last = state.value.players.last()
        mutableState.value = state.value.copy(destination = GameFlowDestination.TURN, currentPlayer = last,
            viewedPlayers = state.value.viewedPlayers.filterNot { it.id == last.id })
    }

    fun beginVoting() = operation { applySnapshot(backend.voting.begin(requireNotNull(roundId))) }

    fun vote(event: VotingEvent) {
        if (state.value.busy) return
        when (event) {
            VotingEvent.OpenSelection -> if (state.value.voting.destination == VotingDestination.VOTE_INTRO) {
                updateVoting { it.copy(destination = VotingDestination.SELECT_CANDIDATE) }
            }
            is VotingEvent.ToggleSelection -> toggleSelection(event.playerId)
            VotingEvent.Confirm -> confirmVote()
            VotingEvent.Acknowledge -> operation { applySnapshot(backend.voting.acknowledge(requireNotNull(roundId))) }
            VotingEvent.Reveal -> operation { applySnapshot(backend.voting.reveal(requireNotNull(roundId))) }
            VotingEvent.Continue -> operation { continueAfterReveal() }
            VotingEvent.Retry -> retry()
            VotingEvent.Back -> votingBack()
        }
    }

    private fun toggleSelection(playerId: Int) {
        val voting = state.value.voting
        if (voting.destination == VotingDestination.SELECT_CANDIDATE && voting.candidates.any { it.id == playerId }) {
            updateVoting {
                it.copy(selectedId = if (it.selectedId == playerId) null else playerId)
            }
        }
    }

    private fun confirmVote() {
        val voting = state.value.voting
        if (voting.destination != VotingDestination.SELECT_CANDIDATE || voting.selectedId == null) return
        operation {
            val phase = requireNotNull(snapshot)
            val targetId = requireNotNull(voting.selectedId)
            backend.submitVote(phase.roundId, phase.phaseId, requireNotNull(voting.currentPlayer).id, targetId)
            applySnapshot(backend.voting.get(phase.roundId))
        }
    }

    private fun votingBack() {
        when (state.value.voting.destination) {
            VotingDestination.SELECT_CANDIDATE -> updateVoting {
                it.copy(destination = VotingDestination.VOTE_INTRO, selectedId = null)
            }
            VotingDestination.VOTE_LOCKED -> vote(VotingEvent.Acknowledge)
            VotingDestination.REVEAL_ROLE -> vote(VotingEvent.Continue)
            else -> mutableState.value = state.value.copy(destination = GameFlowDestination.READY)
        }
    }

    fun retry() { lastOperation?.let(::operation) }

    private fun applySnapshot(value: VotingSnapshot) {
        snapshot = value
        val players = state.value.players.associateBy { it.id }
        val destination = when {
            value.pendingHandoff -> VotingDestination.VOTE_LOCKED
            value.status in setOf(VotingPhaseStatus.REVEALED, VotingPhaseStatus.GUESS_PENDING) -> VotingDestination.REVEAL_ROLE
            value.status in setOf(VotingPhaseStatus.RESULTS, VotingPhaseStatus.TIE) -> VotingDestination.VOTE_RESULTS
            else -> VotingDestination.VOTE_INTRO
        }
        val leader = value.leaderIds.singleOrNull()
        val current = if (value.pendingHandoff) value.ballots.lastOrNull()?.voterId else value.nextVoterId
        val leadingVotes = value.ballots.count { it.candidateId == value.leaderIds.firstOrNull() }
        val total = value.ballots.size
        val counts = value.ballots.groupingBy { it.candidateId }.eachCount()
        
        val results = if (!value.pendingHandoff && value.status != VotingPhaseStatus.OPEN) VoteResultUiState(
            selectedPlayer = players[leader],
            voteCount = leadingVotes,
            voters = value.ballots.filter { it.candidateId == leader }.map { requireNotNull(players[it.voterId]) },
            isTie = value.status == VotingPhaseStatus.TIE,
            remainingAdversaries = value.remainingAdversaries,
            tiedPlayers = value.leaderIds.map { requireNotNull(players[it]) },
            allCounts = counts,
            totalVotes = total,
            candidates = value.candidateIds.map { requireNotNull(players[it]) }
        ) else null

        val voting = VotingUiState(destination = destination, currentPlayer = players[current],
            nextPlayer = players[value.nextVoterId],
            candidates = value.candidateIds.filter { it != current }.map { requireNotNull(players[it]) },
            target = value.target, result = results, revealedRole = value.revealedRole,
            roundFinished = value.roundFinished)
        mutableState.value = state.value.copy(destination = GameFlowDestination.VOTING, voting = voting, content = null)
    }

    private fun updateVoting(block: (VotingUiState) -> VotingUiState) {
        mutableState.value = state.value.copy(voting = block(state.value.voting))
    }

    @Suppress("TooGenericExceptionCaught") // UI boundary reports persistence and validation failures with retry.
    private fun operation(block: suspend () -> Unit) {
        if (state.value.busy) return
        lastOperation = block
        mutableState.value = state.value.copy(busy = true, hasError = false)
        updateVoting { it.copy(busy = true, hasError = false) }
        viewModelScope.launch {
            try { block() } catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) {
                android.util.Log.e("Impostor", "Game transition failed", failure)
                mutableState.value = state.value.copy(hasError = true)
            }
            finally {
                mutableState.value = state.value.copy(busy = false)
                updateVoting { it.copy(busy = false, hasError = state.value.hasError) }
            }
        }
    }

    private fun savePhoto(bitmap: Bitmap): String {
        val directory = File(context.filesDir, "player-photos").apply { mkdirs() }
        val photo = File(directory, "${UUID.randomUUID()}.png")
        photo.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, it)) }
        return photo.toURI().toString()
    }
}

private const val PNG_QUALITY = 100


private fun initialGameState(mode: GameModeType) = GameFlowUiState(mode = mode,
    destination = if (mode == GameModeType.QUESTION) GameFlowDestination.CATEGORY_SELECTION else GameFlowDestination.CONTENT_SETUP)
