@file:Suppress("MagicNumber")

package com.impostor.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.impostor.app.ui.theme.ImpostorTheme

private fun previewPlayers() = List(8) {
    PlayerUi(it + 1, it + 1, TurnPlayerUi("Player ${it + 1}", PLAYER_AVATARS[it]))
}

@Preview @Composable
internal fun VoteIntroPreview() = ImpostorTheme { VoteIntroScreen(previewPlayers()[0], {}, {}) }

@Preview(widthDp = 320, heightDp = 480) @Composable
internal fun VoteSmallPreview() = ImpostorTheme { VoteIntroScreen(previewPlayers()[0], {}, {}) }

@Preview @Composable
internal fun VoteSelectionPreview() = ImpostorTheme {
    VoteSelectionScreen(VotingUiState(candidates = previewPlayers(), selectedId = 2), {}, {}, {})
}

@Preview @Composable
internal fun VoteLockedPreview() = ImpostorTheme { VoteLockedScreen(previewPlayers()[0], previewPlayers()[1], {}, {}) }

@Preview @Composable
internal fun VoteResultsPreview() = ImpostorTheme {
    VoteResultsScreen(VoteResultUiState(previewPlayers()[1], 4, previewPlayers().take(4), false, 1), {}, {})
}

@Preview @Composable
internal fun VoteTiePreview() = ImpostorTheme {
    VoteResultsScreen(VoteResultUiState(null, 3, emptyList(), true, 2, previewPlayers().take(2)), {}, {})
}

