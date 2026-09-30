package com.ncorti.kotlin.template.library.data.local

import androidx.room.TypeConverter
import com.ncorti.kotlin.template.library.domain.enums.ContentType
import com.ncorti.kotlin.template.library.domain.enums.GameModeType
import com.ncorti.kotlin.template.library.domain.enums.GameStatus
import com.ncorti.kotlin.template.library.domain.enums.RoleType
import com.ncorti.kotlin.template.library.domain.enums.RoundStatus
import com.ncorti.kotlin.template.library.domain.enums.ScoreReason

@Suppress("TooManyFunctions")
class Converters {

    @TypeConverter
    fun fromGameStatus(value: GameStatus) = value.name

    @TypeConverter
    fun toGameStatus(value: String) = GameStatus.valueOf(value)

    @TypeConverter
    fun fromRoundStatus(value: RoundStatus) = value.name

    @TypeConverter
    fun toRoundStatus(value: String) = RoundStatus.valueOf(value)

    @TypeConverter
    fun fromGameModeType(value: GameModeType) = value.name

    @TypeConverter
    fun toGameModeType(value: String) = GameModeType.valueOf(value)

    @TypeConverter
    fun fromRoleType(value: RoleType) = value.name

    @TypeConverter
    fun toRoleType(value: String) = RoleType.valueOf(value)

    @TypeConverter
    fun fromContentType(value: ContentType) = value.name

    @TypeConverter
    fun toContentType(value: String) = ContentType.valueOf(value)

    @TypeConverter
    fun fromScoreReason(value: ScoreReason) = value.name

    @TypeConverter
    fun toScoreReason(value: String) = ScoreReason.valueOf(value)
}
