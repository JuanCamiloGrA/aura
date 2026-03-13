package com.humans.aura.features.daily_goals.data

import com.humans.aura.core.domain.interfaces.DailyGoalRepository
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.DailyGoal
import com.humans.aura.core.services.database.dao.DailyGoalDao
import com.humans.aura.core.services.database.entity.DailyGoalEntity
import com.humans.aura.core.services.database.entity.GoalSubtaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId

class RoomDailyGoalRepository(
    private val dailyGoalDao: DailyGoalDao,
    private val timeProvider: TimeProvider,
) : DailyGoalRepository {

    override fun observeTodayGoal(): Flow<DailyGoal?> =
        dailyGoalDao.observeGoalForDay(todayStartEpochMillis()).map { relation ->
            relation?.toDomain()
        }

    override suspend fun seedTodayGoal() {
        val dayStart = todayStartEpochMillis()
        if (dailyGoalDao.countGoalsForDay(dayStart) > 0) {
            return
        }

        val goalId = dailyGoalDao.insertGoal(
            DailyGoalEntity(
                dayStartEpochMillis = dayStart,
                mainTitle = "Protect deep work and keep the log honest",
            ),
        )

        dailyGoalDao.insertSubtasks(
            listOf(
                GoalSubtaskEntity(goalId = goalId, title = "Open the current activity instantly", isCompleted = true, position = 0),
                GoalSubtaskEntity(goalId = goalId, title = "Expose local prediction and status flags", isCompleted = false, position = 1),
                GoalSubtaskEntity(goalId = goalId, title = "Prepare offline sync markers", isCompleted = false, position = 2),
            ),
        )
    }

    override suspend fun clearTodayGoal() {
        dailyGoalDao.deleteGoalForDay(todayStartEpochMillis())
    }

    private fun todayStartEpochMillis(): Long =
        Instant.ofEpochMilli(timeProvider.currentTimeMillis())
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
}
