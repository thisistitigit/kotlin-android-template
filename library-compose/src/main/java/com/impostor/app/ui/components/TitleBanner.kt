package com.impostor.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.impostor.app.ui.theme.AppTextStyles
import com.impostor.app.ui.theme.AppWhite
import com.impostor.app.ui.theme.MainPurple

@Composable
fun TitleBanner(
    text: String,
    modifier: Modifier = Modifier,
    height: Dp = DEFAULT_BANNER_HEIGHT,
    backgroundColor: Color = MainPurple,
    oval: Boolean = false,
    content: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
            .height(height),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.matchParentSize()) {
            val topEdge = size.height * TOP_EDGE_RATIO
            val topControl = size.height * TOP_CONTROL_RATIO
            val bottomControl = size.height * BOTTOM_CONTROL_RATIO
            val path = Path().apply {
                moveTo(0f, topEdge)
                quadraticTo(size.width / 2f, topControl, size.width, topEdge)
                lineTo(size.width, if (oval) size.height * OVAL_EDGE_RATIO else size.height)
                quadraticTo(size.width / 2f, if (oval) size.height * OVAL_CONTROL_RATIO else bottomControl,
                    0f, if (oval) size.height * OVAL_EDGE_RATIO else size.height)
                close()
            }
            drawPath(path, backgroundColor)
        }
        if (content != null) content() else if (text.isNotEmpty()) {
            Text(text = text, style = AppTextStyles.title, color = AppWhite)
        }
    }
}
private val DEFAULT_BANNER_HEIGHT = 150.dp
private const val TOP_EDGE_RATIO = 0.2f
private const val TOP_CONTROL_RATIO = -0.2f
private const val BOTTOM_CONTROL_RATIO = 0.68f

private const val OVAL_EDGE_RATIO = 0.8f
private const val OVAL_CONTROL_RATIO = 1.2f
