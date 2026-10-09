package com.impostor.app.ui.screens

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.impostor.library.domain.GameBackend
import com.impostor.library.domain.enums.GameModeType
import com.impostor.library.domain.enums.QuestionCategory
import com.impostor.library.domain.enums.RoleType
import com.impostor.library.domain.enums.RoundStatus
import com.impostor.library.domain.model.PlayerRound
import com.impostor.library.domain.model.VotingPhaseStatus
import com.impostor.library.domain.model.VotingSnapshot
import com.impostor.library.domain.usecase.PlayerRegistration
import com.impostor.library.domain.usecase.RoundContentInput
import com.impostor.library.data.local.entity.AnswerEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class GameFlowSetup(val mode: GameModeType, val impostors: Int, val whites: Int,
    val players: List<TurnPlayerUi>)
enum class GameFlowDestination { CATEGORY_SELECTION, PREPARING, TURN, CONTENT_REVEAL, READY,
    ANSWER_TURN, ANSWER_INPUT, ANSWER_RESULTS, VOTING, MR_WHITE_GUESS, ROUND_CONTENT, FINISHED, SCORES }
data class PlayerAnswerUi(val player: PlayerUi, val text: String)
data class GameFlowUiState(
    val destination: GameFlowDestination = GameFlowDestination.PREPARING,
    val players: List<PlayerUi> = emptyList(),
    val currentPlayer: PlayerUi? = null,
    val viewedPlayers: List<PlayerUi> = emptyList(),
    val content: RevealContent? = null,
    val turnCatRes: Int = TURN_CAT_IMAGES.first(),
    val voting: VotingUiState = VotingUiState(),
    val guess: String = "",
    val answerText: String = "",
    val answerPrompt: String? = null,
    val answeredPlayers: List<PlayerUi> = emptyList(),
    val answers: List<PlayerAnswerUi> = emptyList(),
    val roundContent: RevealContent? = null,
    val summary: RoundSummaryUiState? = null,
    val classification: List<PlayerScoreUi> = emptyList(),
    val mode: GameModeType = GameModeType.CLASSIC,
    val selectedCategory: QuestionCategory = QuestionCategory.NBA,
    val selectingNextRound: Boolean = false,
    val busy: Boolean = false,
    val hasError: Boolean = false
)

@Suppress("TooManyFunctions")
class GameFlowViewModel(private val backend: GameBackend, private val context: Context,
    private var setup: GameFlowSetup, private var sessionKey: String) : ViewModel() {
    private val sessionStore = GameSessionStore(context)
    private val resumeCheckpoint = sessionStore.readCheckpoint(sessionKey)
    private val mutableState = MutableStateFlow(initialGameState(setup.mode).copy(busy = resumeCheckpoint != null))
    val state = mutableState.asStateFlow()
    private var previousRoundId: Int? = null
    private var gameId: Int? = null
    private var roundId: Int? = null
    private var assignedPlayers: List<PlayerRound> = emptyList()
    private var snapshot: VotingSnapshot? = null
    private var lastOperation: (suspend () -> Unit)? = null

    init {
        viewModelScope.launch {
            resumeCheckpoint?.let { checkpoint ->
                try { restore(checkpoint) } catch (failure: Exception) {
                    android.util.Log.e("Impostor", "Could not restore game", failure)
                    gameId = null
                    roundId = null
                    sessionStore.clearCheckpoint()
                    mutableState.value = initialGameState(setup.mode)
                    if (setup.mode == GameModeType.CLASSIC) prepare()
                }
            }
            if (resumeCheckpoint == null && setup.mode == GameModeType.CLASSIC) prepare()
            state.collect { current ->
                gameId?.let { game ->
                    sessionStore.saveCheckpoint(GameCheckpoint(sessionKey, game, roundId,
                        previousRoundId, current.currentPlayer?.id,
                        current.viewedPlayers.map(PlayerUi::id), current.destination,
                        current.mode, current.selectedCategory))
                }
            }
        }
    }

    override fun onCleared() { backend.close() }

    private suspend fun restore(checkpoint: GameCheckpoint) {
        gameId = checkpoint.gameId
        previousRoundId = checkpoint.previousRoundId
        roundId = checkpoint.roundId
        val round = checkpoint.roundId?.let { requireNotNull(backend.gameRepository.getRound(it)) }
        if (round == null) {
            val mode = checkpoint.mode ?: setup.mode
            setup = setup.copy(mode = mode)
            val players = checkpoint.previousRoundId?.let { previous ->
                backend.gameRepository.getAssignments(previous)
                    .sortedBy { it.gamePlayer.seatOrder }
                    .mapIndexed { index, detail ->
                        PlayerUi(detail.gamePlayer.id, index + 1, setup.players[index])
                    }
            }.orEmpty()
            val summary = checkpoint.previousRoundId?.let { previous ->
                val result = backend.finishRound(previous)
                RoundSummaryUiState(result.winningRoles,
                    players.map { PlayerScoreUi(it, result.scores[it.id] ?: 0) },
                    result.incorrectTargetWinner, result.mrWhiteWinnerId != null)
            }
            val destination = if (checkpoint.destination == GameFlowDestination.FINISHED && summary != null)
                GameFlowDestination.FINISHED else if (mode == GameModeType.QUESTION)
                GameFlowDestination.CATEGORY_SELECTION else GameFlowDestination.PREPARING
            mutableState.value = initialGameState(mode).copy(
                destination = destination,
                selectedCategory = checkpoint.category ?: QuestionCategory.NBA,
                selectingNextRound = true, summary = summary, players = players)
            if (destination == GameFlowDestination.PREPARING) prepare()
            return
        }
        val mode = requireNotNull(backend.gameRepository.getRoundMode(round.id))
        setup = setup.copy(mode = mode)
        val details = backend.gameRepository.getAssignments(round.id).sortedBy { it.gamePlayer.seatOrder }
        require(details.size == setup.players.size)
        assignedPlayers = details.mapIndexed { index, detail ->
            PlayerRound(detail.gamePlayer.id, setup.players[index].name,
                setup.players[index].photoUri, detail.role.type, detail.content?.text)
        }
        val players = assignedPlayers.mapIndexed { index, player ->
            PlayerUi(player.gamePlayerId, index + 1, setup.players[index])
        }
        val secret = requireNotNull(assignedPlayers.first { it.role == RoleType.CIVILIAN }.secretContent)
        val roundContent = if (mode == GameModeType.QUESTION) RevealContent.Question(secret)
            else RevealContent.Word(secret)
        val viewed = players.filter { it.id in checkpoint.viewedIds }
        val base = GameFlowUiState(mode = mode, players = players, viewedPlayers = viewed,
            roundContent = roundContent, turnCatRes = TURN_CAT_IMAGES.random(),
            selectedCategory = checkpoint.category ?: QuestionCategory.NBA)
        mutableState.value = base.copy(busy = true)
        when (round.status) {
            RoundStatus.VOTING, RoundStatus.RESULTS, RoundStatus.FINISHED -> {
                val phase = backend.voting.get(round.id)
                applySnapshot(phase)
                if (phase.roundFinished) {
                    val result = backend.finishRound(round.id)
                    val scores = players.map { PlayerScoreUi(it, result.scores[it.id] ?: 0) }
                    val destination = if (checkpoint.destination in setOf(GameFlowDestination.FINISHED,
                            GameFlowDestination.SCORES)) checkpoint.destination else GameFlowDestination.ROUND_CONTENT
                    mutableState.value = state.value.copy(destination = destination,
                        summary = RoundSummaryUiState(result.winningRoles, scores,
                            result.incorrectTargetWinner, result.mrWhiteWinnerId != null))
                    if (destination == GameFlowDestination.SCORES) {
                        val byId = players.associateBy(PlayerUi::id)
                        val classification = backend.getClassification(checkpoint.gameId).map {
                            PlayerScoreUi(requireNotNull(byId[it.gamePlayerId]), it.totalScore)
                        }
                        mutableState.value = state.value.copy(classification = classification)
                    }
                }
            }
            else -> {
                val saved = backend.gameRepository.getAnswers(round.id)
                val byId = players.associateBy(PlayerUi::id)
                val answered = saved.map { requireNotNull(byId[it.gamePlayerId]) }
                val nextAnswer = players.firstOrNull { it.id !in answered.map(PlayerUi::id) }
                mutableState.value = when {
                    mode == GameModeType.QUESTION && saved.size == players.size -> base.copy(
                        destination = GameFlowDestination.ANSWER_RESULTS,
                        answers = saved.map { PlayerAnswerUi(requireNotNull(byId[it.gamePlayerId]), it.answerText) },
                        answeredPlayers = answered)
                    mode == GameModeType.QUESTION && (saved.isNotEmpty() ||
                        checkpoint.destination in setOf(GameFlowDestination.ANSWER_TURN,
                            GameFlowDestination.ANSWER_INPUT)) -> base.copy(
                        destination = GameFlowDestination.ANSWER_TURN,
                        currentPlayer = nextAnswer, answeredPlayers = answered,
                        answerPrompt = nextAnswer?.let { questionFor(it.id) })
                    viewed.size == players.size -> base.copy(destination = GameFlowDestination.READY)
                    else -> base.copy(destination = GameFlowDestination.TURN,
                        currentPlayer = players.first { it.id !in checkpoint.viewedIds })
                }
            }
        }
        mutableState.value = state.value.copy(busy = false)
    }

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
        if (setup.mode == GameModeType.CLASSIC) prepare()
    }

    fun prepare() = operation {
        val input = state.value
        val existing = gameId
        val round = if (existing == null) {
            val registrations = withContext(Dispatchers.IO) {
                setup.players.map { player ->
                    PlayerRegistration(player.name, player.photoUri ?: player.photo?.let(::savePhoto))
                }
            }
            val game = backend.prepareGame(registrations, setup.mode, setup.impostors, if (setup.mode == GameModeType.CLASSIC) setup.whites else 0,
                RoundContentInput("", "",
                    if (setup.mode == GameModeType.QUESTION) input.selectedCategory else null))
            gameId = game.gameId
            assignedPlayers = game.players
            game.roundId
        } else {
            val game = backend.prepareNextRound(existing, requireNotNull(previousRoundId), setup.mode,
                RoundContentInput("", "",
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
            selectedCategory = input.selectedCategory,
            roundContent = if (setup.mode == GameModeType.QUESTION) RevealContent.Question(secret) else RevealContent.Word(secret))
    }

    fun selectCategory(category: QuestionCategory) {
        if (!state.value.busy && state.value.destination == GameFlowDestination.CATEGORY_SELECTION)
            mutableState.value = state.value.copy(selectedCategory = category)
    }

    fun confirmCategory() {
        if (!state.value.busy && state.value.destination == GameFlowDestination.CATEGORY_SELECTION)
            prepare()
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
                    summary = RoundSummaryUiState(result.winningRoles, rows,
                        result.incorrectTargetWinner, result.mrWhiteWinnerId != null))
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
        previousRoundId = roundId ?: previousRoundId
        roundId = null
        assignedPlayers = emptyList()
        mutableState.value = state.value.copy(destination = if (setup.mode == GameModeType.QUESTION)
            GameFlowDestination.CATEGORY_SELECTION else GameFlowDestination.PREPARING,
            content = null, voting = VotingUiState(), guess = "", answerText = "", hasError = false,
            selectingNextRound = true)
        if (setup.mode == GameModeType.CLASSIC) prepare()
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

    fun finishSession(onFinished: () -> Unit) = operation {
        require(state.value.destination == GameFlowDestination.FINISHED)
        backend.finishGame(requireNotNull(gameId))
        gameId = null
        roundId = null
        sessionStore.clearSession()
        onFinished()
    }

    fun beginDiscussion() {
        if (state.value.mode != GameModeType.QUESTION || state.value.destination != GameFlowDestination.READY) return
        mutableState.value = state.value.copy(destination = GameFlowDestination.ANSWER_TURN,
            currentPlayer = state.value.players.first(), answerText = "", answeredPlayers = emptyList(),
            answerPrompt = questionFor(state.value.players.first().id))
    }

    fun showAnswerInput() {
        if (state.value.destination == GameFlowDestination.ANSWER_TURN)
            mutableState.value = state.value.copy(destination = GameFlowDestination.ANSWER_INPUT)
    }

    fun hideAnswerInput() {
        if (state.value.destination == GameFlowDestination.ANSWER_INPUT)
            mutableState.value = state.value.copy(destination = GameFlowDestination.ANSWER_TURN, answerText = "")
    }

    fun updateAnswer(text: String) {
        if (!state.value.busy && state.value.destination == GameFlowDestination.ANSWER_INPUT)
            mutableState.value = state.value.copy(answerText = text.take(80))
    }

    fun submitAnswer() = operation {
        val round = requireNotNull(roundId)
        val player = requireNotNull(state.value.currentPlayer)
        val text = state.value.answerText.trim()
        require(text.isNotBlank())
        val existing = backend.gameRepository.getAnswers(round).firstOrNull { it.gamePlayerId == player.id }
        if (existing == null) backend.submitAnswer(AnswerEntity(roundId = round,
            gamePlayerId = player.id, answerText = text))
        else check(existing.answerText == text) { "The saved answer cannot be changed." }
        val saved = backend.gameRepository.getAnswers(round)
        val players = state.value.players.associateBy { it.id }
        val answered = saved.map { requireNotNull(players[it.gamePlayerId]) }
        val next = state.value.players.firstOrNull { it.id !in saved.map(AnswerEntity::gamePlayerId) }
        mutableState.value = state.value.copy(destination = if (next == null)
            GameFlowDestination.ANSWER_RESULTS else GameFlowDestination.ANSWER_TURN,
            currentPlayer = next, answerText = "", answeredPlayers = answered,
            answerPrompt = next?.let { questionFor(it.id) },
            answers = if (next == null) saved.map { PlayerAnswerUi(requireNotNull(players[it.gamePlayerId]), it.answerText) }
                else emptyList())
    }

    private fun questionFor(playerId: Int): String = requireNotNull(
        assignedPlayers.single { it.gamePlayerId == playerId }.secretContent)

    fun beginVoting() = operation {
        require(state.value.destination == if (state.value.mode == GameModeType.QUESTION)
            GameFlowDestination.ANSWER_RESULTS else GameFlowDestination.READY)
        applySnapshot(backend.voting.begin(requireNotNull(roundId)))
    }

    fun vote(event: VotingEvent) {
        if (state.value.busy) return
        when (event) {
            is VotingEvent.SelectRole -> operation {
                applySnapshot(backend.voting.selectTarget(requireNotNull(roundId), event.role))
            }
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
            val voterId = requireNotNull(voting.currentPlayer).id
            val persisted = backend.voting.get(phase.roundId)
            val existingVote = persisted.takeIf { it.phaseId == phase.phaseId }
                ?.ballots?.firstOrNull { it.voterId == voterId }
            if (existingVote == null) backend.submitVote(phase.roundId, phase.phaseId, voterId, targetId)
            else check(existingVote.candidateId == targetId) { "The saved vote cannot be changed." }
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
            VotingDestination.VOTE_INTRO -> if (state.value.mode == GameModeType.CLASSIC)
                updateVoting { it.copy(destination = VotingDestination.SELECT_ROLE) }
            else mutableState.value = state.value.copy(destination = GameFlowDestination.ANSWER_RESULTS)
            VotingDestination.SELECT_ROLE -> Unit
            VotingDestination.VOTE_RESULTS -> Unit
        }
    }

    fun retry() { lastOperation?.let(::operation) }

    private fun applySnapshot(value: VotingSnapshot) {
        snapshot = value
        val players = state.value.players.associateBy { it.id }
        val destination = when {
            value.status == VotingPhaseStatus.TARGET_SELECTION -> VotingDestination.SELECT_ROLE
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
        
        val results = if (!value.pendingHandoff && value.status in setOf(VotingPhaseStatus.RESULTS,
                VotingPhaseStatus.TIE, VotingPhaseStatus.REVEALED, VotingPhaseStatus.GUESS_PENDING)) VoteResultUiState(
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
            target = value.target, availableTargets = value.availableTargets,
            result = results, revealedRole = value.revealedRole,
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
    destination = if (mode == GameModeType.QUESTION) GameFlowDestination.CATEGORY_SELECTION else GameFlowDestination.PREPARING)
