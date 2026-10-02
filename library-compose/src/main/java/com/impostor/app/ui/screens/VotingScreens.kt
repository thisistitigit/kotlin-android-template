@file:Suppress("MagicNumber")

package com.impostor.app.ui.screens

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.impostor.app.ui.components.GameScreenLayout
import com.impostor.app.ui.components.GameActionButton
import com.impostor.app.ui.components.PlayerAvatar
import com.impostor.app.ui.components.PlayerChip
import com.impostor.app.ui.components.PrivacyCard
import com.impostor.app.ui.components.VoteCandidateCard
import com.impostor.app.ui.components.VoteResultCard
import com.impostor.app.ui.components.VoterAvatars
import com.impostor.app.ui.theme.AppTextStyles
import com.impostor.app.ui.theme.AppWhite
import com.impostor.app.ui.theme.DeepPurple
import com.impostor.app.ui.theme.LightPurple
import com.impostor.app.ui.theme.MainPurple
import com.impostor.app.ui.theme.MarkerYellow
import com.impostor.app.ui.theme.Spacing
import com.impostor.library.compose.R
import com.impostor.library.domain.enums.RoleType

@Composable
internal fun VotingAsset(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val resource = remember(name, context) { resolveVotingAsset(context, name) }
    Box(modifier.heightIn(min = 160.dp, max = 320.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
        if (resource != 0) {
            Image(painterResource(resource), null, contentScale = ContentScale.Fit,
                modifier = Modifier.sizeIn(maxWidth = 320.dp, maxHeight = 320.dp).fillMaxWidth())
        } else {
            Text(stringResource(R.string.asset_placeholder, "$name.png"), color = LightPurple,
                textAlign = TextAlign.Center)
        }
    }
}

@Suppress("DiscouragedApi")
private fun resolveVotingAsset(context: Context, name: String): Int {
    return try {
        val id = context.resources.getIdentifier(name, "drawable", context.packageName)
        if (id == 0) return 0
        val value = TypedValue()
        context.resources.getValue(id, value, true)
        if (value.string?.toString()?.endsWith(".png") == true) id else 0
    } catch (_: Resources.NotFoundException) { 0 }
}

@Composable
internal fun VotingTitle(text: String, color: Color = MainPurple) {
    Text(text, style = AppTextStyles.title, color = color, textAlign = TextAlign.Center)
}

@Composable
fun VoteIntroScreen(player: PlayerUi, onBack: () -> Unit, onVote: () -> Unit, modifier: Modifier = Modifier) {
    GameScreenLayout(onBack, modifier, contentArrangement = Arrangement.spacedBy(Spacing.medium), bottomAction = { GameActionButton(stringResource(R.string.vote), onClick = onVote) }) {
        PlayerChip(player.avatar, player.number)
        VotingTitle(stringResource(R.string.vote))
        Text(stringResource(R.string.voting_your_turn), style = MaterialTheme.typography.body1, color = AppWhite)
        VotingAsset("pointing_cat")
        PrivacyCard(stringResource(R.string.dont_show_vote), Modifier.fillMaxWidth())
    }
}

@Composable
fun VoteSelectionScreen(state: VotingUiState, onBack: () -> Unit, onToggle: (Int) -> Unit,
    onConfirm: () -> Unit, modifier: Modifier = Modifier) {
    GameScreenLayout(onBack, modifier, scrollContent = false,
        contentArrangement = Arrangement.spacedBy(Spacing.medium), bottomAction = { GameActionButton(stringResource(R.string.confirm_vote), onConfirm,
            enabled = state.selectedId != null && !state.busy) }) {
        val title = if (state.target == RoleType.MR_WHITE) R.string.who_is_mr_white else R.string.who_is_impostor
        VotingTitle(stringResource(title), AppWhite)
        
        LazyVerticalGrid(
            columns = GridCells.Adaptive(120.dp),
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
            contentPadding = PaddingValues(Spacing.small)
        ) {
            items(state.candidates, key = { it.id }) { player ->
                VoteCandidateCard(
                    player = player,
                    selected = state.selectedId == player.id,
                    enabled = !state.busy,
                    onClick = { onToggle(player.id) }
                )
            }
        }
        
    }
}

@Composable
fun VoteLockedScreen(player: PlayerUi, nextPlayer: PlayerUi?, onBack: () -> Unit, onGotIt: () -> Unit,
    modifier: Modifier = Modifier) {
    GameScreenLayout(onBack, modifier, DeepPurple,
        contentArrangement = Arrangement.spacedBy(Spacing.medium), bottomAction = { GameActionButton(stringResource(R.string.got_it), onClick = onGotIt) }) {
        PlayerChip(player.avatar, player.number)
        VotingTitle(stringResource(R.string.vote_locked))
        VotingAsset("locked_cat")
        Text(stringResource(if (nextPlayer == null) R.string.all_votes_locked else R.string.pass_phone_label),
            style = MaterialTheme.typography.body1, color = AppWhite, textAlign = TextAlign.Center)
        nextPlayer?.let { VotingTitle(it.avatar.name, MarkerYellow) }
    }
}

@Composable
fun VoteResultsScreen(state: VoteResultUiState, onBack: () -> Unit, onReveal: () -> Unit,
    modifier: Modifier = Modifier) {
    GameScreenLayout(onBack, modifier, contentArrangement = Arrangement.spacedBy(Spacing.medium), bottomAction = {
        GameActionButton(stringResource(if (state.isTie) R.string.start_runoff else R.string.reveal_role),
            onClick = onReveal)
    }) {
        VotingTitle(stringResource(if (state.isTie) R.string.voting_tie else R.string.votes_are_in))
        
        state.selectedPlayer?.let { player ->
            Text(state.voteCount.toString(), style = AppTextStyles.chosenNumber, color = AppWhite)
            PlayerAvatar(player.avatar.avatarRes, photo = player.avatar.photo, modifier = Modifier.size(160.dp))
            VotingTitle(player.avatar.name, AppWhite)
            VoterAvatars(state.voters, Modifier.fillMaxWidth())
        }
        Text(stringResource(R.string.votes_against), style = AppTextStyles.labelsScroll, color = AppWhite)
        
        // Grid of results with sliders
        LazyVerticalGrid(
            columns = GridCells.Adaptive(110.dp),
            modifier = Modifier.heightIn(max = 500.dp).fillMaxWidth(),
            contentPadding = PaddingValues(Spacing.small),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            items(state.candidates, key = { it.id }) { candidate ->
                val count = state.allCounts[candidate.id] ?: 0
                VoteResultCard(
                    player = candidate,
                    voteCount = count,
                    totalVotes = state.totalVotes
                )
            }
        }

        if (state.isTie) {
            Text(stringResource(R.string.runoff_explanation), color = AppWhite, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun VoteRoleRevealScreen(state: VoteRoleRevealUiState, onBack: () -> Unit,
    onContinue: () -> Unit, modifier: Modifier = Modifier) {
    RoleRevealScreen(state, onBack, onContinue, modifier)
}
