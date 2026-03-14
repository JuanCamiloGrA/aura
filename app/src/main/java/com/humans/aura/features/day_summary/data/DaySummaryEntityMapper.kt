package com.humans.aura.features.day_summary.data

import com.humans.aura.core.domain.models.DaySummary
import com.humans.aura.core.domain.models.SummaryGenerationStatus
import com.humans.aura.core.services.database.entity.summary.DailySummaryEntity

fun DailySummaryEntity.toDomain(): DaySummary = DaySummary(
    id = id,
    dayStartEpochMillis = dayStartEpochMillis,
    summaryText = summaryText,
    rawContextJson = rawContextJson,
    promptVersion = promptVersion,
    modelName = modelName,
    generationStatus = SummaryGenerationStatus.valueOf(generationStatus),
    errorMessage = errorMessage,
    lastAttemptEpochMillis = lastAttemptEpochMillis,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    isSyncedToD1 = isSyncedToD1,
)
