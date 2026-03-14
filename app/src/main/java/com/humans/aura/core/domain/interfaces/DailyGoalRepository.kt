package com.humans.aura.core.domain.interfaces

import com.humans.aura.core.domain.models.DailyGoal
import com.humans.aura.core.domain.models.GoalSubtaskDraft
import kotlinx.coroutines.flow.Flow

interface DailyGoalRepository {
    fun observeTodayGoal(): Flow<DailyGoal?>

    suspend fun getGoalForDay(dayStartEpochMillis: Long): DailyGoal?

    suspend fun saveTodayGoal(
        mainTitle: String,
        subtasks: List<GoalSubtaskDraft>,
    )

    suspend fun clearTodayGoal()
}
