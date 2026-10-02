package com.impostor.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.impostor.app.ui.ImpostorApp
import com.impostor.app.ui.theme.ImpostorTheme
import org.junit.Rule
import org.junit.Test

class ImpostorAppTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun skipOnboardingOpensNewGameAndAllowsModeSelection() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent { ImpostorTheme { ImpostorApp() } }
        composeTestRule.mainClock.advanceTimeBy(SPLASH_AND_WELCOME_MILLIS)
        composeTestRule.onNodeWithText("Skip").performClick()

        composeTestRule.onNodeWithText("New Game").assertIsDisplayed()
        composeTestRule.onNodeWithText("Classic").performClick()
        composeTestRule.onNodeWithText("Continue").performClick()
        composeTestRule.onNodeWithText("Players?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Continue").performClick()
        composeTestRule.onNodeWithText("Impostors?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mr. White").assertIsDisplayed()
    }

    private companion object {
        const val SPLASH_AND_WELCOME_MILLIS = 3_000L
    }
}
