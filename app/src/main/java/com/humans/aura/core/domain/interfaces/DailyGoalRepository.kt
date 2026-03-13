package com.humans.aura.core.domain.interfaces

import com.humans.aura.core.domain.models.DailyGoal
import kotlinx.coroutines.flow.Flow

interface DailyGoalRepository {
    fun observeTodayGoal(): Flow<DailyGoal?>

    suspend fun seedTodayGoal()

    suspend fun clearTodayGoal()
}
