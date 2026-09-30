package com.ncorti.kotlin.template.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ncorti.kotlin.template.app.ui.theme.MarkerYellow
import com.ncorti.kotlin.template.app.ui.theme.Spacing

@Composable
fun GameModeCircle(
    label: String,
    @DrawableRes iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.semantics { this.selected = selected },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(164.dp)
                .shadow(if (selected) 8.dp else 0.dp, CircleShape)
                .background(if (selected) MarkerYellow else Color.Transparent, CircleShape)
                .border(4.dp, MaterialTheme.colors.onBackground, CircleShape)
                .clickable(role = Role.RadioButton, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(112.dp)
            )
        }
        Spacer(Modifier.height(Spacing.medium))
        Text(text = label, style = MaterialTheme.typography.h5)
    }
}
