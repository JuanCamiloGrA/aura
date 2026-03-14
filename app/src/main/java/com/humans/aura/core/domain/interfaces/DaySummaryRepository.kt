package com.humans.aura.core.domain.interfaces

import com.humans.aura.core.domain.models.DaySummary
import kotlinx.coroutines.flow.Flow

interface DaySummaryRepository {
    fun observeLatestSummary(): Flow<DaySummary?>

    fun observeRecentSummaries(limit: Int = 7): Flow<List<DaySummary>>

    suspend fun createPendingSummary(dayStartEpochMillis: Long): DaySummary

    suspend fun getPendingSummaries(limit: Int = 7): List<DaySummary>

    suspend fun updatePendingContext(
        summaryId: Long,
        rawContextJson: String,
        promptVersion: String,
        modelName: String,
        lastAttemptEpochMillis: Long,
    )

    suspend fun updateSummaryResult(
        summaryId: Long,
        summaryText: String,
        modelName: String,
        lastAttemptEpochMillis: Long,
    )

    suspend fun recordRetryableFailure(
        summaryId: Long,
        errorMessage: String,
        modelName: String,
        lastAttemptEpochMillis: Long,
    )

    suspend fun recordTerminalFailure(
        summaryId: Long,
        errorMessage: String,
        modelName: String,
        lastAttemptEpochMillis: Long,
    )
}
