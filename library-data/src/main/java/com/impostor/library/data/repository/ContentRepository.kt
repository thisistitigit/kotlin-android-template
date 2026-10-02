package com.impostor.library.data.repository

import com.impostor.library.data.local.dao.ContentDao
import com.impostor.library.data.local.entity.ContentEntity
import com.impostor.library.data.local.entity.ContentPackEntity
import com.impostor.library.data.local.entity.ContentSetEntity
import com.impostor.library.domain.enums.GameModeType

class ContentRepository(
    private val contentDao: ContentDao
) {
    suspend fun saveCustomContent(mode: GameModeType, normal: String, adversary: String,
        category: com.impostor.library.domain.enums.QuestionCategory? = null): Pair<ContentEntity, ContentEntity> {
        require(normal.isNotBlank() && adversary.isNotBlank())
        require(category == null || mode == GameModeType.QUESTION)
        val pack = contentDao.insertPack(ContentPackEntity(name = "Custom", isCustom = true)).toInt()
        val set = contentDao.insertSet(ContentSetEntity(packId = pack, gameModeType = mode, category = category?.key)).toInt()
        val question = mode == GameModeType.QUESTION
        val civilian = ContentEntity(contentSetId = set, text = normal.trim(), type =
            if (question) com.impostor.library.domain.enums.ContentType.NORMAL_QUESTION
            else com.impostor.library.domain.enums.ContentType.NORMAL_WORD)
        val impostor = ContentEntity(contentSetId = set, text = adversary.trim(), type =
            if (question) com.impostor.library.domain.enums.ContentType.IMPOSTOR_QUESTION
            else com.impostor.library.domain.enums.ContentType.IMPOSTOR_WORD)
        return civilian.copy(id = contentDao.insertContent(civilian).toInt()) to
            impostor.copy(id = contentDao.insertContent(impostor).toInt())
    }

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
