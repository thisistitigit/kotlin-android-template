package com.ncorti.kotlin.template.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ncorti.kotlin.template.app.ui.components.AppButton
import com.ncorti.kotlin.template.app.ui.components.AppButtonStyle
import com.ncorti.kotlin.template.app.ui.components.GameModeCircle
import com.ncorti.kotlin.template.app.ui.theme.Spacing
import com.ncorti.kotlin.template.app.ui.theme.TemplateTheme
import com.ncorti.kotlin.template.library.compose.R
import com.ncorti.kotlin.template.library.domain.enums.GameModeType

@Composable
fun HomeScreen(
    selectedMode: GameModeType?,
    onModeSelected: (GameModeType) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.text_logo),
            contentDescription = stringResource(R.string.impostor_logo_description),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(R.string.new_game),
            style = MaterialTheme.typography.h3,
            modifier = Modifier.align(Alignment.Start).padding(top = Spacing.large)
        )
        Spacer(Modifier.height(Spacing.extraLarge))
        GameModeCircle(
            label = stringResource(R.string.classic),
            iconRes = R.drawable.classic,
            selected = selectedMode == GameModeType.CLASSIC,
            onClick = { onModeSelected(GameModeType.CLASSIC) }
        )
        Spacer(Modifier.height(Spacing.extraLarge))
        GameModeCircle(
            label = stringResource(R.string.questions),
            iconRes = R.drawable.questions,
            selected = selectedMode == GameModeType.QUESTION,
            onClick = { onModeSelected(GameModeType.QUESTION) }
        )
        Spacer(Modifier.height(Spacing.extraLarge))
        AppButton(
            text = stringResource(R.string.continue_label),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.large),
            style = AppButtonStyle.OUTLINED,
            showShadow = true,
            enabled = selectedMode != null
        )
        Spacer(Modifier.height(Spacing.large))
    }
}

@Preview(showBackground = true)
@Suppress("UnusedPrivateMember")
@Composable
private fun HomePreview() = TemplateTheme {
    HomeScreen(GameModeType.CLASSIC, {}, {})
}
