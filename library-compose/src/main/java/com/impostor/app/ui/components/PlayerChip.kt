package com.impostor.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.impostor.app.ui.screens.TurnPlayerUi
import com.impostor.app.ui.theme.AppTextStyles
import com.impostor.app.ui.theme.AppWhite
import com.impostor.app.ui.theme.MainPurple
import com.impostor.app.ui.theme.MarkerYellow
import com.impostor.app.ui.theme.Spacing
import com.impostor.library.compose.R

@Composable
fun PlayerChip(player: TurnPlayerUi, playerNumber: Int, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        PlayerAvatar(player.avatarRes, photo = player.photo,
            decoration = PlayerAvatarDecoration(MarkerYellow, 1.dp), modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(Spacing.small))
        Text(stringResource(R.string.player_number, playerNumber), style = MaterialTheme.typography.body2,
            color = AppWhite)
        Spacer(Modifier.width(Spacing.small))
        Text(player.name, style = AppTextStyles.labelsScroll, color = MainPurple,
            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 180.dp))
    }
}
