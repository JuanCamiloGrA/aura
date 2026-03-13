package com.humans.aura.features.stopwatch.domain

data class ActivityPrediction(
    val title: String,
    val occurrencesCount: Int,
    val lastSeenEpochMillis: Long,
)
