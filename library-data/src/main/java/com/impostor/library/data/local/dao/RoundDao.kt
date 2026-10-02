package com.impostor.library.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.impostor.library.data.local.entity.RoleEntity
import com.impostor.library.data.local.entity.RoundAssignmentEntity
import com.impostor.library.data.local.entity.RoundEntity
import com.impostor.library.data.local.relation.RoundAssignmentDetails
import com.impostor.library.domain.enums.RoleType
import com.impostor.library.domain.enums.RoundStatus

@Dao
@Suppress("TooManyFunctions")
interface RoundDao {
    @Insert
    suspend fun insertRound(round: RoundEntity): Long

    @Insert
    suspend fun insertAssignments(assignments: List<RoundAssignmentEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRoles(roles: List<RoleEntity>)

    @Query("SELECT * FROM roles WHERE type = :type LIMIT 1")
    suspend fun getRole(type: RoleType): RoleEntity?

    @Query("SELECT * FROM rounds WHERE game_id = :gameId ORDER BY round_number")
    suspend fun getRounds(gameId: Int): List<RoundEntity>

    @Query("SELECT COALESCE(MAX(round_number), 0) + 1 FROM rounds WHERE game_id = :gameId")
    suspend fun getNextRoundNumber(gameId: Int): Int

    @Query("SELECT * FROM rounds WHERE round_id = :roundId")
    suspend fun getRound(roundId: Int): RoundEntity?

    @Transaction
    @Query("SELECT * FROM round_assignments WHERE round_id = :roundId")
    suspend fun getAssignments(roundId: Int): List<RoundAssignmentDetails>

    @Query("SELECT EXISTS(SELECT 1 FROM round_assignments WHERE round_id = :roundId AND game_player_id = :playerId)")
    suspend fun isPlayerInRound(roundId: Int, playerId: Int): Boolean

    @Query("UPDATE rounds SET status = :status WHERE round_id = :roundId")
    suspend fun updateStatus(roundId: Int, status: RoundStatus)

    @Query("UPDATE rounds SET status = :status, finished_at = :finishedAt WHERE round_id = :roundId")
    suspend fun finishRound(
        roundId: Int,
        status: RoundStatus = RoundStatus.FINISHED,
        finishedAt: Long = System.currentTimeMillis()
    )
}
