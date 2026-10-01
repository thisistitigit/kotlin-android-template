package com.ncorti.kotlin.template.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.R as LucideR
import com.ncorti.kotlin.template.app.ui.theme.AppWhite
import com.ncorti.kotlin.template.app.ui.theme.LightPurple
import com.ncorti.kotlin.template.app.ui.theme.Spacing
import com.ncorti.kotlin.template.library.compose.R

@Composable
fun AppHeader(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onSkip: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxWidth().height(HEADER_HEIGHT),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.text_logo),
            contentDescription = stringResource(R.string.impostor_logo_description),
            contentScale = ContentScale.Fit,
            modifier = Modifier.width(HEADER_LOGO_WIDTH).height(44.dp)
        )
        onBack?.let { callback ->
            IconButton(
                onClick = callback,
                modifier = Modifier.align(Alignment.CenterStart).size(Spacing.minimumTouchTarget)
            ) {
                Icon(
                    painter = painterResource(LucideR.drawable.lucide_ic_arrow_left),
                    contentDescription = stringResource(R.string.back),
                    tint = AppWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        onSkip?.let { callback ->
            Row(
                modifier = Modifier.align(Alignment.CenterEnd)
                    .clickable(role = Role.Button, onClick = callback)
                    .padding(vertical = Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.skip), color = LightPurple)
                Icon(
                    painter = painterResource(LucideR.drawable.lucide_ic_chevron_right),
                    contentDescription = null,
                    tint = LightPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private val HEADER_HEIGHT = 56.dp
private val HEADER_LOGO_WIDTH = 178.dp
