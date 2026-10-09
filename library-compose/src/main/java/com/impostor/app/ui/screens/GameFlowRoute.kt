package com.impostor.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.impostor.library.compose.R
import com.impostor.library.domain.GameBackend

@Composable
fun GameFlowRoute(setup: GameFlowSetup, sessionKey: String, onExit: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val factory = remember(context) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                GameFlowViewModel(GameBackend.create(context), context, setup, sessionKey) as T
        }
    }
    val model: GameFlowViewModel = viewModel(key = "game-flow", factory = factory)
    LaunchedEffect(sessionKey) { model.configureSession(sessionKey, setup) }
    val state by model.state.collectAsStateWithLifecycle()
    BackHandler {
        if (!state.busy) when (state.destination) {
            GameFlowDestination.CATEGORY_SELECTION -> if (state.selectingNextRound) model.showSummary() else onExit()
            GameFlowDestination.PREPARING -> Unit
            GameFlowDestination.CONTENT_REVEAL -> model.hideContent()
            GameFlowDestination.READY -> model.readyBack()
            GameFlowDestination.ANSWER_INPUT -> model.hideAnswerInput()
            GameFlowDestination.ANSWER_TURN -> Unit
            GameFlowDestination.ANSWER_RESULTS -> Unit
            GameFlowDestination.SCORES -> model.showSummary()
            GameFlowDestination.FINISHED -> model.finishSession(onExit)
            GameFlowDestination.MR_WHITE_GUESS -> Unit
            GameFlowDestination.ROUND_CONTENT -> model.showSummary()
            GameFlowDestination.VOTING -> model.vote(VotingEvent.Back)
            else -> onExit()
        }
    }
    Box(Modifier.fillMaxSize()) {
        GameFlowContent(state, { if (!state.busy) onExit() }, model)
        if (state.busy) CircularProgressIndicator(Modifier.align(Alignment.Center))
        if (state.hasError) {
            AlertDialog(onDismissRequest = { model.retry() }, title = { Text(stringResource(R.string.voting_error)) },
                text = { Text(stringResource(R.string.voting_error_detail)) },
                confirmButton = { TextButton(onClick = model::retry) { Text(stringResource(R.string.retry)) } },
                dismissButton = { TextButton(onClick = onExit) { Text(stringResource(R.string.new_game)) } })
        }
    }
}

@Composable
private fun GameFlowContent(state: GameFlowUiState, onExit: () -> Unit, model: GameFlowViewModel) {
    when (state.destination) {
        GameFlowDestination.CATEGORY_SELECTION -> CategoryScreen(state.selectedCategory, model::selectCategory,
            { if (state.selectingNextRound) model.showSummary() else onExit() },
            model::confirmCategory, enabled = !state.busy)
        GameFlowDestination.PREPARING -> Box(Modifier.fillMaxSize())
        GameFlowDestination.TURN -> PlayerTurnScreen(
            PlayerTurnUiState(requireNotNull(state.currentPlayer).avatar, state.viewedPlayers.map { it.avatar },
                state.turnCatRes, stringResource(R.string.see_content)), onExit, model::showContent)
        GameFlowDestination.CONTENT_REVEAL -> ContentRevealScreen(
            ContentRevealUiState(requireNotNull(state.currentPlayer).avatar, state.currentPlayer.number,
                requireNotNull(state.content), R.drawable.your_word_cat), model::hideContent, model::finishReveal)
        GameFlowDestination.READY -> EveryoneReadyScreen(state.players.map { it.avatar }, model::readyBack,
            if (state.mode == com.impostor.library.domain.enums.GameModeType.QUESTION)
                model::beginDiscussion else model::beginVoting)
        GameFlowDestination.ANSWER_TURN -> AnswerTurnScreen(requireNotNull(state.currentPlayer),
            state.answeredPlayers, model::showAnswerInput)
        GameFlowDestination.ANSWER_INPUT -> AnswerInputScreen(requireNotNull(state.currentPlayer),
            requireNotNull(state.answerPrompt), state.answerText, state.busy,
            model::updateAnswer, model::hideAnswerInput, model::submitAnswer)
        GameFlowDestination.ANSWER_RESULTS -> AnswerResultsScreen(state.answers, model::beginVoting)
        GameFlowDestination.VOTING -> VotingRoute(state.voting, model::vote)
        GameFlowDestination.MR_WHITE_GUESS -> MrWhiteGuessScreen(
            MrWhiteGuessUiState(requireNotNull(state.voting.result?.selectedPlayer), state.guess, state.busy),
            model::updateGuess, model::submitGuess)
        GameFlowDestination.ROUND_CONTENT -> RoundContentRevealScreen(requireNotNull(state.roundContent),
            model::showSummary, model::showSummary)
        GameFlowDestination.FINISHED -> RoundSummaryScreen(requireNotNull(state.summary),
            { model.finishSession(onExit) }, model::playAgain, model::showScores)
        GameFlowDestination.SCORES -> ScoresScreen(state.classification, model::showSummary, model::playAgain)

    }
}

@Composable
fun VotingRoute(state: VotingUiState, onEvent: (VotingEvent) -> Unit) {
    val back = { onEvent(VotingEvent.Back) }
    when (state.destination) {
        VotingDestination.SELECT_ROLE -> VoteRoleSelectionScreen(state.availableTargets,
            { onEvent(VotingEvent.SelectRole(it)) })
        VotingDestination.VOTE_INTRO -> VoteIntroScreen(requireNotNull(state.currentPlayer), back,
            { onEvent(VotingEvent.OpenSelection) })
        VotingDestination.SELECT_CANDIDATE -> VoteSelectionScreen(state, back,
            { onEvent(VotingEvent.ToggleSelection(it)) },
            { onEvent(VotingEvent.Confirm) })
        VotingDestination.VOTE_LOCKED -> VoteLockedScreen(requireNotNull(state.currentPlayer), state.nextPlayer,
            back, { onEvent(VotingEvent.Acknowledge) })
        VotingDestination.VOTE_RESULTS -> VoteResultsScreen(requireNotNull(state.result), back,
            { onEvent(if (state.result.isTie) VotingEvent.Continue else VotingEvent.Reveal) })
        VotingDestination.REVEAL_ROLE -> VoteRoleRevealScreen(
            VoteRoleRevealUiState(requireNotNull(state.result?.selectedPlayer),
                requireNotNull(state.revealedRole), state.roundFinished, state.target), back,
            { onEvent(VotingEvent.Continue) })
    }
}
