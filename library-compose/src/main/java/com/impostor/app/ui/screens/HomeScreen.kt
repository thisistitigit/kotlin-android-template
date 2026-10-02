@file:Suppress("MagicNumber")

package com.impostor.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.impostor.app.ui.components.AppButton
import com.impostor.app.ui.components.AppButtonStyle
import com.impostor.app.ui.components.AppHeader
import com.impostor.app.ui.components.GameModeCircle
import com.impostor.app.ui.components.GameModeIcons
import com.impostor.app.ui.components.TitleBanner
import com.impostor.app.ui.theme.AppBackground
import com.impostor.app.ui.theme.ImpostorTheme
import com.impostor.app.ui.theme.Spacing
import com.impostor.library.compose.R
import com.impostor.library.domain.enums.GameModeType

@Composable
fun HomeScreen(
    selectedMode: GameModeType?,
    onModeSelected: (GameModeType) -> Unit,
     onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        val screenHeight = maxHeight
        val isCompact = screenHeight < COMPACT_HOME_HEIGHT
        val isVeryCompact = screenHeight < VERY_COMPACT_HOME_HEIGHT

        val bannerHeight = when {
            isVeryCompact -> 96.dp
            isCompact -> 116.dp
            else -> 140.dp
        }
        val circleSize = when {
            isVeryCompact -> 110.dp
            isCompact -> 130.dp
            else -> 154.dp
        }
        val bottomSpacing = when {
            isVeryCompact -> Spacing.small
            isCompact -> Spacing.medium
            else -> Spacing.large
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppHeader(
                modifier = Modifier.padding(horizontal = Spacing.medium)
            )
            Spacer(Modifier.weight(0.5f))
            TitleBanner(
                text = stringResource(R.string.new_game),
                modifier = Modifier.fillMaxWidth(),
                height = bannerHeight
            )

            Spacer(Modifier.weight(1f))

            GameModeCircle(
                label = stringResource(R.string.classic),
                icons = GameModeIcons(R.drawable.classic, R.drawable.classic_not_selected),
                selected = selectedMode == GameModeType.CLASSIC,
                onClick = { onModeSelected(GameModeType.CLASSIC) },
                circleSize = circleSize
            )

            Spacer(Modifier.height(Spacing.small))

            GameModeCircle(
                label = stringResource(R.string.questions),
                icons = GameModeIcons(R.drawable.questions, R.drawable.questions_not_selected),
                selected = selectedMode == GameModeType.QUESTION,
                onClick = { onModeSelected(GameModeType.QUESTION) },
                circleSize = circleSize
            )

            Spacer(Modifier.weight(1.2f))

            AppButton(
                text = stringResource(R.string.play_label),
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.large),
                style = AppButtonStyle.OUTLINED,
                showShadow = true,
                enabled = selectedMode != null
            )

            Spacer(Modifier.height(bottomSpacing))
        }
    }
}

private val COMPACT_HOME_HEIGHT = 720.dp
private val VERY_COMPACT_HOME_HEIGHT = 620.dp

@Preview(showBackground = true)
@Suppress("UnusedPrivateMember")
@Composable
internal fun HomePreview() = ImpostorTheme {
    HomeScreen(GameModeType.CLASSIC, {}, {})
}
