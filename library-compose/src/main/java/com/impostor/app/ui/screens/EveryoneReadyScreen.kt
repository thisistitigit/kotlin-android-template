@file:Suppress("MagicNumber")

package com.impostor.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.impostor.app.ui.components.GameScreenLayout
import com.impostor.app.ui.components.GameActionButton
import com.impostor.app.ui.components.AppCard
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
import com.composables.icons.lucide.R as LucideR

/**
 * Screen displayed when all players have seen their secret content.
 * It shows a celebratory kitten and a row of all players with ready checkmarks.
 */
@Composable
fun EveryoneReadyScreen(
    players: List<TurnPlayerUi>,
    onBack: () -> Unit,
    onStartVoting: () -> Unit,
    modifier: Modifier = Modifier
) {
    GameScreenLayout(onBack, modifier, contentArrangement = Arrangement.spacedBy(Spacing.medium),
        bottomAction = { GameActionButton(stringResource(R.string.start_voting), onStartVoting) }) {
        Text(buildAnnotatedString {
            append(stringResource(R.string.everyone_is))
            append(" ")
            withStyle(SpanStyle(color = MainPurple)) { append(stringResource(R.string.ready_suffix)) }
        }, style = AppTextStyles.title, color = AppWhite, textAlign = TextAlign.Center)
        Text(stringResource(R.string.all_players_done), color = AppWhite, textAlign = TextAlign.Center)
        Image(painterResource(R.drawable.thumbs_up_kitten), null, contentScale = ContentScale.Fit,
            modifier = Modifier.size(220.dp))
        ReadyPlayersCard(players, compact = true, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ReadyPlayersCard(
    players: List<TurnPlayerUi>,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.height(if (compact) 140.dp else 180.dp),
        backgroundColor = DeepPurple,
        contentPadding = Spacing.medium
    ) {
        val avatarSize = 60.dp
        val overlapStep = 40.dp
        val totalWidth = avatarSize + (overlapStep * (players.size - 1))

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Overlapping avatars similar to SeenAvatars but centered
            Box(modifier = Modifier.width(totalWidth).height(avatarSize + 4.dp)) {
                players.forEachIndexed { index, player ->
                    Box(
                        modifier = Modifier
                            .offset(x = overlapStep * index)
                            .zIndex(index.toFloat())
                    ) {
                        PlayerAvatar(
                            avatarRes = player.avatarRes,
                            photo = player.photo,
                            decoration = PlayerAvatarDecoration(AppWhite, 2.dp),
                            modifier = Modifier.size(avatarSize)
                        )
                        // Ready Checkbox (MarkerYellow circle with check icon)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(MarkerYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(LucideR.drawable.lucide_ic_check),
                                contentDescription = null,
                                tint = AppBackground,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
internal fun EveryoneReadyPreview() = ImpostorTheme {
    EveryoneReadyScreen(
        players = listOf(
            TurnPlayerUi("Tiago", R.drawable.vibrent_4),
            TurnPlayerUi("Ana", R.drawable.vibrent_1),
            TurnPlayerUi("Luís", R.drawable.vibrent_2),
            TurnPlayerUi("Mia", R.drawable.vibrent_3),
            TurnPlayerUi("Bia", R.drawable.vibrent_5),
            TurnPlayerUi("Rui", R.drawable.vibrent_6)
        ),
        onBack = {},
        onStartVoting = {}
    )
}
