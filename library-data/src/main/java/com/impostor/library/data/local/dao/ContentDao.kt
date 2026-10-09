package com.impostor.library.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.impostor.library.data.local.entity.ContentEntity
import com.impostor.library.data.local.entity.ContentPackEntity
import com.impostor.library.data.local.entity.ContentSetEntity
import com.impostor.library.domain.enums.GameModeType

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

    @Query("SELECT * FROM content_packs WHERE name = :name AND is_custom = 0 LIMIT 1")
    suspend fun getBuiltInPack(name: String): ContentPackEntity?

    @Query("SELECT * FROM content_sets WHERE description = :key AND pack_id = :packId LIMIT 1")
    suspend fun getBuiltInSet(packId: Int, key: String): ContentSetEntity?

    @Query("""SELECT cs.* FROM content_sets cs JOIN content_packs cp ON cp.pack_id = cs.pack_id
        WHERE cp.is_custom = 0 AND cs.game_mode_type = :mode
        AND ((:category IS NULL AND cs.category IS NULL) OR cs.category = :category)""")
    suspend fun getBuiltInSets(mode: GameModeType, category: String?): List<ContentSetEntity>

    @Query("SELECT content_set_id FROM rounds WHERE game_id = :gameId AND content_set_id IS NOT NULL")
    suspend fun getUsedSetIds(gameId: Int): List<Int>

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
