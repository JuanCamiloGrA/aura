package com.humans.aura.features.stopwatch.presentation

import com.humans.aura.MainDispatcherRule
import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityStatus
import com.humans.aura.features.stopwatch.domain.ActivityPrediction
import com.humans.aura.features.stopwatch.domain.ClearActivitiesUseCase
import com.humans.aura.features.stopwatch.domain.LogNewActivityCommand
import com.humans.aura.features.stopwatch.domain.LogNewActivityUseCase
import com.humans.aura.features.stopwatch.domain.ObserveCurrentActivityUseCase
import com.humans.aura.features.stopwatch.domain.ObserveRecentActivitiesUseCase
import com.humans.aura.features.stopwatch.domain.PredictNextActivityTitleUseCase
import com.humans.aura.features.stopwatch.domain.UpdateCurrentActivityStatusUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StopwatchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun ui_state_prefills_prediction_when_draft_empty_and_formats_duration() = runTest {
        val activityRepository = FakeActivityRepository(
            current = Activity(1, "Deep Work", 0L, 3_661_000L, ActivityStatus.ACTIVE, false),
            recent = listOf(Activity(2, "Review", 1L, 61_000L, ActivityStatus.ACCURATE, false)),
            prediction = ActivityPrediction("Review", 2, 100L),
        )
        val viewModel = createViewModel(activityRepository)
        startCollecting(viewModel)
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals("Review", state.draftTitle)
        assertEquals("01:01:01", state.runningDurationLabel)
        assertEquals("Deep Work", state.currentActivity?.title)
        assertEquals(1, state.recentActivities.size)
    }

    @Test
    fun draft_changes_override_prediction_and_clears_suggestion() = runTest {
        val activityRepository = FakeActivityRepository(prediction = ActivityPrediction("Review", 2, 100L))
        val viewModel = createViewModel(activityRepository)
        startCollecting(viewModel)
        advanceUntilIdle()

        viewModel.onDraftTitleChanged("Write tests")
        advanceUntilIdle()

        assertEquals("Write tests", viewModel.uiState.value.draftTitle)
        assertNull(viewModel.uiState.value.prediction)
    }

    @Test
    fun typing_after_autofill_clears_prediction_and_keeps_free_text() = runTest {
        val activityRepository = FakeActivityRepository(prediction = ActivityPrediction("Review", 2, 100L))
        val viewModel = createViewModel(activityRepository)
        startCollecting(viewModel)
        advanceUntilIdle()

        assertEquals("Review", viewModel.uiState.value.draftTitle)
        assertEquals("Review", viewModel.uiState.value.prediction?.title)

        viewModel.onDraftTitleChanged("Write tests")
        advanceUntilIdle()

        assertEquals("Write tests", viewModel.uiState.value.draftTitle)
        assertNull(viewModel.uiState.value.prediction)
        assertFalse(viewModel.uiState.value.isPredictionAutofilled)
    }

    @Test
    fun refresh_prediction_updates_ui() = runTest {
        val activityRepository = FakeActivityRepository(prediction = null)
        val viewModel = createViewModel(activityRepository)
        startCollecting(viewModel)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.prediction)

        activityRepository.prediction = ActivityPrediction("Email", 1, 120L)
        viewModel.refreshPrediction()
        advanceUntilIdle()

        assertEquals("Email", viewModel.uiState.value.prediction?.title)
        assertEquals("Email", viewModel.uiState.value.draftTitle)
    }

    @Test
    fun log_new_activity_uses_explicit_draft_and_resets_logging_state() = runTest {
        val activityRepository = FakeActivityRepository(prediction = null)
        val viewModel = createViewModel(activityRepository)
        startCollecting(viewModel)
        advanceUntilIdle()

        viewModel.onDraftTitleChanged("Write tests")
        viewModel.logNewActivity()
        advanceUntilIdle()

        assertEquals("Write tests", activityRepository.loggedTitles.single())
        assertFalse(viewModel.uiState.value.isLogging)
        assertEquals("", viewModel.uiState.value.draftTitle)
    }

    @Test
    fun log_new_activity_uses_prediction_when_draft_is_blank() = runTest {
        val activityRepository = FakeActivityRepository(prediction = ActivityPrediction("Review", 2, 100L))
        val viewModel = createViewModel(activityRepository)
        startCollecting(viewModel)
        advanceUntilIdle()

        viewModel.logNewActivity()
        advanceUntilIdle()

        assertEquals("Review", activityRepository.loggedTitles.single())
    }

    @Test
    fun log_new_activity_with_blank_input_and_missing_prediction_does_not_log() = runTest {
        val activityRepository = FakeActivityRepository(prediction = null)
        val viewModel = createViewModel(activityRepository)
        startCollecting(viewModel)
        advanceUntilIdle()

        viewModel.logNewActivity()
        advanceUntilIdle()

        assertEquals(emptyList<String>(), activityRepository.loggedTitles)
        assertEquals("", viewModel.uiState.value.draftTitle)
        assertFalse(viewModel.uiState.value.isLogging)
    }

    @Test
    fun log_new_activity_ignores_reentry_while_logging() = runTest {
        val activityRepository = FakeActivityRepository(logGate = CompletableDeferred())
        val viewModel = createViewModel(activityRepository)
        startCollecting(viewModel)
        advanceUntilIdle()

        viewModel.onDraftTitleChanged("Focus")
        viewModel.logNewActivity()
        runCurrent()

        assertEquals(true, viewModel.uiState.value.isLogging)

        viewModel.logNewActivity()
        runCurrent()
        activityRepository.logGate?.complete(Unit)
        advanceUntilIdle()

        assertEquals(listOf("Focus"), activityRepository.loggedTitles)
        assertFalse(viewModel.uiState.value.isLogging)
    }

    @Test
    fun log_new_activity_failure_preserves_draft() = runTest {
        val activityRepository = FakeActivityRepository(throwOnLog = true)
        val viewModel = createViewModel(activityRepository)
        startCollecting(viewModel)
        advanceUntilIdle()

        viewModel.onDraftTitleChanged("Failing task")
        viewModel.logNewActivity()
        advanceUntilIdle()

        assertEquals(emptyList<String>(), activityRepository.loggedTitles)
        assertEquals("Failing task", viewModel.uiState.value.draftTitle)
        assertFalse(viewModel.uiState.value.isLogging)
    }

    @Test
    fun status_buttons_delegate_to_repository() = runTest {
        val activityRepository = FakeActivityRepository()
        val viewModel = createViewModel(activityRepository)
        startCollecting(viewModel)
        advanceUntilIdle()

        viewModel.markInaccurate()
        viewModel.markLost()
        advanceUntilIdle()

        assertEquals(listOf(ActivityStatus.INACCURATE, ActivityStatus.LOST), activityRepository.updatedStatuses)
    }

    @Test
    fun clear_all_resets_state_and_repository() = runTest {
        val activityRepository = FakeActivityRepository(prediction = ActivityPrediction("Review", 2, 100L))
        val viewModel = createViewModel(activityRepository)
        startCollecting(viewModel)
        advanceUntilIdle()

        viewModel.onDraftTitleChanged("Focus")
        viewModel.clearAll()
        advanceUntilIdle()

        assertEquals(1, activityRepository.clearCalls)
        assertNull(viewModel.uiState.value.prediction)
        assertEquals("", viewModel.uiState.value.draftTitle)
    }

    private fun createViewModel(
        activityRepository: FakeActivityRepository,
    ): StopwatchViewModel = StopwatchViewModel(
        observeCurrentActivityUseCase = ObserveCurrentActivityUseCase(activityRepository),
        observeRecentActivitiesUseCase = ObserveRecentActivitiesUseCase(activityRepository),
        logNewActivityUseCase = LogNewActivityUseCase(activityRepository, FakeTimeProvider()),
        predictNextActivityTitleUseCase = PredictNextActivityTitleUseCase(activityRepository, FakeTimeProvider()),
        updateCurrentActivityStatusUseCase = UpdateCurrentActivityStatusUseCase(activityRepository),
        clearActivitiesUseCase = ClearActivitiesUseCase(activityRepository),
    )

    private fun kotlinx.coroutines.test.TestScope.startCollecting(viewModel: StopwatchViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
    }

    private class FakeActivityRepository(
        current: Activity? = null,
        recent: List<Activity> = emptyList(),
        var prediction: ActivityPrediction? = null,
        var throwOnLog: Boolean = false,
        var logGate: CompletableDeferred<Unit>? = null,
    ) : ActivityRepository {
        private val currentFlow = MutableStateFlow(current)
        private val recentFlow = MutableStateFlow(recent)
        val loggedTitles = mutableListOf<String>()
        val updatedStatuses = mutableListOf<ActivityStatus>()
        var clearCalls = 0
        var predictCalls = 0

        override fun observeCurrentActivity(): Flow<Activity?> = currentFlow
        override fun observeRecentActivities(limit: Int): Flow<List<Activity>> = recentFlow
        override fun observeActivitiesForDay(dayStartEpochMillis: Long): Flow<List<Activity>> = MutableStateFlow(emptyList())

        override suspend fun logNewActivity(command: LogNewActivityCommand): Activity {
            logGate?.await()
            if (throwOnLog) {
                throw IllegalStateException("boom")
            }
            loggedTitles += command.title
            return Activity(1, command.title, command.timestampEpochMillis, null, ActivityStatus.ACTIVE, false).also {
                currentFlow.value = it
            }
        }

        override suspend fun predictNextTitle(nowEpochMillis: Long): ActivityPrediction? {
            predictCalls += 1
            return prediction
        }

        override suspend fun updateCurrentActivityStatus(status: ActivityStatus) {
            updatedStatuses += status
        }

        override suspend fun clearAll() {
            clearCalls += 1
            currentFlow.value = null
            recentFlow.value = emptyList()
        }
    }

    private class FakeTimeProvider : TimeProvider {
        override fun currentTimeMillis(): Long = 0L
        override fun currentDayStartEpochMillis(): Long = 0L
    }
}
