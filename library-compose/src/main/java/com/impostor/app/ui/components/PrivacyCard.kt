package com.impostor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.impostor.app.ui.theme.AppBackground
import com.impostor.app.ui.theme.AppWhite
import com.impostor.app.ui.theme.DeepPurple
import com.impostor.app.ui.theme.MainPurple
import com.impostor.app.ui.theme.Spacing
import com.composables.icons.lucide.R as LucideR

@Composable
fun PrivacyCard(text: String, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier, backgroundColor = DeepPurple, contentPadding = Spacing.large) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
            Box(Modifier.size(72.dp).background(MainPurple.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center) {
                Icon(painterResource(LucideR.drawable.lucide_ic_lock_keyhole), null,
                    tint = AppBackground, modifier = Modifier.size(40.dp))
            }
            Text(text, style = MaterialTheme.typography.body1, color = AppWhite, textAlign = TextAlign.Center)
        }
    }
}
