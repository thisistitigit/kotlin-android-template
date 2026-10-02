package com.impostor.library.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Immutable electorate/candidate snapshots keep runoff history independent. */
@Entity(
    tableName = "voting_phases",
    foreignKeys = [ForeignKey(
        entity = RoundEntity::class, parentColumns = ["round_id"], childColumns = ["round_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("round_id")]
)
data class VotingPhaseEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "phase_id") val id: Int = 0,
    @ColumnInfo(name = "round_id") val roundId: Int,
    val target: String,
    val voters: String,
    val candidates: String,
    val status: String = "OPEN",
    @ColumnInfo(name = "selected_player_id") val selectedPlayerId: Int? = null,
    @ColumnInfo(name = "pending_handoff") val pendingHandoff: Boolean = false,
    @ColumnInfo(name = "guess_text") val guessText: String? = null,
    @ColumnInfo(name = "guess_correct") val guessCorrect: Boolean? = null
)
