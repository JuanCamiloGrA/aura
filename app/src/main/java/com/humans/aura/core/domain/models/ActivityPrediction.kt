package com.humans.aura.core.domain.models

data class ActivityPrediction(
    val title: String,
    val occurrencesCount: Int,
    val lastSeenEpochMillis: Long,
)
