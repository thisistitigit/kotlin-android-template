package com.ncorti.kotlin.template.library.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ncorti.kotlin.template.library.data.local.entity.ContentEntity
import com.ncorti.kotlin.template.library.data.local.entity.ContentPackEntity
import com.ncorti.kotlin.template.library.data.local.entity.ContentSetEntity
import com.ncorti.kotlin.template.library.domain.enums.GameModeType

@Dao
interface ContentDao {

    @Insert
    suspend fun insertPack(pack: ContentPackEntity): Long

    @Insert
    suspend fun insertSet(set: ContentSetEntity): Long

    @Insert
    suspend fun insertContent(content: ContentEntity): Long

    @Query("SELECT * FROM content_packs ORDER BY name")
    suspend fun getPacks(): List<ContentPackEntity>

    @Query(
        """
        SELECT * FROM content_sets
        WHERE pack_id = :packId
        AND game_mode_type = :mode
        """
    )
    suspend fun getSets(
        packId: Int,
        mode: GameModeType
    ): List<ContentSetEntity>

    @Query(
        """
        SELECT * FROM contents
        WHERE content_set_id = :setId
        """
    )
    suspend fun getContentsForSet(
        setId: Int
    ): List<ContentEntity>

    @Query(
        """
        SELECT * FROM content_sets
        WHERE pack_id = :packId
        AND game_mode_type = :mode
        ORDER BY RANDOM()
        LIMIT 1
        """
    )
    suspend fun getRandomSet(
        packId: Int,
        mode: GameModeType
    ): ContentSetEntity?
}
