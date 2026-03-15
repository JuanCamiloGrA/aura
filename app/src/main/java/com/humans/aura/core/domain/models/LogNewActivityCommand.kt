package com.humans.aura.core.domain.models

data class LogNewActivityCommand(
    val title: String,
    val timestampEpochMillis: Long,
)
