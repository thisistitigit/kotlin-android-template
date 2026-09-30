package com.ncorti.kotlin.template.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

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

private val AppTypography = Typography(
    h3 = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.ExtraBold),
    h4 = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.ExtraBold),
    h5 = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
    button = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
    body1 = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium)
)

@Composable
fun TemplateTheme(content: @Composable () -> Unit) {
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
