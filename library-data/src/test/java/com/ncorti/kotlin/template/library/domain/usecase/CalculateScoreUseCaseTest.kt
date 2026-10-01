package com.ncorti.kotlin.template.library.domain.usecase

import com.ncorti.kotlin.template.library.domain.enums.RoleType
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateScoreUseCaseTest {
    private val useCase = CalculateScoreUseCase()

    @Test
    fun discoveredMrWhiteWinsOnlyWhenSecretIsGuessed() {
        val result = useCase(
            roundId = 1,
            playerIds = setOf(1, 2, 3, 4),
            impostorIds = setOf(1),
            mrWhiteIds = setOf(2, 4),
            votes = mapOf(1 to 2, 3 to 2, 4 to 2),
            mrWhiteGuesserId = 2
        ).result

        assertEquals(setOf(RoleType.MR_WHITE), result.winningRoles)
        assertEquals(7, result.scores.getValue(2))
    }

    @Test
    fun scoresAccumulatePerRoundWithoutDependingOnRoundCount() {
        val result = useCase(
            roundId = 42,
            playerIds = setOf(1, 2, 3),
            impostorIds = setOf(1),
            mrWhiteIds = emptySet(),
            votes = mapOf(1 to 2, 2 to 1, 3 to 1)
        ).result

        assertEquals(setOf(RoleType.CIVILIAN), result.winningRoles)
        assertEquals(5, result.scores.getValue(2))
        assertEquals(5, result.scores.getValue(3))
    }
}
