package com.humans.aura.core.services.database

data class ActivityPredictionEntity(
    val title: String,
    val occurrencesCount: Int,
    val lastSeenEpochMillis: Long,
)
