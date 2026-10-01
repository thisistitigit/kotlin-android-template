package com.ncorti.kotlin.template.app.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ncorti.kotlin.template.app.ui.components.AppButton
import com.ncorti.kotlin.template.app.ui.components.AppButtonStyle
import com.ncorti.kotlin.template.app.ui.components.AppCard
import com.ncorti.kotlin.template.app.ui.components.SkipButton
import com.ncorti.kotlin.template.app.ui.theme.AppBlack
import com.ncorti.kotlin.template.app.ui.theme.AppWhite
import com.ncorti.kotlin.template.app.ui.theme.LightPurple
import com.ncorti.kotlin.template.app.ui.theme.Spacing
import com.ncorti.kotlin.template.app.ui.theme.TemplateTheme
import com.ncorti.kotlin.template.library.compose.R
import kotlinx.coroutines.delay

private data class OnboardingPage(
    @param:DrawableRes val background: Int,
    val title: String,
    val showsCard: Boolean
)

@Composable
fun OnboardingScreen(
    pageIndex: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pages = onboardingPages()
    val page = pages[pageIndex.coerceIn(pages.indices)]
    if (!page.showsCard) {
        LaunchedEffect(pageIndex) {
            delay(WELCOME_DURATION_MILLIS)
            onNext()
        }
    }
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(page.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(AppBlack.copy(alpha = BACKGROUND_OVERLAY_ALPHA)))
        if (page.showsCard) {
            OnboardingContent(pageIndex, page, pages.lastIndex, onNext, onSkip)
        } else {
            WelcomeContent()
        }
    }
}

@Composable
private fun WelcomeContent() {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(Spacing.large),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.welcome_to), style = MaterialTheme.typography.h5)
        Spacer(Modifier.height(Spacing.medium))
        Image(
            painter = painterResource(R.drawable.welcoming_logo),
            contentDescription = stringResource(R.string.impostor_logo_description),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun OnboardingContent(
    pageIndex: Int,
    page: OnboardingPage,
    lastIndex: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Box(Modifier.fillMaxWidth()) {
            SkipButton(onClick = onSkip, modifier = Modifier.align(Alignment.CenterEnd))
        }
        Spacer(Modifier.weight(1f))
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.primary
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.mask_minimal),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(Spacing.medium))
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.h5,
                    color = AppWhite,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(Spacing.large))
                PageIndicator(selectedIndex = pageIndex - 1)
            }
        }
        Spacer(Modifier.weight(1f))
        AppButton(
            text = if (pageIndex == lastIndex) stringResource(R.string.get_started) else stringResource(R.string.next),
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.extraLarge),
            style = AppButtonStyle.FROSTED,
            showShadow = true
        )
        Spacer(Modifier.height(Spacing.extraLarge))
    }
}

@Composable
private fun PageIndicator(selectedIndex: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
        repeat(ONBOARDING_CARD_COUNT) { index ->
            Box(
                Modifier
                    .size(width = if (index == selectedIndex) 24.dp else 12.dp, height = 5.dp)
                    .clip(CircleShape)
                    .background(if (index == selectedIndex) AppWhite else LightPurple.copy(alpha = 0.45f))
            )
        }
    }
}

@Composable
private fun onboardingPages() = listOf(
    OnboardingPage(R.drawable.onboarding_welcome, stringResource(R.string.welcome_to), false),
    OnboardingPage(R.drawable.onboarding_gather, stringResource(R.string.gather_friends), true),
    OnboardingPage(R.drawable.onboarding_choose, stringResource(R.string.choose_game), true),
    OnboardingPage(R.drawable.onboarding_fun, stringResource(R.string.have_fun), true)
)

@Preview(showBackground = true)
@Suppress("UnusedPrivateMember")
@Composable
internal fun OnboardingPreview() = TemplateTheme {
    OnboardingScreen(pageIndex = 1, onNext = {}, onSkip = {})
}

private const val BACKGROUND_OVERLAY_ALPHA = 0.5f
private const val WELCOME_DURATION_MILLIS = 1_500L
private const val ONBOARDING_CARD_COUNT = 3
