package com.impostor.app.ui.screens

import android.content.Context
import com.impostor.library.domain.enums.GameModeType
import com.impostor.library.domain.enums.QuestionCategory
import org.json.JSONArray
import org.json.JSONObject

/** Small, non-secret navigation checkpoint. Room remains authoritative for roles, votes and scores. */
internal class GameSessionStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "impostor-session", Context.MODE_PRIVATE)

    fun saveLaunch(key: String, setup: GameFlowSetup) {
        val players = JSONArray()
        setup.players.forEach { player ->
            players.put(JSONObject().put("name", player.name)
                .put("avatar", PLAYER_AVATARS.indexOf(player.avatarRes))
                .put("photo", player.photoUri))
        }
        preferences.edit().putString("launch", JSONObject()
            .put("key", key).put("mode", setup.mode.name)
            .put("impostors", setup.impostors).put("whites", setup.whites)
            .put("players", players).toString()).apply()
    }

    fun readLaunch(): SavedLaunch? = runCatching {
        val raw = preferences.getString("launch", null) ?: return null
        val objectValue = JSONObject(raw)
        val array = objectValue.getJSONArray("players")
        val players = (0 until array.length()).map { index ->
            val player = array.getJSONObject(index)
            val avatarIndex = player.getInt("avatar")
            require(avatarIndex in PLAYER_AVATARS.indices)
            TurnPlayerUi(player.getString("name"), PLAYER_AVATARS[avatarIndex],
                photoUri = player.optString("photo").takeIf { it.isNotBlank() && it != "null" })
        }
        SavedLaunch(objectValue.getString("key"), GameFlowSetup(
            GameModeType.valueOf(objectValue.getString("mode")),
            objectValue.getInt("impostors"), objectValue.getInt("whites"), players))
    }.getOrNull()

    fun saveCheckpoint(checkpoint: GameCheckpoint) {
        preferences.edit().putString("checkpoint", JSONObject()
            .put("key", checkpoint.key).put("gameId", checkpoint.gameId)
            .put("roundId", checkpoint.roundId).put("previousRoundId", checkpoint.previousRoundId)
            .put("currentId", checkpoint.currentPlayerId)
            .put("viewed", JSONArray(checkpoint.viewedIds))
            .put("destination", checkpoint.destination.name)
            .put("mode", checkpoint.mode?.name)
            .put("category", checkpoint.category?.name).toString()).apply()
    }

    fun readCheckpoint(key: String): GameCheckpoint? = runCatching {
        val raw = preferences.getString("checkpoint", null) ?: return null
        val objectValue = JSONObject(raw)
        if (objectValue.getString("key") != key) return null
        val viewed = objectValue.getJSONArray("viewed")
        val savedDestination = objectValue.getString("destination")
        GameCheckpoint(key, objectValue.getInt("gameId"),
            objectValue.optInt("roundId", -1).takeIf { it > 0 },
            objectValue.optInt("previousRoundId", -1).takeIf { it > 0 },
            objectValue.optInt("currentId", -1).takeIf { it > 0 },
            (0 until viewed.length()).map(viewed::getInt),
            if (savedDestination in setOf("CONTENT_SETUP", "ROUND_MODE_SELECTION"))
                GameFlowDestination.PREPARING
                else GameFlowDestination.valueOf(savedDestination),
            objectValue.optString("mode").takeIf(String::isNotBlank)?.let(GameModeType::valueOf),
            objectValue.optString("category").takeIf(String::isNotBlank)?.let(QuestionCategory::valueOf))
    }.getOrNull()

    fun clearCheckpoint() {
        preferences.edit().remove("checkpoint").apply()
    }

    fun clearSession() {
        preferences.edit().remove("checkpoint").remove("launch").apply()
    }
}

internal data class SavedLaunch(val key: String, val setup: GameFlowSetup)

internal data class GameCheckpoint(val key: String, val gameId: Int,
    val roundId: Int?, val previousRoundId: Int?, val currentPlayerId: Int?,
    val viewedIds: List<Int>, val destination: GameFlowDestination,
    val mode: GameModeType?, val category: QuestionCategory?)
