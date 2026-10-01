package com.ncorti.kotlin.template.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ncorti.kotlin.template.app.ui.theme.AppWhite

@Composable
fun BackButton(contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(38.dp)
            .semantics { this.contentDescription = contentDescription }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(12.dp, 20.dp)) {
            drawLine(
                AppWhite,
                Offset(size.width, 0f),
                Offset(0f, size.height / 2f),
                STROKE.toPx(),
                StrokeCap.Round
            )
            drawLine(
                AppWhite,
                Offset(0f, size.height / 2f),
                Offset(size.width, size.height),
                STROKE.toPx(),
                StrokeCap.Round
            )
        }
    }
}

private val STROKE = 3.dp
