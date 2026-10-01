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
        val assignments = useCase(setup(mrWhiteCount = 2))

        assertEquals(impostor.id, assignments.first { it.gamePlayerId == 1 }.roleId)
        val mrWhiteAssignments = assignments.filter { it.roleId == mrWhite.id }
        assertEquals(2, mrWhiteAssignments.size)
        mrWhiteAssignments.forEach { assertNull(it.contentId) }
        assertEquals(word.id, assignments.first { it.gamePlayerId == 4 }.contentId)
    }

    @Test
    fun disabledMrWhiteLeavesOnlyImpostorsAndCivilians() {
        val assignments = AssignRolesUseCase { it }(setup(mrWhiteCount = 0))
        assertEquals(1, assignments.count { it.roleId == impostor.id })
        assertEquals(0, assignments.count { it.roleId == mrWhite.id })
        assertEquals(3, assignments.count { it.roleId == civilian.id })
    }

    private fun setup(mrWhiteCount: Int) = RoundSetup(
        roundId = 1,
        players = (1..4).map { GamePlayerEntity(id = it, gameId = 1, playerId = it, seatOrder = it - 1) },
        impostorCount = 1,
        mrWhiteCount = mrWhiteCount,
        civilianRole = civilian,
        impostorRole = impostor,
        mrWhiteRole = mrWhite,
        civilianContent = word,
        impostorContent = word
    )
}
