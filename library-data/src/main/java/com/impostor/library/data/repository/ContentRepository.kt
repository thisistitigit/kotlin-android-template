package com.impostor.library.data.repository

import com.impostor.library.data.local.dao.ContentDao
import com.impostor.library.data.local.entity.ContentEntity
import com.impostor.library.data.local.entity.ContentPackEntity
import com.impostor.library.data.local.entity.ContentSetEntity
import com.impostor.library.domain.enums.GameModeType
import com.impostor.library.domain.enums.QuestionCategory
import com.impostor.library.domain.model.BuiltInCatalog

class ContentRepository(
    private val contentDao: ContentDao
) {
    /** Called within the parent game transaction. Existing seed rows survive app upgrades. */
    suspend fun selectBuiltInContent(mode: GameModeType, category: QuestionCategory?,
        gameId: Int? = null): Pair<ContentEntity, ContentEntity> {
        require((mode == GameModeType.QUESTION) == (category != null))
        val packName = "Impostor built-in v1"
        val packId = contentDao.getBuiltInPack(packName)?.id ?:
            contentDao.insertPack(ContentPackEntity(name = packName, isCustom = false, language = "en")).toInt()
        BuiltInCatalog.entries.forEach { entry ->
            if (contentDao.getBuiltInSet(packId, entry.key) == null) {
                val setId = contentDao.insertSet(ContentSetEntity(packId = packId,
                    description = entry.key, gameModeType = entry.mode, category = entry.category?.key)).toInt()
                val question = entry.mode == GameModeType.QUESTION
                contentDao.insertContent(ContentEntity(contentSetId = setId, text = entry.civilian,
                    type = if (question) com.impostor.library.domain.enums.ContentType.NORMAL_QUESTION
                    else com.impostor.library.domain.enums.ContentType.NORMAL_WORD))
                contentDao.insertContent(ContentEntity(contentSetId = setId, text = entry.impostor,
                    type = if (question) com.impostor.library.domain.enums.ContentType.IMPOSTOR_QUESTION
                    else com.impostor.library.domain.enums.ContentType.IMPOSTOR_WORD))
            }
        }
        val available = contentDao.getBuiltInSets(mode, category?.key)
        require(available.isNotEmpty()) { "No content for this mode and category." }
        val used = gameId?.let { contentDao.getUsedSetIds(it).toSet() }.orEmpty()
        val unused = available.filterNot { it.id in used }
        val chosen = (unused.ifEmpty { available }).random()
        val contents = contentDao.getContentsForSet(chosen.id)
        val civilianType = if (mode == GameModeType.QUESTION)
            com.impostor.library.domain.enums.ContentType.NORMAL_QUESTION
        else com.impostor.library.domain.enums.ContentType.NORMAL_WORD
        val impostorType = if (mode == GameModeType.QUESTION)
            com.impostor.library.domain.enums.ContentType.IMPOSTOR_QUESTION
        else com.impostor.library.domain.enums.ContentType.IMPOSTOR_WORD
        return requireNotNull(contents.singleOrNull { it.type == civilianType }) to
            requireNotNull(contents.singleOrNull { it.type == impostorType })
    }
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
