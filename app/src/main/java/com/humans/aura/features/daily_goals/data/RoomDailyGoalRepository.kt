package com.humans.aura.features.daily_goals.data

import com.humans.aura.core.domain.interfaces.DailyGoalRepository
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.DailyGoal
import com.humans.aura.core.domain.models.GoalSubtaskDraft
import com.humans.aura.core.services.database.dao.DailyGoalDao
import com.humans.aura.core.services.database.entity.GoalSubtaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDailyGoalRepository(
    private val dailyGoalDao: DailyGoalDao,
    private val timeProvider: TimeProvider,
) : DailyGoalRepository {

    override fun observeTodayGoal(): Flow<DailyGoal?> =
        dailyGoalDao.observeGoalForDay(timeProvider.currentDayStartEpochMillis()).map { relation ->
            relation?.toDomain()
        }

    override suspend fun saveTodayGoal(
        mainTitle: String,
        subtasks: List<GoalSubtaskDraft>,
    ) {
        dailyGoalDao.saveGoalWithSubtasks(
            dayStartEpochMillis = timeProvider.currentDayStartEpochMillis(),
            mainTitle = mainTitle.trim(),
            subtasks = subtasks.mapIndexed { index, subtask ->
                GoalSubtaskEntity(
                    goalId = 0,
                    title = subtask.title.trim(),
                    isCompleted = subtask.isCompleted,
                    position = index,
                    isSyncedToD1 = false,
                )
            },
        )
    }

    override suspend fun markAiGenerationPending() {
        dailyGoalDao.markAiGenerationPending(timeProvider.currentDayStartEpochMillis())
    }

    override suspend fun clearTodayGoal() {
        dailyGoalDao.deleteGoalForDay(timeProvider.currentDayStartEpochMillis())
    }
}
