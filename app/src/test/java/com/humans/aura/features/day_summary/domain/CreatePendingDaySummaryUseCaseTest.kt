package com.humans.aura.features.day_summary.domain

import com.humans.aura.core.domain.interfaces.DaySummaryRepository
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.DaySummary
import com.humans.aura.core.domain.models.SummaryGenerationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CreatePendingDaySummaryUseCaseTest {

    @Test
    fun invoke_creates_pending_summary_for_current_day() = runTest {
        val repository = FakeDaySummaryRepository()

        val summary = CreatePendingDaySummaryUseCase(
            daySummaryRepository = repository,
            timeProvider = object : TimeProvider {
                override fun currentTimeMillis(): Long = 999L
                override fun currentDayStartEpochMillis(): Long = 123L
            },
        ).invoke()

        assertEquals(123L, repository.createdDayStart)
        assertEquals(SummaryGenerationStatus.PENDING, summary.generationStatus)
    }

    private class FakeDaySummaryRepository : DaySummaryRepository {
        var createdDayStart: Long? = null

        override fun observeLatestSummary(): Flow<DaySummary?> = emptyFlow()
        override fun observeRecentSummaries(limit: Int): Flow<List<DaySummary>> = emptyFlow()

        override suspend fun createPendingSummary(dayStartEpochMillis: Long): DaySummary {
            createdDayStart = dayStartEpochMillis
            return DaySummary(
                id = 1L,
                dayStartEpochMillis = dayStartEpochMillis,
                summaryText = null,
                reflection = null,
                rawContextJson = "{}",
                promptVersion = "v1",
                modelName = "pending",
                generationStatus = SummaryGenerationStatus.PENDING,
                errorMessage = null,
                lastAttemptEpochMillis = null,
                createdAtEpochMillis = 1L,
                updatedAtEpochMillis = 1L,
                isSyncedToD1 = false,
            )
        }

        override suspend fun getPendingSummaries(limit: Int): List<DaySummary> = emptyList()
        override suspend fun updatePendingContext(summaryId: Long, rawContextJson: String, promptVersion: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
        override suspend fun updateSummaryResult(summaryId: Long, summaryText: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
        override suspend fun recordRetryableFailure(summaryId: Long, errorMessage: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
        override suspend fun recordTerminalFailure(summaryId: Long, errorMessage: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
    }
}
