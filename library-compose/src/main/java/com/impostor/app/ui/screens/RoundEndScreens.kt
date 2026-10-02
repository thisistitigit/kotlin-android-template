@file:Suppress("MagicNumber")

package com.impostor.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.impostor.app.ui.components.GameScreenLayout
import com.impostor.app.ui.components.GameActionButton
import com.impostor.app.ui.components.SecretContentCard
import com.impostor.app.ui.components.PlayerAvatar
import com.impostor.app.ui.components.TitleBanner
import com.impostor.app.ui.theme.AppBackground
import com.impostor.app.ui.theme.AppTextStyles
import com.impostor.app.ui.theme.AppWhite
import com.impostor.app.ui.theme.DeepPurple
import com.impostor.app.ui.theme.ImpostorTheme
import com.impostor.app.ui.theme.LightPurple
import com.impostor.app.ui.theme.MainPurple
import com.impostor.app.ui.theme.MarkerYellow
import com.impostor.app.ui.theme.Spacing
import com.impostor.library.compose.R
import com.impostor.library.domain.enums.RoleType

data class MrWhiteGuessUiState(val player: PlayerUi, val text: String, val busy: Boolean = false)
data class PlayerScoreUi(val player: PlayerUi, val points: Int)
data class RoundSummaryUiState(val winningRoles: Set<RoleType>, val scores: List<PlayerScoreUi>)

/** Role variants share one layout and never read undisclosed assignments. */
@Composable
fun RoleRevealScreen(state: VoteRoleRevealUiState, onBack: () -> Unit, onContinue: () -> Unit,
    modifier: Modifier = Modifier) {
    val civilian = state.role == RoleType.CIVILIAN
    val background = if (state.role == RoleType.IMPOSTOR) DeepPurple else AppBackground
    GameScreenLayout(onBack, modifier, background, contentArrangement = Arrangement.spacedBy(Spacing.medium), bottomAction = {
        GameActionButton(stringResource(R.string.continue_label), onClick = onContinue)
    }) {
        val content: @Composable () -> Unit = {
            Column(modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                Text(stringResource(if (civilian) R.string.role_not_impostor else R.string.role_caught),
                    style = if (civilian) AppTextStyles.labelsScroll else AppTextStyles.title,
                    textAlign = TextAlign.Center, color = if (civilian) AppBackground else MainPurple)
                VotingAsset(when (state.role) {
                    RoleType.CIVILIAN -> "civilian_reveal_cat"
                    RoleType.IMPOSTOR -> "impostor"
                    RoleType.MR_WHITE -> "mr_white"
                }, Modifier.height(200.dp))
                VotingTitle(state.player.avatar.name, if (civilian) AppBackground else LightPurple)
                Text(stringResource(when (state.role) {
                    RoleType.CIVILIAN -> R.string.role_was_civilian
                    RoleType.IMPOSTOR -> R.string.role_was_impostor
                    RoleType.MR_WHITE -> R.string.role_is_white
                }), style = AppTextStyles.labelsScroll, textAlign = TextAlign.Center,
                    color = if (civilian) AppBackground else AppWhite)
            }
        }
        if (civilian) TitleBanner("", height = 440.dp, backgroundColor = MarkerYellow,
            oval = true, content = content) else content()
    }
}

@Composable
fun MrWhiteGuessScreen(state: MrWhiteGuessUiState, onTextChange: (String) -> Unit,
    onSubmit: () -> Unit, modifier: Modifier = Modifier) {
    // Back cannot bypass a required, private guess or disclose the secret.
    BoxWithConstraints(modifier.fillMaxSize()) {
        val compact = maxHeight < 640.dp
        GameScreenLayout(null, Modifier.imePadding(), contentArrangement = Arrangement.spacedBy(Spacing.medium), bottomAction = {
            GameActionButton(stringResource(R.string.guess_submit), onSubmit, state.text.isNotBlank() && !state.busy)
        }) {
            Text(stringResource(R.string.guess_word_title),
                style = if (compact) AppTextStyles.labelsScroll else AppTextStyles.title,
                color = AppWhite, textAlign = TextAlign.Center)
            VotingAsset("guess_word_cat", Modifier.height(if (compact) 140.dp else 240.dp))
            Text(stringResource(R.string.guess_word_hint), color = AppWhite)
            OutlinedTextField(state.text, onTextChange, enabled = !state.busy, singleLine = true,
                label = { Text(stringResource(R.string.guess_word_input)) },
                colors = TextFieldDefaults.outlinedTextFieldColors(textColor = AppWhite,
                    cursorColor = MainPurple, focusedBorderColor = MainPurple,
                    unfocusedBorderColor = LightPurple, focusedLabelColor = LightPurple,
                    unfocusedLabelColor = LightPurple), modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun RoundContentRevealScreen(content: RevealContent, onBack: () -> Unit, onContinue: () -> Unit,
    modifier: Modifier = Modifier) {
    require(content != RevealContent.MrWhite)
    val question = content is RevealContent.Question
    GameScreenLayout(onBack, modifier, contentArrangement = Arrangement.spacedBy(Spacing.medium), bottomAction = {
        GameActionButton(stringResource(R.string.continue_label), onClick = onContinue)
    }) {
        VotingTitle(stringResource(if (question) R.string.round_question_title else R.string.round_word_title), AppWhite)
        SecretContentCard(when (content) {
            is RevealContent.Question -> content.value
            is RevealContent.Word -> content.value
            RevealContent.MrWhite -> error("A completed round must have public content")
        }, isWord = !question)
        VotingAsset(if (question) "win" else "civilian_reveal_cat")
        Text(stringResource(R.string.round_content_finished), style = AppTextStyles.labelsScroll, color = AppWhite)
    }
}

@Composable
private fun ScoreRows(scores: List<PlayerScoreUi>, cumulative: Boolean) {
    scores.forEach { score ->
        Row(Modifier.fillMaxWidth().padding(vertical = Spacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            PlayerAvatar(score.player.avatar.avatarRes, photo = score.player.avatar.photo,
                modifier = Modifier.size(40.dp))
            Text(score.player.avatar.name, modifier = Modifier.weight(1f), color = AppWhite)
            Text(stringResource(if (cumulative) R.string.total_points else R.string.round_points, score.points),
                style = AppTextStyles.labelsScroll, color = MarkerYellow)
        }
    }
}

@Composable
fun RoundSummaryScreen(state: RoundSummaryUiState, onBack: () -> Unit, onNewGame: () -> Unit,
    onScores: () -> Unit, modifier: Modifier = Modifier) {
    val white = state.winningRoles == setOf(RoleType.MR_WHITE)
    val civilian = RoleType.CIVILIAN in state.winningRoles
    GameScreenLayout(onBack, modifier, secondaryAction = {
        TextButton(onClick = onScores) { Text(stringResource(R.string.view_scores), color = MarkerYellow) }
    }, contentArrangement = Arrangement.spacedBy(Spacing.medium), bottomAction = { GameActionButton(stringResource(R.string.new_game), onClick = onNewGame) }) {
        VotingTitle(stringResource(R.string.game_over_title), LightPurple)
        Text(stringResource(when {
            white -> R.string.white_win_title
            civilian -> R.string.civilians_win_title
            else -> R.string.impostors_win_title
        }), style = AppTextStyles.labelsScroll, color = MarkerYellow, textAlign = TextAlign.Center)
        VotingAsset("win")
        Text(stringResource(when {
            white -> R.string.white_win_reason
            civilian -> R.string.civilians_win_reason
            else -> R.string.impostors_win_reason
        }), color = AppWhite, textAlign = TextAlign.Center)
        ScoreRows(state.scores, false)
    }
}

@Composable
fun ScoresScreen(scores: List<PlayerScoreUi>, onBack: () -> Unit, onNewGame: () -> Unit,
    modifier: Modifier = Modifier) {
    GameScreenLayout(onBack, modifier, contentArrangement = Arrangement.spacedBy(Spacing.medium), bottomAction = {
        GameActionButton(stringResource(R.string.new_game), onClick = onNewGame)
    }) {
        VotingTitle(stringResource(R.string.scores_title), AppWhite)
        ScoreRows(scores, true)
    }
}

private val exampleEndPlayer = PlayerUi(1, 1, TurnPlayerUi("Tiago", R.drawable.vibrent_4))

@Preview @Composable internal fun CivilianEndPreview() = ImpostorTheme {
    RoleRevealScreen(VoteRoleRevealUiState(exampleEndPlayer, RoleType.CIVILIAN, false), {}, {})
}
@Preview @Composable internal fun ImpostorEndPreview() = ImpostorTheme {
    RoleRevealScreen(VoteRoleRevealUiState(exampleEndPlayer, RoleType.IMPOSTOR, false), {}, {})
}
@Preview @Composable internal fun MrWhiteEndPreview() = ImpostorTheme {
    RoleRevealScreen(VoteRoleRevealUiState(exampleEndPlayer, RoleType.MR_WHITE, false), {}, {})
}
@Preview(widthDp = 320, heightDp = 568) @Composable internal fun GuessSmallPreview() = ImpostorTheme {
    MrWhiteGuessScreen(MrWhiteGuessUiState(exampleEndPlayer, ""), {}, {})
}
@Preview @Composable internal fun WordEndPreview() = ImpostorTheme {
    RoundContentRevealScreen(RevealContent.Word("Mountain"), {}, {})
}
@Preview @Composable internal fun QuestionEndPreview() = ImpostorTheme {
    RoundContentRevealScreen(RevealContent.Question("Who is the most competitive player?"), {}, {})
}
@Preview @Composable internal fun SummaryEndPreview() = ImpostorTheme {
    RoundSummaryScreen(RoundSummaryUiState(setOf(RoleType.CIVILIAN),
        (1..8).map { PlayerScoreUi(exampleEndPlayer.copy(id = it, number = it), it) }), {}, {}, {})
}
