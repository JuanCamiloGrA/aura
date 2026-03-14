package com.humans.aura.core.domain.models

data class DaySummary(
    val id: Long,
    val dayStartEpochMillis: Long,
    val summaryText: String?,
    val rawContextJson: String,
    val promptVersion: String,
    val modelName: String,
    val generationStatus: SummaryGenerationStatus,
    val errorMessage: String?,
    val lastAttemptEpochMillis: Long?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val isSyncedToD1: Boolean,
)
