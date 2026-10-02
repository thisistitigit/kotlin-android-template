package com.impostor.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.impostor.app.ui.theme.AppWhite
import com.impostor.app.ui.theme.DisabledMode
import com.impostor.app.ui.theme.LightPurple
import com.impostor.app.ui.theme.Spacing

@Composable
fun GameModeCircle(
    label: String,
    icons: GameModeIcons,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    circleSize: Dp = DEFAULT_CIRCLE_SIZE
) {
    val iconSize = (circleSize.value * ICON_TO_CIRCLE_RATIO).dp
    val spacerHeight = if (circleSize < DEFAULT_CIRCLE_SIZE) Spacing.small else Spacing.medium

    Column(
        modifier = modifier.semantics { this.selected = selected },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(circleSize)
                .shadow(if (selected) 8.dp else 4.dp, CircleShape)
                .background(if (selected) LightPurple else DisabledMode, CircleShape)
                .clickable(role = Role.RadioButton, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(if (selected) icons.selected else icons.unselected),
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
        }
        Spacer(Modifier.height(spacerHeight))
        Text(text = label, style = MaterialTheme.typography.h5)
    }
}

private val DEFAULT_CIRCLE_SIZE = 164.dp
private const val ICON_TO_CIRCLE_RATIO = 112f / 164f
