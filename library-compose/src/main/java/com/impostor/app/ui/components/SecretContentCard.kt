@file:Suppress("MagicNumber")

package com.impostor.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.impostor.app.ui.theme.AppBackground
import com.impostor.app.ui.theme.AppTextStyles
import com.impostor.app.ui.theme.MainPurple
import com.impostor.app.ui.theme.Spacing

@Composable
fun SecretContentCard(
    text: String,
    isWord: Boolean,
    modifier: Modifier = Modifier
) {
    if (isWord) {
        Box(
            modifier = modifier
                .padding(horizontal = Spacing.medium)
                .rotate(WORD_BUBBLE_ROTATION),
            contentAlignment = Alignment.Center
        ) {
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MainPurple,
                contentPadding = Spacing.large
            ) {
                Text(
                    text = text,
                    style = AppTextStyles.title,
                    color = AppBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    } else {
        // Question Bubble with tail
        Column(
            modifier = modifier.padding(horizontal = Spacing.medium),
            horizontalAlignment = Alignment.Start
        ) {
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MainPurple,
                contentPadding = Spacing.large
            ) {
                Text(
                    text = text,
                    style = AppTextStyles.labelsScroll,
                    color = AppBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.large) // Force 2 lines behavior
                )
            }
            // Bubble Tail
            Canvas(
                modifier = Modifier
                    .offset(x = 24.dp, y = (-2).dp)
                    .size(24.dp, 20.dp)
            ) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = MainPurple)
            }
        }
    }
}

private const val WORD_BUBBLE_ROTATION = -7f
