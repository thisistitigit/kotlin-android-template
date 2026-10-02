package com.impostor.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable

private val Palette = darkColors(
    primary = MainPurple,
    primaryVariant = LightPurple,
    secondary = MarkerYellow,
    background = AppBackground,
    surface = AppBackground,
    onPrimary = AppWhite,
    onSecondary = AppBlack,
    onBackground = AppWhite,
    onSurface = AppWhite
)

@Composable
fun ImpostorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = Palette,
        typography = AppTypography,
        shapes = Shapes(
            small = RoundedCornerShape(Spacing.small),
            medium = RoundedCornerShape(Spacing.large),
            large = RoundedCornerShape(Spacing.extraLarge)
        ),
        content = content
    )
}
