package com.impostor.library.domain.model

import com.impostor.library.data.local.entity.ScoreEventEntity

data class ScoreCalculation(
    val result: RoundResult,
    val events: List<ScoreEventEntity>
)
