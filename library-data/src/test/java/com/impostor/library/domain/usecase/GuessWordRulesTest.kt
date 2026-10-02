package com.impostor.library.domain.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GuessWordRulesTest {
    @Test fun matchesFullWordsAndNormalizesWhitespaceCaseAndUnicode() {
        assertTrue(GuessWordRules.matches("  BLUE   moon ", "Blue moon"))
        assertTrue(GuessWordRules.matches("cafe\u0301", "café"))
        assertFalse(GuessWordRules.matches("moon", "Blue moon"))
        assertFalse(GuessWordRules.matches("cafe", "café"))
    }
}
