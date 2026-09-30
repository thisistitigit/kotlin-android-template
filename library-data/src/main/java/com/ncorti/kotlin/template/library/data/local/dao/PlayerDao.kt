package com.ncorti.kotlin.template.library.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ncorti.kotlin.template.library.data.local.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Insert
    suspend fun insert(player: PlayerEntity): Long

    @Update
    suspend fun update(player: PlayerEntity)

    @Delete
    suspend fun delete(player: PlayerEntity)

    @Query("SELECT * FROM players ORDER BY name")
    fun getAll(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE player_id = :id")
    suspend fun getById(id: Int): PlayerEntity?
}
