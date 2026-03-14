package com.humans.aura.features.day_summary.presentation

import app.cash.turbine.test
import com.humans.aura.MainDispatcherRule
import com.humans.aura.core.domain.interfaces.DaySummaryRepository
import com.humans.aura.core.domain.models.DaySummary
import com.humans.aura.core.domain.models.SummaryGenerationStatus
import com.humans.aura.features.day_summary.domain.ObserveLatestSummaryUseCase
import com.humans.aura.features.day_summary.domain.ObserveRecentSummariesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DaySummaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun ui_state_exposes_latest_and_recent_summaries() = runTest {
        val latest = fakeSummary(id = 1, text = "A focused day")
        val recent = listOf(latest, fakeSummary(id = 2, text = "A reflective day"))
        val repository = FakeDaySummaryRepository(latest, recent)
        val viewModel = DaySummaryViewModel(
            observeLatestSummaryUseCase = ObserveLatestSummaryUseCase(repository),
            observeRecentSummariesUseCase = ObserveRecentSummariesUseCase(repository),
        )
        advanceUntilIdle()

        viewModel.uiState.test {
            val item = awaitItem()
            if (!item.isLoading) {
                assertEquals("A focused day", item.latestSummary?.summaryText)
                assertEquals(2, item.recentSummaries.size)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    private fun fakeSummary(id: Long, text: String): DaySummary = DaySummary(
        id = id,
        dayStartEpochMillis = id * 100L,
        summaryText = text,
        rawContextJson = "{}",
        promptVersion = "v1",
        modelName = "gemini-test",
        generationStatus = SummaryGenerationStatus.COMPLETED,
        errorMessage = null,
        lastAttemptEpochMillis = 1L,
        createdAtEpochMillis = 1L,
        updatedAtEpochMillis = 1L,
        isSyncedToD1 = false,
    )

    private class FakeDaySummaryRepository(
        latest: DaySummary?,
        recent: List<DaySummary>,
    ) : DaySummaryRepository {
        private val latestFlow = MutableStateFlow(latest)
        private val recentFlow = MutableStateFlow(recent)

        override fun observeLatestSummary(): Flow<DaySummary?> = latestFlow
        override fun observeRecentSummaries(limit: Int): Flow<List<DaySummary>> = recentFlow
        override suspend fun createPendingSummary(dayStartEpochMillis: Long): DaySummary = error("unused")
        override suspend fun getPendingSummaries(limit: Int): List<DaySummary> = emptyList()
        override suspend fun updatePendingContext(summaryId: Long, rawContextJson: String, promptVersion: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
        override suspend fun updateSummaryResult(summaryId: Long, summaryText: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
        override suspend fun recordRetryableFailure(summaryId: Long, errorMessage: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
        override suspend fun recordTerminalFailure(summaryId: Long, errorMessage: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
    }
}
