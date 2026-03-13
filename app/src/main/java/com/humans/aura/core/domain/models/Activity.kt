package com.humans.aura.core.domain.models

data class Activity(
    val id: Long,
    val title: String,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long?,
    val status: ActivityStatus,
    val isSyncedToD1: Boolean,
)
