@file:Suppress("MagicNumber")

package com.impostor.app.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.impostor.app.ui.components.AppButton
import com.impostor.app.ui.components.AppButtonStyle
import com.impostor.app.ui.components.AppCard
import com.impostor.app.ui.components.AppHeader
import com.impostor.app.ui.components.PlayerAvatar
import com.impostor.app.ui.components.PlayerAvatarDecoration
import com.impostor.app.ui.theme.AppBackground
import com.impostor.app.ui.theme.AppTextStyles
import com.impostor.app.ui.theme.AppWhite
import com.impostor.app.ui.theme.DeepPurple
import com.impostor.app.ui.theme.MainPurple
import com.impostor.app.ui.theme.MarkerYellow
import com.impostor.app.ui.theme.Spacing
import com.impostor.app.ui.theme.ImpostorTheme
import com.impostor.library.compose.R

// ── Domain types ──────────────────────────────────────────────────────────────

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

// ── Main screen ───────────────────────────────────────────────────────────────

@Composable
fun ContentRevealScreen(
    state: ContentRevealUiState,
    onBack: () -> Unit,
    onGotIt: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize().background(AppBackground)) {
        val compact = maxHeight < 700.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.medium)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppHeader(onBack = onBack)

            Spacer(Modifier.height(Spacing.small))

            PlayerChip(
                player = state.player,
                playerNumber = state.playerNumber,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(if (compact) Spacing.medium else Spacing.large))

            RevealHeading(
                playerName = state.player.name,
                content = state.content
            )

            Spacer(Modifier.height(if (compact) Spacing.medium else Spacing.large))

            // Central Area: Content or Privacy Cover
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (!state.isVisible) {
                    PrivacyCard(content = state.content, isCover = true)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        when (state.content) {
                            is RevealContent.Word -> {
                                Text(
                                    text = stringResource(R.string.remember_it),
                                    style = MaterialTheme.typography.body1,
                                    color = AppWhite.copy(alpha = 0.7f)
                                )
                                Spacer(Modifier.height(Spacing.medium))
                                ContentBubble(text = state.content.value, isWord = true)
                            }
                            is RevealContent.Question -> {
                                ContentBubble(text = state.content.value, isWord = false)
                            }
                            RevealContent.MrWhite -> { /* No bubble for Mr White */ }
                        }

                        Spacer(Modifier.height(Spacing.large))

                        Image(
                            painter = painterResource(
                                if (state.content is RevealContent.MrWhite) R.drawable.mr_white else state.catRes
                            ),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(if (compact) 240.dp else 300.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.medium))

            // Privacy Card (Footer style, exactly like SetupScreens)
            if (state.isVisible) {
                PrivacyCard(content = state.content)
            }

            Spacer(Modifier.height(if (compact) Spacing.medium else Spacing.large))

            AppButton(
                text = stringResource(R.string.got_it),
                onClick = onGotIt,
                style = AppButtonStyle.SECONDARY,
                modifier = Modifier.width(172.dp)
            )

            Spacer(Modifier.height(Spacing.extraLarge))
        }
    }
}

// ── Sub-components ────────────────────────────────────────────────────────────

@Composable
private fun PlayerChip(
    player: TurnPlayerUi,
    playerNumber: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        PlayerAvatar(
            avatarRes = player.avatarRes,
            photo = player.photo,
            decoration = PlayerAvatarDecoration(borderColor = MarkerYellow, borderWidth = 1.dp),
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.width(Spacing.small))
        Text(
            text = "Player $playerNumber",
            style = MaterialTheme.typography.body2,
            color = AppWhite.copy(alpha = 0.6f)
        )
        Spacer(Modifier.width(Spacing.small))
        Text(text = "•", color = AppWhite.copy(alpha = 0.6f))
        Spacer(Modifier.width(Spacing.small))
        Text(
            text = player.name,
            style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
            color = MainPurple
        )
    }
}

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
                    text = buildAnnotatedString {
                        append("You’re ")
                        withStyle(SpanStyle(color = MainPurple)) {
                            append("Mr White")
                        }
                    },
                    style = AppTextStyles.title,
                    color = AppWhite,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Good Luck!",
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
private fun ContentBubble(
    text: String,
    isWord: Boolean,
    modifier: Modifier = Modifier
) {
    if (isWord) {
        Box(
            modifier = modifier
                .padding(horizontal = Spacing.medium)
                .rotate(WORD_BUBBLE_ROTATION),
            contentAlignment = Alignment.Center
        ) {
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MainPurple,
                contentPadding = Spacing.large
            ) {
                Text(
                    text = text,
                    style = AppTextStyles.title,
                    color = AppBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    } else {
        // Question Bubble with tail
        Column(
            modifier = modifier.padding(horizontal = Spacing.medium),
            horizontalAlignment = Alignment.Start
        ) {
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MainPurple,
                contentPadding = Spacing.large
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.h6.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        lineHeight = 28.sp
                    ),
                    color = AppBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.large) // Force 2 lines behavior
                )
            }
            // Bubble Tail
            Canvas(
                modifier = Modifier
                    .offset(x = 24.dp, y = (-2).dp)
                    .size(24.dp, 20.dp)
            ) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = MainPurple)
            }
        }
    }
}

@Composable
private fun PrivacyCard(
    content: RevealContent,
    modifier: Modifier = Modifier,
    isCover: Boolean = false
) {
    val privacyText = when (content) {
        is RevealContent.Question -> stringResource(R.string.dont_show_question)
        else -> stringResource(R.string.dont_show_word)
    }

    AppCard(
        modifier = if (isCover) modifier.fillMaxWidth(0.9f) else modifier.fillMaxWidth(),
        backgroundColor = DeepPurple,
        contentPadding = Spacing.medium
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(if (isCover) 80.dp else 64.dp)
                    .background(AppWhite.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(com.composables.icons.lucide.R.drawable.lucide_ic_lock_keyhole),
                    contentDescription = null,
                    tint = AppWhite.copy(alpha = 0.6f),
                    modifier = Modifier.size(if (isCover) 40.dp else 32.dp)
                )
            }
            Spacer(modifier = Modifier.height(Spacing.medium))
            Text(
                text = if (isCover) stringResource(R.string.content_hidden) else privacyText,
                style = MaterialTheme.typography.body2,
                color = AppWhite.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private const val WORD_BUBBLE_ROTATION = -7f

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