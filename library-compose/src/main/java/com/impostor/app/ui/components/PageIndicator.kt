package com.impostor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.impostor.app.ui.theme.AppWhite
import com.impostor.app.ui.theme.LightPurple
import com.impostor.app.ui.theme.Spacing

@Composable
fun PageIndicator(count: Int, selectedIndex: Int, modifier: Modifier = Modifier,
    selectedColor: Color = AppWhite, idleColor: Color = LightPurple) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
        repeat(count) { index ->
            Box(Modifier.size(if (index == selectedIndex) 24.dp else 6.dp, 6.dp)
                .clip(CircleShape).background(if (index == selectedIndex) selectedColor else idleColor))
        }
    }
}
