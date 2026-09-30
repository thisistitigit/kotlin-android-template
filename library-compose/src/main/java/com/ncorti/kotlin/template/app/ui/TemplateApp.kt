package com.ncorti.kotlin.template.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ncorti.kotlin.template.app.ui.screens.HomeScreen
import com.ncorti.kotlin.template.app.ui.screens.OnboardingScreen
import com.ncorti.kotlin.template.app.ui.screens.SplashScreen
import com.ncorti.kotlin.template.library.domain.enums.GameModeType
import kotlinx.coroutines.delay

private enum class AppDestination { SPLASH, ONBOARDING, HOME }

@Composable
fun TemplateApp() {
    var destination by rememberSaveable { mutableStateOf(AppDestination.SPLASH) }
    var onboardingPage by rememberSaveable { mutableIntStateOf(0) }
    var selectedMode by rememberSaveable { mutableStateOf<GameModeType?>(null) }

    LaunchedEffect(destination) {
        if (destination == AppDestination.SPLASH) {
            delay(SPLASH_DURATION_MILLIS)
            destination = AppDestination.ONBOARDING
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
        when (destination) {
            AppDestination.SPLASH -> SplashScreen()
            AppDestination.ONBOARDING -> OnboardingScreen(
                pageIndex = onboardingPage,
                onNext = {
                    if (onboardingPage < LAST_ONBOARDING_PAGE) onboardingPage++ else destination = AppDestination.HOME
                },
                onSkip = { destination = AppDestination.HOME }
            )
            AppDestination.HOME -> HomeScreen(
                selectedMode = selectedMode,
                onModeSelected = { selectedMode = it },
                onContinue = { },
                modifier = Modifier.safeDrawingPadding()
            )
        }
    }
}

private const val SPLASH_DURATION_MILLIS = 1_200L
private const val LAST_ONBOARDING_PAGE = 3
