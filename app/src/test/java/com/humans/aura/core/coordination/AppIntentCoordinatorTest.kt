package com.humans.aura.core.coordination

import com.humans.aura.MainDispatcherRule
import com.humans.aura.core.domain.interfaces.DaySummaryRepository
import com.humans.aura.core.domain.interfaces.IntentMediator
import com.humans.aura.core.domain.interfaces.SyncScheduler
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.interfaces.WallpaperController
import com.humans.aura.core.domain.models.AppIntent
import com.humans.aura.core.domain.models.DaySummary
import com.humans.aura.core.domain.models.SummaryGenerationStatus
import com.humans.aura.features.day_closure.domain.HandleSleepIntentUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppIntentCoordinatorTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun start_handles_sleep_intents_once() = runTest {
        val mediator = FakeIntentMediator()
        val daySummaryRepository = FakeDaySummaryRepository()
        val coordinator = AppIntentCoordinator(
            intentMediator = mediator,
            handleSleepIntentUseCase = HandleSleepIntentUseCase(
                daySummaryRepository = daySummaryRepository,
                timeProvider = FakeTimeProvider(),
                wallpaperController = FakeWallpaperController(),
                syncScheduler = FakeSyncScheduler(),
            ),
            appScope = CoroutineScope(coroutineContext),
        )

        coordinator.start()
        coordinator.start()
        advanceUntilIdle()
        mediator.emit(AppIntent.SleepLogged("Sleep", 100L))
        advanceUntilIdle()

        assertEquals(1, daySummaryRepository.pendingCalls)
        coordinator.stop()
    }

    @Test
    fun stop_before_start_is_safe() = runTest {
        val coordinator = AppIntentCoordinator(
            intentMediator = FakeIntentMediator(),
            handleSleepIntentUseCase = HandleSleepIntentUseCase(
                daySummaryRepository = FakeDaySummaryRepository(),
                timeProvider = FakeTimeProvider(),
                wallpaperController = FakeWallpaperController(),
                syncScheduler = FakeSyncScheduler(),
            ),
            appScope = CoroutineScope(coroutineContext),
        )

        coordinator.stop()
        advanceUntilIdle()
    }

    private class FakeIntentMediator : IntentMediator {
        private val mutable = MutableSharedFlow<AppIntent>()
        override val intents = mutable.asSharedFlow()

        override suspend fun emit(intent: AppIntent) {
            mutable.emit(intent)
        }
    }

    private class FakeDaySummaryRepository : DaySummaryRepository {
        var pendingCalls = 0

        override fun observeLatestSummary(): Flow<DaySummary?> = MutableStateFlow(null)
        override fun observeRecentSummaries(limit: Int): Flow<List<DaySummary>> = MutableStateFlow(emptyList())

        override suspend fun createPendingSummary(dayStartEpochMillis: Long): DaySummary {
            pendingCalls += 1
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
                createdAtEpochMillis = 0L,
                updatedAtEpochMillis = 0L,
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
        override fun currentTimeMillis(): Long = 0L
        override fun currentDayStartEpochMillis(): Long = 0L
    }

    private class FakeWallpaperController : WallpaperController {
        override suspend fun setWorkModeWallpaper(title: String) = Unit
        override suspend fun setNightModeWallpaper() = Unit
    }

    private class FakeSyncScheduler : SyncScheduler {
        override fun scheduleDayClosureSync() = Unit
    }
}
