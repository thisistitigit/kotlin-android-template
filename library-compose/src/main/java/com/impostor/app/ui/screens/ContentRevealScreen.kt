@file:Suppress("MagicNumber")

package com.impostor.app.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.impostor.app.ui.components.GameScreenLayout
import com.impostor.app.ui.components.GameActionButton
import com.impostor.app.ui.components.SecretContentCard
import com.impostor.app.ui.components.PlayerChip
import com.impostor.app.ui.components.PrivacyCard
import com.impostor.app.ui.theme.AppTextStyles
import com.impostor.app.ui.theme.AppWhite
import com.impostor.app.ui.theme.MainPurple
import com.impostor.app.ui.theme.Spacing
import com.impostor.app.ui.theme.ImpostorTheme
import com.impostor.library.compose.R

// â”€â”€ Domain types â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

sealed interface RevealContent {
    data class Word(val value: String) : RevealContent
    data class Question(val value: String) : RevealContent
    data object MrWhite : RevealContent
}

data class ContentRevealUiState(
    val player: TurnPlayerUi,
    val playerNumber: Int,
    val content: RevealContent,
    @DrawableRes val catRes: Int,
    val isVisible: Boolean = true
)

// â”€â”€ Main screen â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
fun ContentRevealScreen(
    state: ContentRevealUiState,
    onBack: () -> Unit,
    onGotIt: () -> Unit,
    modifier: Modifier = Modifier
) {
    GameScreenLayout(onBack, modifier, contentArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Spacing.medium),
        bottomAction = { GameActionButton(stringResource(R.string.got_it), onGotIt) }) {
        PlayerChip(state.player, state.playerNumber, Modifier.fillMaxWidth())
        RevealHeading(state.player.name, state.content)
        if (!state.isVisible) {
            RevealPrivacyCard(state.content, isCover = true)
        } else {
            when (val content = state.content) {
                is RevealContent.Word -> {
                    Text(stringResource(R.string.remember_it), style = MaterialTheme.typography.body1,
                        color = AppWhite.copy(alpha = 0.7f))
                    SecretContentCard(content.value, isWord = true)
                }
                is RevealContent.Question -> SecretContentCard(content.value, isWord = false)
                RevealContent.MrWhite -> Unit
            }
            Image(painterResource(if (state.content == RevealContent.MrWhite) R.drawable.mr_white else state.catRes),
                null, contentScale = ContentScale.Fit, modifier = Modifier.size(220.dp))
            RevealPrivacyCard(state.content)
        }
    }
}

// â”€â”€ Sub-components â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun RevealHeading(
    playerName: String,
    content: RevealContent,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        when (content) {
            RevealContent.MrWhite -> {
                Text(
                    text = playerName,
                    style = AppTextStyles.title,
                    color = AppWhite,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.youre_mr_white),
                    style = AppTextStyles.title,
                    color = AppWhite,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.good_luck),
                    style = MaterialTheme.typography.h6.copy(fontSize = 20.sp),
                    color = AppWhite.copy(alpha = 0.82f),
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                val suffix = if (content is RevealContent.Word)
                    stringResource(R.string.your_word_is)
                else
                    stringResource(R.string.your_question_is)

                Text(
                    text = playerName,
                    style = AppTextStyles.title,
                    color = AppWhite,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = suffix,
                    style = AppTextStyles.title,
                    color = MainPurple,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun RevealPrivacyCard(content: RevealContent, modifier: Modifier = Modifier, isCover: Boolean = false) {
    val text = when {
        isCover -> stringResource(R.string.content_hidden)
        content is RevealContent.Question -> stringResource(R.string.dont_show_question)
        content is RevealContent.MrWhite -> stringResource(R.string.whitey)
        else -> stringResource(R.string.dont_show_word)
    }
    PrivacyCard(text = text, modifier = modifier.fillMaxWidth())
}


@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
internal fun MrWhiteRevealPreview() = ImpostorTheme {
    ContentRevealScreen(
        state = ContentRevealUiState(
            player = TurnPlayerUi("Tiago", R.drawable.vibrent_4),
            playerNumber = 4,
            content = RevealContent.MrWhite,
            catRes = R.drawable.mr_white,
            isVisible = true
        ),
        onBack = {},
        onGotIt = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
internal fun QuestionRevealPreview() = ImpostorTheme {
    ContentRevealScreen(
        state = ContentRevealUiState(
            player = TurnPlayerUi("Tiago", R.drawable.vibrent_4),
            playerNumber = 4,
            content = RevealContent.Question("Who's The Most Performative NBA Player"),
            catRes = R.drawable.your_word_cat,
            isVisible = true
        ),
        onBack = {},
        onGotIt = {}
    )
}
