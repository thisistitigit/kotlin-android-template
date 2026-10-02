@file:Suppress("MagicNumber")

package com.impostor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.impostor.app.ui.screens.PlayerUi
import com.impostor.app.ui.theme.AppBackground
import com.impostor.app.ui.theme.AppTextStyles
import com.impostor.app.ui.theme.AppWhite
import com.impostor.app.ui.theme.LightPurple
import com.impostor.app.ui.theme.MainPurple
import com.impostor.app.ui.theme.MarkerYellow
import com.impostor.app.ui.theme.Spacing
import com.composables.icons.lucide.R as LucideR

@Composable
fun VoteCandidateCard(
    player: PlayerUi,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .defaultMinSize(minWidth = 100.dp)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(Spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        Box(Modifier.size(96.dp)) {
            PlayerAvatar(
                player.avatar.avatarRes,
                photo = player.avatar.photo,
                decoration = PlayerAvatarDecoration(
                    if (selected) MarkerYellow else Color.Transparent,
                    if (selected) 4.dp else 0.dp
                ),
                modifier = Modifier.fillMaxSize()
            )
            if (selected) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .background(MarkerYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(LucideR.drawable.lucide_ic_check),
                        null,
                        tint = AppBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Text(
            player.avatar.name,
            style = AppTextStyles.labelsScroll.copy(fontSize = 16.sp),
            color = AppWhite,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun VoteResultCard(
    player: PlayerUi,
    voteCount: Int,
    totalVotes: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalVotes > 0) voteCount.toFloat() / totalVotes else 0f
    
    Column(
        modifier = modifier.width(100.dp).padding(Spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        PlayerAvatar(
            player.avatar.avatarRes,
            photo = player.avatar.photo,
            decoration = PlayerAvatarDecoration(MainPurple, 1.dp),
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = player.avatar.name,
            style = AppTextStyles.labelsScroll.copy(fontSize = 12.sp),
            color = AppWhite,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
        Text(
            text = "$voteCount",
            style = AppTextStyles.labelsScroll.copy(fontSize = 14.sp, fontWeight = FontWeight.Black),
            color = MarkerYellow
        )
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = MarkerYellow,
            backgroundColor = AppWhite.copy(alpha = 0.2f)
        )
    }
}

/** Horizontal scrolling retains recognisable, unclipped avatars even with many voters. */
@Composable
fun VoterAvatars(players: List<PlayerUi>, modifier: Modifier = Modifier) {
    if (players.isEmpty()) return
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val preferredWidth = 64.dp * players.size - 12.dp * (players.size - 1) + Spacing.small * 2
        LazyRow(Modifier.widthIn(max = maxWidth).width(preferredWidth),
            horizontalArrangement = Arrangement.spacedBy((-12).dp),
            contentPadding = PaddingValues(Spacing.small)) {
            items(players, key = { it.id }) { player ->
                Box(Modifier.size(64.dp).semantics { contentDescription = player.avatar.name }) {
                    PlayerAvatar(player.avatar.avatarRes, photo = player.avatar.photo,
                        decoration = PlayerAvatarDecoration(LightPurple, 1.dp), modifier = Modifier.size(64.dp))
                }
            }
        }
    }
}
