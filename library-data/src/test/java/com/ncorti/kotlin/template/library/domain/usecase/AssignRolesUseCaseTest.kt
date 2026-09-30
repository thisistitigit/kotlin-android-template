package com.ncorti.kotlin.template.library.domain.usecase

import com.ncorti.kotlin.template.library.data.local.entity.ContentEntity
import com.ncorti.kotlin.template.library.data.local.entity.GamePlayerEntity
import com.ncorti.kotlin.template.library.data.local.entity.RoleEntity
import com.ncorti.kotlin.template.library.domain.enums.ContentType
import com.ncorti.kotlin.template.library.domain.enums.RoleType
import com.ncorti.kotlin.template.library.domain.model.RoundSetup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AssignRolesUseCaseTest {
    private val civilian = RoleEntity(id = 1, name = "Civilian", type = RoleType.CIVILIAN)
    private val impostor = RoleEntity(id = 2, name = "Impostor", type = RoleType.IMPOSTOR)
    private val mrWhite = RoleEntity(id = 3, name = "Mr. White", type = RoleType.MR_WHITE)
    private val word = ContentEntity(id = 1, contentSetId = 1, text = "Tree", type = ContentType.NORMAL_WORD)

    @Test
    fun mrWhiteIsOptionalAndAlwaysReceivesBlankContent() {
        val useCase = AssignRolesUseCase { it }
        val assignments = useCase(setup(includeMrWhite = true))

        assertEquals(impostor.id, assignments.first { it.gamePlayerId == 1 }.roleId)
        val mrWhiteAssignment = assignments.first { it.gamePlayerId == 2 }
        assertEquals(mrWhite.id, mrWhiteAssignment.roleId)
        assertNull(mrWhiteAssignment.contentId)
        assertEquals(word.id, assignments.first { it.gamePlayerId == 3 }.contentId)
    }

    @Test
    fun disabledMrWhiteLeavesOnlyImpostorsAndCivilians() {
        val assignments = AssignRolesUseCase { it }(setup(includeMrWhite = false))
        assertEquals(1, assignments.count { it.roleId == impostor.id })
        assertEquals(0, assignments.count { it.roleId == mrWhite.id })
        assertEquals(3, assignments.count { it.roleId == civilian.id })
    }

    private fun setup(includeMrWhite: Boolean) = RoundSetup(
        roundId = 1,
        players = (1..4).map { GamePlayerEntity(id = it, gameId = 1, playerId = it, seatOrder = it - 1) },
        impostorCount = 1,
        includeMrWhite = includeMrWhite,
        civilianRole = civilian,
        impostorRole = impostor,
        mrWhiteRole = mrWhite,
        civilianContent = word,
        impostorContent = word
    )
}
