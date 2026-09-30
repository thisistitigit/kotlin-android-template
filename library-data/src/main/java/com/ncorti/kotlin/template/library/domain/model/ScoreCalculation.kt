package com.ncorti.kotlin.template.library.domain.model

import com.ncorti.kotlin.template.library.data.local.entity.ScoreEventEntity

data class ScoreCalculation(
    val result: RoundResult,
    val events: List<ScoreEventEntity>
)
