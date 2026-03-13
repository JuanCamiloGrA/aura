package com.humans.aura.features.daily_goals.presentation

import app.cash.turbine.test
import com.humans.aura.MainDispatcherRule
import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.interfaces.DailyGoalRepository
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityStatus
import com.humans.aura.core.domain.models.DailyGoal
import com.humans.aura.core.domain.models.GoalSubtask
import com.humans.aura.core.domain.models.GoalSubtaskDraft
import com.humans.aura.features.daily_goals.domain.ClearTodayGoalUseCase
import com.humans.aura.features.daily_goals.domain.ObserveTodayActivitiesUseCase
import com.humans.aura.features.daily_goals.domain.ObserveTodayGoalUseCase
import com.humans.aura.features.daily_goals.domain.SaveTodayGoalUseCase
import com.humans.aura.features.stopwatch.domain.ActivityPrediction
import com.humans.aura.features.stopwatch.domain.LogNewActivityCommand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DailyGoalsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun ui_state_prefills_goal_when_inputs_empty() = runTest {
        val goalRepository = FakeDailyGoalRepository(
            goal = DailyGoal(
                id = 1,
                dayStartEpochMillis = 0L,
                mainTitle = "Protect deep work",
                subtasks = listOf(GoalSubtask(1, 1, "Focus", false, 0, false)),
                isAiGenerationPending = false,
                isSyncedToD1 = false,
            ),
        )
        val viewModel = createViewModel(goalRepository)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            if (!state.isLoading) {
                assertEquals("Protect deep work", state.mainTitleInput)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun save_today_goal_delegates_current_inputs() = runTest {
        val goalRepository = FakeDailyGoalRepository()
        val viewModel = createViewModel(goalRepository)
        advanceUntilIdle()

        viewModel.onMainTitleChanged("Ship")
        viewModel.onSubtaskChanged(0, "Implement")
        viewModel.saveTodayGoal()
        advanceUntilIdle()

        assertEquals("Ship", goalRepository.savedMainTitle)
        assertEquals(listOf("Implement"), goalRepository.savedSubtasks.map { it.title })
    }

    private fun createViewModel(
        goalRepository: FakeDailyGoalRepository,
    ): DailyGoalsViewModel {
        val activityRepository = FakeActivityRepository()
        val timeProvider = FakeTimeProvider()
        return DailyGoalsViewModel(
            observeTodayGoalUseCase = ObserveTodayGoalUseCase(goalRepository),
            observeTodayActivitiesUseCase = ObserveTodayActivitiesUseCase(activityRepository, timeProvider),
            saveTodayGoalUseCase = SaveTodayGoalUseCase(goalRepository),
            clearTodayGoalUseCase = ClearTodayGoalUseCase(goalRepository),
        )
    }

    private class FakeDailyGoalRepository(
        goal: DailyGoal? = null,
    ) : DailyGoalRepository {
        private val goalFlow = MutableStateFlow(goal)
        var savedMainTitle: String? = null
        var savedSubtasks: List<GoalSubtaskDraft> = emptyList()

        override fun observeTodayGoal(): Flow<DailyGoal?> = goalFlow

        override suspend fun saveTodayGoal(mainTitle: String, subtasks: List<GoalSubtaskDraft>) {
            savedMainTitle = mainTitle
            savedSubtasks = subtasks
        }

        override suspend fun markAiGenerationPending() = Unit
        override suspend fun clearTodayGoal() = Unit
    }

    private class FakeActivityRepository : ActivityRepository {
        override fun observeCurrentActivity(): Flow<Activity?> = MutableStateFlow(null)
        override fun observeRecentActivities(limit: Int): Flow<List<Activity>> = MutableStateFlow(emptyList())
        override fun observeActivitiesForDay(dayStartEpochMillis: Long): Flow<List<Activity>> = MutableStateFlow(listOf(Activity(1, "Focus", 0L, null, ActivityStatus.ACTIVE, false)))
        override suspend fun logNewActivity(command: LogNewActivityCommand): Activity = throw UnsupportedOperationException()
        override suspend fun predictNextTitle(nowEpochMillis: Long): ActivityPrediction? = null
        override suspend fun updateCurrentActivityStatus(status: ActivityStatus) = Unit
        override suspend fun clearAll() = Unit
    }

    private class FakeTimeProvider : TimeProvider {
        override fun currentTimeMillis(): Long = 0L
        override fun currentDayStartEpochMillis(): Long = 0L
    }
}
