package com.impostor.app.ui.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.impostor.app.ui.components.AppButton
import com.impostor.app.ui.components.AppButtonStyle
import com.impostor.app.ui.components.AppCard
import com.impostor.app.ui.components.AppHeader
import com.impostor.app.ui.theme.AppBlack
import com.impostor.app.ui.theme.AppWhite
import com.impostor.app.ui.theme.LightPurple
import com.impostor.app.ui.theme.Spacing
import com.impostor.app.ui.theme.ImpostorTheme
import com.impostor.library.compose.R
import kotlinx.coroutines.delay

private data class OnboardingPage(
    @param:DrawableRes val background: Int,
    @param:DrawableRes val titleRes: Int? = null,
    @param:StringRes val descriptionRes: Int? = null,
    @param:DrawableRes val illustrationRes: Int? = null,
    val hasCard: Boolean = true,
    val isWelcome: Boolean = false
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

    if (page.isWelcome) {
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

        if (page.isWelcome) {
            WelcomeContent()
        } else {
            OnboardingContent(pageIndex, page, pages.lastIndex, onNext, onSkip)
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
        AppHeader(onSkip = onSkip, modifier = Modifier.padding(horizontal = Spacing.medium))

        Spacer(Modifier.weight(1f))

        if (page.hasCard) {
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.primary
            ) {
                PageCenterContent(pageIndex = pageIndex, page = page)
            }
        } else {
            PageCenterContent(pageIndex = pageIndex, page = page)
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
private fun PageCenterContent(
    pageIndex: Int,
    page: OnboardingPage
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        page.illustrationRes?.let { illustration ->
            Image(
                painter = painterResource(illustration),
                contentDescription = null,
                modifier = Modifier.size(if (page.hasCard) 64.dp else 120.dp)
            )
            Spacer(Modifier.height(Spacing.medium))
        }

        page.titleRes?.let { titleXmlRes ->
            Image(
                painter = painterResource(titleXmlRes),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.medium)
            )
            Spacer(Modifier.height(Spacing.medium))
        }

        page.descriptionRes?.let { descRes ->
            Text(
                text = stringResource(descRes),
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.SansSerif,
                color = AppWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.large)
            )
            Spacer(Modifier.height(Spacing.large))
        }

        PageIndicator(selectedIndex = pageIndex - 1)
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
    OnboardingPage(
        background = R.drawable.welcome_picture,
        isWelcome = true
    ),
    OnboardingPage(
        background = R.drawable.onboarding_gather,
        titleRes = R.drawable.title_1,
        descriptionRes = R.string.onboarding_desc_1,
        illustrationRes = R.drawable.impostor,
        hasCard = false
    ),
    OnboardingPage(
        background = R.drawable.onboarding_gather,
        titleRes = R.drawable.title_2,
        descriptionRes = R.string.onboarding_desc_2,
        illustrationRes = R.drawable.mr_white,
        hasCard = false
    ),
    OnboardingPage(
        background = R.drawable.onboarding_gather,
        titleRes = R.drawable.title_3,
        // descriptionRes = R.string.onboarding_desc_3,
        illustrationRes = R.drawable.win,
        hasCard = false
    )
)

@Preview(showBackground = true)
@Suppress("UnusedPrivateMember")
@Composable
internal fun OnboardingPreview() = ImpostorTheme {
    OnboardingScreen(pageIndex = 1, onNext = {}, onSkip = {})
}

private const val BACKGROUND_OVERLAY_ALPHA = 0.5f
private const val WELCOME_DURATION_MILLIS = 1_500L
private const val ONBOARDING_CARD_COUNT = 3
