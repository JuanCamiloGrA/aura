package com.humans.aura.features.day_summary.data

import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.interfaces.DailyGoalRepository
import com.humans.aura.core.domain.interfaces.DaySummaryRepository
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityStatus
import com.humans.aura.core.domain.models.DaySummary
import com.humans.aura.core.domain.models.DailyGoal
import com.humans.aura.core.domain.models.GoalSubtask
import com.humans.aura.core.domain.models.SummaryGenerationStatus
import com.humans.aura.features.stopwatch.domain.ActivityPrediction
import com.humans.aura.features.stopwatch.domain.LogNewActivityCommand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoomConversationContextRepositoryTest {

    @Test
    fun build_context_for_day_computes_metrics_and_filters_recent_summaries() = runTest {
        val activities = listOf(
            Activity(id = 1, title = "Deep Work", startTimeEpochMillis = 0, endTimeEpochMillis = 3_600_000, status = ActivityStatus.ACCURATE, isSyncedToD1 = false),
            Activity(id = 2, title = "Walk", startTimeEpochMillis = 3_600_000, endTimeEpochMillis = 5_400_000, status = ActivityStatus.LOST, isSyncedToD1 = false),
            Activity(id = 3, title = "Reading", startTimeEpochMillis = 5_400_000, endTimeEpochMillis = null, status = ActivityStatus.ACTIVE, isSyncedToD1 = false),
        )
        val goal = DailyGoal(
            id = 10,
            dayStartEpochMillis = 10_000,
            mainTitle = "Ship milestone",
            subtasks = listOf(
                GoalSubtask(id = 1, goalId = 10, title = "Worker factory", isCompleted = true, position = 0, isSyncedToD1 = false),
                GoalSubtask(id = 2, goalId = 10, title = "Coverage", isCompleted = false, position = 1, isSyncedToD1 = false),
            ),
            isSyncedToD1 = false,
        )
        val summaries = (1L..10L).map { dayOffset ->
            DaySummary(
                id = dayOffset,
                dayStartEpochMillis = dayOffset * 1_000,
                summaryText = "summary-$dayOffset",
                rawContextJson = "{}",
                promptVersion = "v1",
                modelName = "gemini",
                generationStatus = SummaryGenerationStatus.COMPLETED,
                errorMessage = null,
                lastAttemptEpochMillis = null,
                createdAtEpochMillis = dayOffset,
                updatedAtEpochMillis = dayOffset,
                isSyncedToD1 = false,
            )
        }
        val repository = RoomConversationContextRepository(
            activityRepository = FakeActivityRepository(activities),
            dailyGoalRepository = FakeDailyGoalRepository(goal),
            daySummaryRepository = FakeDaySummaryRepository(summaries),
            timeProvider = FakeTimeProvider(now = 7_200_000, dayStart = 10_000),
        )

        val context = repository.buildContextForDay(10_000)

        assertEquals(10_000L, context.dayStartEpochMillis)
        assertEquals(goal, context.dailyGoal)
        assertEquals(120L, context.focusMinutes)
        assertEquals(30L, context.lostMinutes)
        assertEquals(0.5f, context.completionRatio)
        assertEquals("Deep Work", context.longestActivityTitle)
        assertEquals(7, context.recentSummaries.size)
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L, 6L, 7L), context.recentSummaries.map { it.id })
    }

    @Test
    fun build_context_for_day_handles_missing_goal_and_zero_subtasks() = runTest {
        val repository = RoomConversationContextRepository(
            activityRepository = FakeActivityRepository(emptyList()),
            dailyGoalRepository = FakeDailyGoalRepository(null),
            daySummaryRepository = FakeDaySummaryRepository(emptyList()),
            timeProvider = FakeTimeProvider(now = 1_000, dayStart = 55_000),
        )

        val context = repository.buildChatContext(limit = 3)

        assertEquals(55_000L, context.dayStartEpochMillis)
        assertNull(context.dailyGoal)
        assertEquals(0f, context.completionRatio)
        assertEquals(0L, context.focusMinutes)
        assertEquals(0L, context.lostMinutes)
        assertNull(context.longestActivityTitle)
        assertEquals(emptyList<DaySummary>(), context.recentSummaries)
    }

    private class FakeActivityRepository(
        activities: List<Activity>,
    ) : ActivityRepository {
        private val dayActivities = MutableStateFlow(activities)

        override fun observeCurrentActivity(): Flow<Activity?> = flowOf(null)

        override fun observeRecentActivities(limit: Int): Flow<List<Activity>> = flowOf(emptyList())

        override fun observeActivitiesForDay(dayStartEpochMillis: Long): Flow<List<Activity>> = dayActivities

        override suspend fun logNewActivity(command: LogNewActivityCommand): Activity {
            error("Not needed in test")
        }

        override suspend fun predictNextTitle(nowEpochMillis: Long): ActivityPrediction? = null

        override suspend fun updateCurrentActivityStatus(status: ActivityStatus) = Unit

        override suspend fun clearAll() = Unit
    }

    private class FakeDailyGoalRepository(
        private val goal: DailyGoal?,
    ) : DailyGoalRepository {
        override fun observeTodayGoal(): Flow<DailyGoal?> = flowOf(goal)

        override suspend fun getGoalForDay(dayStartEpochMillis: Long): DailyGoal? = goal

        override suspend fun saveTodayGoal(mainTitle: String, subtasks: List<com.humans.aura.core.domain.models.GoalSubtaskDraft>) {
            error("Not needed in test")
        }

        override suspend fun clearTodayGoal() = Unit
    }

    private class FakeDaySummaryRepository(
        private val summaries: List<DaySummary>,
    ) : DaySummaryRepository {
        override fun observeLatestSummary(): Flow<DaySummary?> = flowOf(summaries.firstOrNull())

        override fun observeRecentSummaries(limit: Int): Flow<List<DaySummary>> = flowOf(summaries.take(limit))

        override suspend fun createPendingSummary(dayStartEpochMillis: Long): DaySummary {
            error("Not needed in test")
        }

        override suspend fun getPendingSummaries(limit: Int): List<DaySummary> = emptyList()

        override suspend fun updatePendingContext(summaryId: Long, rawContextJson: String, promptVersion: String, modelName: String, lastAttemptEpochMillis: Long) = Unit

        override suspend fun updateSummaryResult(summaryId: Long, summaryText: String, modelName: String, lastAttemptEpochMillis: Long) = Unit

        override suspend fun recordRetryableFailure(summaryId: Long, errorMessage: String, modelName: String, lastAttemptEpochMillis: Long) = Unit

        override suspend fun recordTerminalFailure(summaryId: Long, errorMessage: String, modelName: String, lastAttemptEpochMillis: Long) = Unit
    }

    private class FakeTimeProvider(
        private val now: Long,
        private val dayStart: Long,
    ) : TimeProvider {
        override fun currentTimeMillis(): Long = now

        override fun currentDayStartEpochMillis(): Long = dayStart
    }
}
