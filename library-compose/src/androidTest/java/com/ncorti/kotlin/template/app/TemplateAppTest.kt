package com.ncorti.kotlin.template.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ncorti.kotlin.template.app.ui.TemplateApp
import com.ncorti.kotlin.template.app.ui.theme.TemplateTheme
import org.junit.Rule
import org.junit.Test

class TemplateAppTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun skipOnboardingOpensNewGameAndAllowsModeSelection() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent { TemplateTheme { TemplateApp() } }
        composeTestRule.mainClock.advanceTimeBy(SPLASH_AND_WELCOME_MILLIS)
        composeTestRule.onNodeWithText("Skip").performClick()

        composeTestRule.onNodeWithText("New Game").assertIsDisplayed()
        composeTestRule.onNodeWithText("Classic").performClick()
        composeTestRule.onNodeWithText("Continue").assertIsDisplayed()
    }

    private companion object {
        const val SPLASH_AND_WELCOME_MILLIS = 3_000L
    }
}
