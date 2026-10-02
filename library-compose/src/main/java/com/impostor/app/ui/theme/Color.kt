@file:Suppress("MagicNumber")

package com.impostor.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val MainPurple = Color(0xFF897CFF)
val LightPurple = Color(0xFFD0CBFF)
val AppBackground = Color(0xFF222222)
val MarkerYellow = Color(0xFFE2F163)
val AppWhite = Color(0xFFFFFFFF)
val AppBlack = Color(0xFF000000)
val FrostedWhite = Color(0x66FFFFFF)
val DisabledMode = Color(0xFF4A4A4A)
val DeepPurple = Color(0xFF292238)
val Transparent = Color(0x00000000)

val NicknameGradient = Brush.horizontalGradient(
    colorStops = arrayOf(
        0.034f to Color(0xFFBABABA),
        0.2745f to AppWhite.copy(alpha = 0.60f),
        0.3947f to AppWhite.copy(alpha = 0.65f),
        0.515f to AppWhite.copy(alpha = 0.80f),
        0.6352f to AppWhite.copy(alpha = 0.88f),
        0.7555f to AppWhite.copy(alpha = 0.90f),
        0.996f to AppWhite
    )
)
