package com.impostor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.impostor.app.ui.theme.AppBackground
import com.impostor.app.ui.theme.Spacing

/** One action baseline for the whole game flow, independent of content length or screen height. */
@Composable
fun GameScreenLayout(
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    background: Color = AppBackground,
    scrollContent: Boolean = true,
    contentPadding: Dp = Spacing.medium,
    contentArrangement: Arrangement.Vertical = Arrangement.Top,
    bottomAction: (@Composable () -> Unit)? = null,
    secondaryAction: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier.fillMaxSize().background(background).safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = GAME_MAX_WIDTH).fillMaxWidth().fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            AppHeader(onBack = onBack, modifier = Modifier.padding(horizontal = Spacing.medium))
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().padding(horizontal = contentPadding)) {
                val body = if (scrollContent) Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight) else Modifier.fillMaxSize()
                Column(body.padding(vertical = Spacing.medium),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = contentArrangement, content = content)
            }
            secondaryAction?.invoke()
            Spacer(Modifier.height(Spacing.medium))
            bottomAction?.let { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { it() } }
            Spacer(Modifier.height(Spacing.extraLarge))
        }
    }
}

/** Shared dimensions and styling; callers only choose the action and its visual variant. */
@Composable
fun GameActionButton(text: String, onClick: () -> Unit, enabled: Boolean = true,
    style: AppButtonStyle = AppButtonStyle.SECONDARY) {
    AppButton(text, onClick, enabled = enabled, style = style, showShadow = true,
        modifier = Modifier.width(GAME_ACTION_WIDTH))
}

private val GAME_MAX_WIDTH = 600.dp
private val GAME_ACTION_WIDTH = 172.dp
