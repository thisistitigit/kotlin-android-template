package com.ncorti.kotlin.template.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ncorti.kotlin.template.app.ui.theme.AppBackground
import com.ncorti.kotlin.template.app.ui.theme.AppWhite
import com.ncorti.kotlin.template.app.ui.theme.FrostedWhite
import com.ncorti.kotlin.template.app.ui.theme.Spacing

@Suppress("LongParameterList")
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AppButtonStyle = AppButtonStyle.PRIMARY,
    showShadow: Boolean = false,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(percent = 50)
    val background = when (style) {
        AppButtonStyle.PRIMARY -> MaterialTheme.colors.primary
        AppButtonStyle.FROSTED -> FrostedWhite
        AppButtonStyle.OUTLINED -> AppBackground
    }
    val border = when (style) {
        AppButtonStyle.PRIMARY -> Color.Transparent
        AppButtonStyle.FROSTED, AppButtonStyle.OUTLINED -> AppWhite.copy(alpha = 0.75f)
    }
    Box(
        modifier = modifier
            .then(if (showShadow) Modifier.shadow(8.dp, shape) else Modifier)
            .defaultMinSize(minHeight = Spacing.minimumTouchTarget)
            .background(background.copy(alpha = if (enabled) background.alpha else 0.45f), shape)
            .border(BorderStroke(1.dp, border), shape)
            .semantics { role = Role.Button }
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = Spacing.large, vertical = Spacing.medium),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.button, color = AppWhite)
    }
}
