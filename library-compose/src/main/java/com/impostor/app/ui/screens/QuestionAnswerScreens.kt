package com.impostor.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.impostor.app.ui.components.AppCard
import com.impostor.app.ui.components.GameActionButton
import com.impostor.app.ui.components.GameScreenLayout
import com.impostor.app.ui.components.PlayerChip
import com.impostor.app.ui.components.PrivacyCard
import com.impostor.app.ui.theme.AppTextStyles
import com.impostor.app.ui.theme.AppWhite
import com.impostor.app.ui.theme.DeepPurple
import com.impostor.app.ui.theme.ImpostorTheme
import com.impostor.app.ui.theme.MainPurple
import com.impostor.app.ui.theme.Spacing
import com.impostor.library.compose.R

@Composable
fun AnswerTurnScreen(player: PlayerUi, answered: List<PlayerUi>, onContinue: () -> Unit) {
    GameScreenLayout(null, contentArrangement = Arrangement.spacedBy(Spacing.large),
        bottomAction = { GameActionButton(stringResource(R.string.answer_now), onContinue) }) {
        PlayerChip(player.avatar, player.number)
        Text(stringResource(R.string.answer_your_turn), style = AppTextStyles.title,
            color = MainPurple, textAlign = TextAlign.Center)
        Text(stringResource(R.string.pass_phone_to, player.avatar.name), color = AppWhite,
            textAlign = TextAlign.Center)
        Text(stringResource(R.string.answers_progress, answered.size), color = AppWhite)
    }
}

@Composable
fun AnswerInputScreen(player: PlayerUi, question: String, answer: String,
    busy: Boolean, onAnswerChange: (String) -> Unit, onBack: () -> Unit, onSubmit: () -> Unit) {
    GameScreenLayout(onBack, contentArrangement = Arrangement.spacedBy(Spacing.medium),
        bottomAction = { GameActionButton(stringResource(R.string.guess_submit), onSubmit,
            enabled = answer.isNotBlank() && !busy) }) {
        PlayerChip(player.avatar, player.number)
        Text(stringResource(R.string.your_question_is), style = AppTextStyles.title, color = AppWhite)
        Text(question, style = AppTextStyles.labelsScroll, color = MainPurple,
            textAlign = TextAlign.Center)
        OutlinedTextField(answer, onAnswerChange, enabled = !busy, singleLine = true,
            label = { Text(stringResource(R.string.your_answer)) }, modifier = Modifier.fillMaxWidth())
        PrivacyCard(stringResource(R.string.dont_show_answer), Modifier.fillMaxWidth())
    }
}

@Composable
fun AnswerResultsScreen(answers: List<PlayerAnswerUi>, onStartVoting: () -> Unit) {
    GameScreenLayout(null, contentArrangement = Arrangement.spacedBy(Spacing.medium),
        bottomAction = { GameActionButton(stringResource(R.string.start_voting), onStartVoting) }) {
        Text(stringResource(R.string.answers_are_in), style = AppTextStyles.title,
            color = MainPurple, textAlign = TextAlign.Center)
        Text(stringResource(R.string.discuss_answers), color = AppWhite, textAlign = TextAlign.Center)
        answers.forEach { answer ->
            AppCard(modifier = Modifier.fillMaxWidth(), backgroundColor = DeepPurple) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    PlayerChip(answer.player.avatar, answer.player.number)
                    Text(answer.text, color = AppWhite, style = AppTextStyles.labelsScroll)
                }
            }
        }
    }
}

@Preview @Composable internal fun AnswerInputPreview() = ImpostorTheme {
    AnswerInputScreen(PlayerUi(1, 1, TurnPlayerUi("Tiago", R.drawable.vibrent_1)),
        "Which NBA player would make the best school principal?", "", false, {}, {}, {})
}

@Preview @Composable internal fun AnswerResultsPreview() = ImpostorTheme {
    AnswerResultsScreen(listOf(
        PlayerAnswerUi(PlayerUi(1, 1, TurnPlayerUi("Tiago", R.drawable.vibrent_1)), "LeBron James"),
        PlayerAnswerUi(PlayerUi(2, 2, TurnPlayerUi("Ana", R.drawable.vibrent_2)), "Michael Jordan")
    ), {})
}
