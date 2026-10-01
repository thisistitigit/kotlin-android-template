package com.ncorti.kotlin.template.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.ncorti.kotlin.template.app.ui.theme.AppTextStyles
import com.ncorti.kotlin.template.app.ui.theme.AppWhite
import com.ncorti.kotlin.template.app.ui.theme.MainPurple

@Composable
fun TitleBanner(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().height(BANNER_HEIGHT),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.matchParentSize()) {
            val curve = size.height * CURVE_RATIO
            val path = Path().apply {
                moveTo(0f, curve)
                quadraticTo(size.width / 2f, -curve, size.width, curve)
                lineTo(size.width, size.height - curve)
                quadraticTo(size.width / 2f, size.height + curve, 0f, size.height - curve)
                close()
            }
            drawPath(path, MainPurple)
        }
        Text(text = text, style = AppTextStyles.title, color = AppWhite)
    }
}

private val BANNER_HEIGHT = 150.dp
private const val CURVE_RATIO = 0.2f
