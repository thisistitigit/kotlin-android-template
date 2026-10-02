package com.impostor.library.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.impostor.library.data.local.entity.VoteEntity

@Dao
interface VoteDao {

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
        WHERE round_id = :roundId
        GROUP BY voted_game_player_id
        ORDER BY voteCount DESC
        """
    )
    suspend fun getVoteResults(
        roundId: Int
    ): List<VoteResult>
}

data class VoteResult(
    @ColumnInfo(name = "voted_game_player_id")
    val gamePlayerId: Int,

    @ColumnInfo(name = "voteCount")
    val votes: Int
)
