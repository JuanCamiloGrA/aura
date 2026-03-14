package com.humans.aura.features.day_summary.data

import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.interfaces.ConversationContextRepository
import com.humans.aura.core.domain.interfaces.DailyGoalRepository
import com.humans.aura.core.domain.interfaces.DaySummaryRepository
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.DaySummaryContext
import kotlinx.coroutines.flow.first

class RoomConversationContextRepository(
    private val activityRepository: ActivityRepository,
    private val dailyGoalRepository: DailyGoalRepository,
    private val daySummaryRepository: DaySummaryRepository,
    private val timeProvider: TimeProvider,
) : ConversationContextRepository {

    override suspend fun buildContextForDay(dayStartEpochMillis: Long): DaySummaryContext {
        val activities = activityRepository.observeActivitiesForDay(dayStartEpochMillis).first()
        val goal = dailyGoalRepository.getGoalForDay(dayStartEpochMillis)
        val recentSummaries = daySummaryRepository.observeRecentSummaries().first()
        val completed = goal?.completedSubtasks ?: 0
        val total = goal?.totalSubtasks ?: 0
        val focusMinutes = activities.sumOf { activity ->
            val end = activity.endTimeEpochMillis ?: timeProvider.currentTimeMillis()
            ((end - activity.startTimeEpochMillis).coerceAtLeast(0) / 60_000L)
        }
        val lostMinutes = activities
            .filter { it.status == com.humans.aura.core.domain.models.ActivityStatus.LOST }
            .sumOf { activity ->
                val end = activity.endTimeEpochMillis ?: timeProvider.currentTimeMillis()
                ((end - activity.startTimeEpochMillis).coerceAtLeast(0) / 60_000L)
            }

        return DaySummaryContext(
            dayStartEpochMillis = dayStartEpochMillis,
            activities = activities,
            dailyGoal = goal,
            recentSummaries = recentSummaries.filter { it.dayStartEpochMillis < dayStartEpochMillis }.take(7),
            completionRatio = if (total == 0) 0f else completed.toFloat() / total.toFloat(),
            focusMinutes = focusMinutes,
            lostMinutes = lostMinutes,
            longestActivityTitle = activities.maxByOrNull { activity ->
                (activity.endTimeEpochMillis ?: timeProvider.currentTimeMillis()) - activity.startTimeEpochMillis
            }?.title,
        )
    }

    override suspend fun buildChatContext(limit: Int): DaySummaryContext =
        buildContextForDay(timeProvider.currentDayStartEpochMillis())
}
