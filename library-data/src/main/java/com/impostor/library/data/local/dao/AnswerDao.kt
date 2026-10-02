package com.impostor.library.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.impostor.library.data.local.entity.AnswerEntity

@Dao
interface AnswerDao {

    @Insert
    suspend fun insert(answer: AnswerEntity): Long

    @Query("SELECT * FROM answers WHERE round_id = :roundId")
    suspend fun getAnswers(roundId: Int): List<AnswerEntity>
}
