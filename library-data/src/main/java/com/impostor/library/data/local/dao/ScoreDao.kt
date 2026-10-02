package com.impostor.library.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.impostor.library.data.local.entity.ScoreEventEntity

@Dao
interface ScoreDao {
    @Insert
    suspend fun insertAll(events: List<ScoreEventEntity>)

    @Query("SELECT * FROM score_events WHERE round_id = :roundId")
    suspend fun getRoundScores(roundId: Int): List<ScoreEventEntity>

    @Query("SELECT COUNT(*) FROM score_events WHERE round_id = :roundId")
    suspend fun countRoundScores(roundId: Int): Int

    @Query(
        """
        SELECT gp.game_player_id, p.name, COALESCE(SUM(se.points), 0) AS total_score
        FROM game_players gp
        JOIN players p ON p.player_id = gp.player_id
        LEFT JOIN score_events se ON se.game_player_id = gp.game_player_id
        WHERE gp.game_id = :gameId
        GROUP BY gp.game_player_id, p.name
        ORDER BY total_score DESC, p.name ASC
        """
    )
    suspend fun getGameClassification(gameId: Int): List<PlayerScore>
}

data class PlayerScore(
    @ColumnInfo(name = "game_player_id") val gamePlayerId: Int,
    val name: String,
    @ColumnInfo(name = "total_score") val totalScore: Int
)
