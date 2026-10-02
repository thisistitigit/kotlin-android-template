package com.impostor.library.data.repository

import com.impostor.library.data.local.dao.ContentDao
import com.impostor.library.data.local.entity.ContentEntity
import com.impostor.library.data.local.entity.ContentPackEntity
import com.impostor.library.data.local.entity.ContentSetEntity
import com.impostor.library.domain.enums.GameModeType

class ContentRepository(
    private val contentDao: ContentDao
) {

    suspend fun getPacks(): List<ContentPackEntity> =
        contentDao.getPacks()

    suspend fun getRandomContentSet(
        packId: Int,
        mode: GameModeType
    ): Pair<ContentSetEntity, List<ContentEntity>>? {

        val set = contentDao.getRandomSet(
            packId,
            mode
        ) ?: return null

        val contents =
            contentDao.getContentsForSet(set.id)

        return set to contents
    }
}
