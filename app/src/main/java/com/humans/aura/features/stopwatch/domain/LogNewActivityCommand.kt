package com.humans.aura.features.stopwatch.domain

data class LogNewActivityCommand(
    val title: String,
    val timestampEpochMillis: Long,
)
