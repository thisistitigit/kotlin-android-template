package com.ncorti.kotlin.template.library.data.repository

import com.ncorti.kotlin.template.library.data.local.dao.PlayerDao
import com.ncorti.kotlin.template.library.data.local.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

class PlayerRepository(
    private val playerDao: PlayerDao
) {

    fun getPlayers(): Flow<List<PlayerEntity>> =
        playerDao.getAll()

    suspend fun getPlayer(id: Int): PlayerEntity? =
        playerDao.getById(id)

    suspend fun createPlayer(
        name: String,
        photoUri: String?
    ): Long {
        require(name.isNotBlank()) {
            "Player name cannot be empty."
        }

        return playerDao.insert(
            PlayerEntity(
                name = name.trim(),
                photoUri = photoUri
            )
        )
    }

    suspend fun updatePlayer(player: PlayerEntity) =
        playerDao.update(player)

    suspend fun deletePlayer(player: PlayerEntity) =
        playerDao.delete(player)
}
