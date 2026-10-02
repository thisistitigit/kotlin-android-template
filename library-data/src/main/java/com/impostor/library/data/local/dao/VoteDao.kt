package com.impostor.library.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.impostor.library.data.local.entity.VoteEntity
import com.impostor.library.data.local.entity.VotingPhaseEntity

@Dao
interface VoteDao {

    @Insert
    suspend fun insertPhase(phase: VotingPhaseEntity): Long

    @Query("SELECT * FROM voting_phases WHERE round_id = :roundId ORDER BY phase_id")
    suspend fun getPhases(roundId: Int): List<VotingPhaseEntity>

    @Query("SELECT * FROM voting_phases WHERE phase_id = :phaseId")
    suspend fun getPhase(phaseId: Int): VotingPhaseEntity?

    @androidx.room.Update
    suspend fun updatePhase(phase: VotingPhaseEntity)

    @Query("SELECT * FROM votes WHERE phase_id = :phaseId ORDER BY vote_id")
    suspend fun getPhaseVotes(phaseId: Int): List<VoteEntity>

    @Insert
    suspend fun insert(vote: VoteEntity): Long

    @Query(
        """
        SELECT * FROM votes
        WHERE round_id = :roundId
        """
    )
    suspend fun getVotes(roundId: Int): List<VoteEntity>

    @Query(
        """
        SELECT voted_game_player_id, COUNT(*) AS voteCount
        FROM votes
        WHERE phase_id = :phaseId
        GROUP BY voted_game_player_id
        ORDER BY voteCount DESC
        """
    )
    suspend fun getVoteResults(
        phaseId: Int
    ): List<VoteResult>
}

data class VoteResult(
    @ColumnInfo(name = "voted_game_player_id")
    val gamePlayerId: Int,

    @ColumnInfo(name = "voteCount")
    val votes: Int
)
