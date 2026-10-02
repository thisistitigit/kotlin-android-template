package com.impostor.library.domain.enums

/** Stable keys persisted with content sets, independent of translated labels or carousel order. */
enum class QuestionCategory(val key: String) {
    ACTORS("actors"), MOVIES("movies"), NBA("nba"), FOOTBALL("football"),
    SERIES("series"), SINGERS("singers")
}
