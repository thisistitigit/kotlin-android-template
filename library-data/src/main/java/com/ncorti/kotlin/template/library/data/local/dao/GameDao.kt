package com.ncorti.kotlin.template.library.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ncorti.kotlin.template.library.data.local.entity.GameEntity
import com.ncorti.kotlin.template.library.data.local.entity.GameModeEntity
import com.ncorti.kotlin.template.library.data.local.entity.GamePlayerEntity
import com.ncorti.kotlin.template.library.data.local.relation.GameWithPlayers
import com.ncorti.kotlin.template.library.domain.enums.GameModeType
import com.ncorti.kotlin.template.library.domain.enums.GameStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Insert
    suspend fun insertGame(game: GameEntity): Long

    @Insert
    suspend fun insertGamePlayers(players: List<GamePlayerEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertModes(modes: List<GameModeEntity>)

    @Query("SELECT * FROM game_modes WHERE type = :type LIMIT 1")
    suspend fun getMode(type: GameModeType): GameModeEntity?

    @Transaction
    @Query("SELECT * FROM games WHERE game_id = :gameId")
    suspend fun getGameWithPlayers(gameId: Int): GameWithPlayers?

    @Query("SELECT * FROM games WHERE game_id = :gameId")
    suspend fun getGame(gameId: Int): GameEntity?

    @Query("SELECT * FROM games ORDER BY created_at DESC")
    fun observeGames(): Flow<List<GameEntity>>

    @Query("UPDATE games SET status = :status WHERE game_id = :gameId")
    suspend fun updateStatus(gameId: Int, status: GameStatus)

    @Query("UPDATE games SET status = :status, finished_at = :finishedAt WHERE game_id = :gameId")
    suspend fun finishGame(
        gameId: Int,
        status: GameStatus = GameStatus.FINISHED,
        finishedAt: Long = System.currentTimeMillis()
    )
}
