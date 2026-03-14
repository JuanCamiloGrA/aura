package com.humans.aura.features.day_closure.domain

import com.humans.aura.core.domain.interfaces.SyncScheduler
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.interfaces.WallpaperController
import com.humans.aura.core.domain.models.AppIntent
import com.humans.aura.core.domain.models.DaySummary
import com.humans.aura.core.domain.models.SummaryGenerationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class HandleSleepIntentUseCaseTest {

    @Test
    fun invoke_sets_wallpaper_and_schedules_sync() = runTest {
        val daySummaryRepository = FakeDaySummaryRepository()
        val wallpaperController = FakeWallpaperController()
        val syncScheduler = FakeSyncScheduler()
        val timeProvider = FakeTimeProvider()

        HandleSleepIntentUseCase(
            daySummaryRepository = daySummaryRepository,
            timeProvider = timeProvider,
            wallpaperController = wallpaperController,
            syncScheduler = syncScheduler,
        ).invoke(AppIntent.SleepLogged("Sleep", 1L))

        assertEquals(1, daySummaryRepository.calls)
        assertEquals(123L, daySummaryRepository.createdDayStart)
        assertEquals(1, wallpaperController.calls)
        assertEquals(1, syncScheduler.calls)
    }

    private class FakeDaySummaryRepository : com.humans.aura.core.domain.interfaces.DaySummaryRepository {
        var calls = 0
        var createdDayStart: Long? = null

        override fun observeLatestSummary(): Flow<DaySummary?> = emptyFlow()

        override fun observeRecentSummaries(limit: Int): Flow<List<DaySummary>> = emptyFlow()

        override suspend fun createPendingSummary(dayStartEpochMillis: Long): DaySummary {
            calls += 1
            createdDayStart = dayStartEpochMillis
            return DaySummary(
                id = 1L,
                dayStartEpochMillis = dayStartEpochMillis,
                summaryText = null,
                rawContextJson = "{}",
                promptVersion = "v1",
                modelName = "pending",
                generationStatus = SummaryGenerationStatus.PENDING,
                errorMessage = null,
                lastAttemptEpochMillis = null,
                createdAtEpochMillis = dayStartEpochMillis,
                updatedAtEpochMillis = dayStartEpochMillis,
                isSyncedToD1 = false,
            )
        }

        override suspend fun getPendingSummaries(limit: Int): List<DaySummary> = emptyList()
        override suspend fun updatePendingContext(summaryId: Long, rawContextJson: String, promptVersion: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
        override suspend fun updateSummaryResult(summaryId: Long, summaryText: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
        override suspend fun recordRetryableFailure(summaryId: Long, errorMessage: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
        override suspend fun recordTerminalFailure(summaryId: Long, errorMessage: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
    }

    private class FakeTimeProvider : TimeProvider {
        override fun currentTimeMillis(): Long = 999L
        override fun currentDayStartEpochMillis(): Long = 123L
    }

    private class FakeWallpaperController : WallpaperController {
        var calls = 0

        override suspend fun setNightModeWallpaper() {
            calls += 1
        }
    }

    private class FakeSyncScheduler : SyncScheduler {
        var calls = 0

        override fun scheduleDayClosureSync() {
            calls += 1
        }
    }
}
