package com.impostor.library.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.impostor.library.data.local.dao.AnswerDao
import com.impostor.library.data.local.dao.ContentDao
import com.impostor.library.data.local.dao.GameDao
import com.impostor.library.data.local.dao.PlayerDao
import com.impostor.library.data.local.dao.RoundDao
import com.impostor.library.data.local.dao.ScoreDao
import com.impostor.library.data.local.dao.VoteDao
import com.impostor.library.data.local.entity.AnswerEntity
import com.impostor.library.data.local.entity.ContentEntity
import com.impostor.library.data.local.entity.ContentPackEntity
import com.impostor.library.data.local.entity.ContentSetEntity
import com.impostor.library.data.local.entity.GameEntity
import com.impostor.library.data.local.entity.GameModeEntity
import com.impostor.library.data.local.entity.GamePlayerEntity
import com.impostor.library.data.local.entity.PlayerEntity
import com.impostor.library.data.local.entity.RoleEntity
import com.impostor.library.data.local.entity.RoundAssignmentEntity
import com.impostor.library.data.local.entity.RoundEntity
import com.impostor.library.data.local.entity.ScoreEventEntity
import com.impostor.library.data.local.entity.VoteEntity

@Database(
    entities = [
        PlayerEntity::class,
        GameModeEntity::class,
        GameEntity::class,
        GamePlayerEntity::class,
        RoundEntity::class,
        RoleEntity::class,
        ContentPackEntity::class,
        ContentSetEntity::class,
        ContentEntity::class,
        RoundAssignmentEntity::class,
        AnswerEntity::class,
        VoteEntity::class,
        ScoreEventEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun gameDao(): GameDao
    abstract fun roundDao(): RoundDao
    abstract fun contentDao(): ContentDao
    abstract fun answerDao(): AnswerDao
    abstract fun voteDao(): VoteDao
    abstract fun scoreDao(): ScoreDao
}
