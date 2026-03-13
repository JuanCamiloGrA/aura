package com.humans.aura.features.stopwatch.presentation

import app.cash.turbine.test
import com.humans.aura.MainDispatcherRule
import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.interfaces.DailyGoalRepository
import com.humans.aura.core.domain.interfaces.IntentMediator
import com.humans.aura.core.domain.interfaces.SyncScheduler
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.interfaces.WallpaperController
import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityStatus
import com.humans.aura.core.domain.models.AppIntent
import com.humans.aura.core.domain.models.DailyGoal
import com.humans.aura.core.domain.models.GoalSubtaskDraft
import com.humans.aura.features.day_closure.domain.HandleSleepIntentUseCase
import com.humans.aura.features.stopwatch.domain.ActivityPrediction
import com.humans.aura.features.stopwatch.domain.ClearActivitiesUseCase
import com.humans.aura.features.stopwatch.domain.LogNewActivityCommand
import com.humans.aura.features.stopwatch.domain.LogNewActivityUseCase
import com.humans.aura.features.stopwatch.domain.ObserveCurrentActivityUseCase
import com.humans.aura.features.stopwatch.domain.ObserveRecentActivitiesUseCase
import com.humans.aura.features.stopwatch.domain.PredictNextActivityTitleUseCase
import com.humans.aura.features.stopwatch.domain.UpdateCurrentActivityStatusUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StopwatchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun ui_state_prefills_prediction_when_draft_empty() = runTest {
        val activityRepository = FakeActivityRepository(prediction = ActivityPrediction("Review", 2, 100L))
        val mediator = FakeIntentMediator()
        val viewModel = createViewModel(activityRepository, mediator)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            if (!state.isLoading) {
                assertEquals("Review", state.draftTitle)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun log_new_activity_calls_repository() = runTest {
        val activityRepository = FakeActivityRepository()
        val mediator = FakeIntentMediator()
        val viewModel = createViewModel(activityRepository, mediator)
        advanceUntilIdle()

        viewModel.onDraftTitleChanged("Write tests")
        viewModel.logNewActivity()
        advanceUntilIdle()

        assertEquals("Write tests", activityRepository.loggedTitles.single())
    }

    @Test
    fun status_buttons_delegate_to_repository() = runTest {
        val activityRepository = FakeActivityRepository()
        val viewModel = createViewModel(activityRepository, FakeIntentMediator())
        advanceUntilIdle()

        viewModel.markLost()
        advanceUntilIdle()

        assertEquals(ActivityStatus.LOST, activityRepository.updatedStatuses.single())
    }

    @Test
    fun sleep_intent_triggers_day_closure_handler() = runTest {
        val activityRepository = FakeActivityRepository()
        val dailyGoalRepository = FakeDailyGoalRepository()
        val mediator = FakeIntentMediator()
        val viewModel = StopwatchViewModel(
            observeCurrentActivityUseCase = ObserveCurrentActivityUseCase(activityRepository),
            observeRecentActivitiesUseCase = ObserveRecentActivitiesUseCase(activityRepository),
            logNewActivityUseCase = LogNewActivityUseCase(activityRepository, FakeTimeProvider()),
            predictNextActivityTitleUseCase = PredictNextActivityTitleUseCase(activityRepository, FakeTimeProvider()),
            updateCurrentActivityStatusUseCase = UpdateCurrentActivityStatusUseCase(activityRepository),
            clearActivitiesUseCase = ClearActivitiesUseCase(activityRepository),
            intentMediator = mediator,
            handleSleepIntentUseCase = HandleSleepIntentUseCase(
                dailyGoalRepository = dailyGoalRepository,
                wallpaperController = FakeWallpaperController(),
                syncScheduler = FakeSyncScheduler(),
            ),
        )
        advanceUntilIdle()

        mediator.emit(AppIntent.SleepLogged("Sleep", 50L))
        advanceUntilIdle()

        assertEquals(1, dailyGoalRepository.pendingCalls)
        viewModel.uiState.value
    }

    private fun createViewModel(
        activityRepository: FakeActivityRepository,
        mediator: FakeIntentMediator,
    ): StopwatchViewModel = StopwatchViewModel(
        observeCurrentActivityUseCase = ObserveCurrentActivityUseCase(activityRepository),
        observeRecentActivitiesUseCase = ObserveRecentActivitiesUseCase(activityRepository),
        logNewActivityUseCase = LogNewActivityUseCase(activityRepository, FakeTimeProvider()),
        predictNextActivityTitleUseCase = PredictNextActivityTitleUseCase(activityRepository, FakeTimeProvider()),
        updateCurrentActivityStatusUseCase = UpdateCurrentActivityStatusUseCase(activityRepository),
        clearActivitiesUseCase = ClearActivitiesUseCase(activityRepository),
        intentMediator = mediator,
        handleSleepIntentUseCase = HandleSleepIntentUseCase(
            dailyGoalRepository = FakeDailyGoalRepository(),
            wallpaperController = FakeWallpaperController(),
            syncScheduler = FakeSyncScheduler(),
        ),
    )

    private class FakeActivityRepository(
        current: Activity? = null,
        recent: List<Activity> = emptyList(),
        var prediction: ActivityPrediction? = null,
    ) : ActivityRepository {
        private val currentFlow = MutableStateFlow(current)
        private val recentFlow = MutableStateFlow(recent)
        val loggedTitles = mutableListOf<String>()
        val updatedStatuses = mutableListOf<ActivityStatus>()

        override fun observeCurrentActivity(): Flow<Activity?> = currentFlow
        override fun observeRecentActivities(limit: Int): Flow<List<Activity>> = recentFlow
        override fun observeActivitiesForDay(dayStartEpochMillis: Long): Flow<List<Activity>> = MutableStateFlow(emptyList())

        override suspend fun logNewActivity(command: LogNewActivityCommand): Activity {
            loggedTitles += command.title
            return Activity(1, command.title, command.timestampEpochMillis, null, ActivityStatus.ACTIVE, false)
        }

        override suspend fun predictNextTitle(nowEpochMillis: Long): ActivityPrediction? = prediction

        override suspend fun updateCurrentActivityStatus(status: ActivityStatus) {
            updatedStatuses += status
        }

        override suspend fun clearAll() = Unit
    }

    private class FakeDailyGoalRepository : DailyGoalRepository {
        var pendingCalls = 0

        override fun observeTodayGoal(): Flow<DailyGoal?> = MutableStateFlow(null)
        override suspend fun saveTodayGoal(mainTitle: String, subtasks: List<GoalSubtaskDraft>) = Unit
        override suspend fun markAiGenerationPending() {
            pendingCalls += 1
        }

        override suspend fun clearTodayGoal() = Unit
    }

    private class FakeTimeProvider : TimeProvider {
        override fun currentTimeMillis(): Long = 0L
        override fun currentDayStartEpochMillis(): Long = 0L
    }

    private class FakeIntentMediator : IntentMediator {
        private val mutable = MutableSharedFlow<AppIntent>()
        override val intents = mutable.asSharedFlow()

        override suspend fun emit(intent: AppIntent) {
            mutable.emit(intent)
        }
    }

    private class FakeWallpaperController : WallpaperController {
        override suspend fun setNightModeWallpaper() = Unit
    }

    private class FakeSyncScheduler : SyncScheduler {
        override fun scheduleDayClosureSync() = Unit
    }
}
