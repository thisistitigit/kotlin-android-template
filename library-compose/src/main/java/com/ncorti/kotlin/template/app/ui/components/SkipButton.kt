package com.ncorti.kotlin.template.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.ncorti.kotlin.template.app.ui.theme.LightPurple
import com.ncorti.kotlin.template.app.ui.theme.Spacing

@Composable
fun SkipButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clickable(role = Role.Button, onClick = onClick).padding(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Skip", color = LightPurple, style = MaterialTheme.typography.body1)
        Text(text = "  ›", color = LightPurple, style = MaterialTheme.typography.h5)
    }
}
