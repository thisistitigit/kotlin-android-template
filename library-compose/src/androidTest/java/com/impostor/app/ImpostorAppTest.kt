package com.impostor.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.impostor.app.ui.ImpostorApp
import com.impostor.app.ui.theme.ImpostorTheme
import org.junit.Rule
import org.junit.Test

class ImpostorAppTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun skipOnboardingOpensNewGameAndAllowsModeSelection() {
        InstrumentationRegistry.getInstrumentation().targetContext
            .getSharedPreferences("impostor-preferences", android.content.Context.MODE_PRIVATE)
            .edit().clear().commit()
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent { ImpostorTheme { ImpostorApp() } }
        composeTestRule.mainClock.advanceTimeBy(SPLASH_AND_WELCOME_MILLIS)
        composeTestRule.onNodeWithText("Skip").performClick()

        composeTestRule.onNodeWithText("New Game").assertIsDisplayed()
        composeTestRule.onNodeWithText("Classic").performClick()
        composeTestRule.onNodeWithText("Continue").performClick()
        composeTestRule.onNodeWithText("Players?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Continue").performClick()
        composeTestRule.onNodeWithText("Nickname").assertIsDisplayed()
    }

    private companion object {
        const val SPLASH_AND_WELCOME_MILLIS = 3_000L
    }
}
