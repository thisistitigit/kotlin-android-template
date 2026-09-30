package com.ncorti.kotlin.template.library.data.repository

import com.ncorti.kotlin.template.library.data.local.dao.ContentDao
import com.ncorti.kotlin.template.library.data.local.entity.ContentEntity
import com.ncorti.kotlin.template.library.data.local.entity.ContentPackEntity
import com.ncorti.kotlin.template.library.data.local.entity.ContentSetEntity
import com.ncorti.kotlin.template.library.domain.enums.GameModeType

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
