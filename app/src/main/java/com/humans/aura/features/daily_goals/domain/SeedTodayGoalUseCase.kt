package com.humans.aura.features.daily_goals.domain

import com.humans.aura.core.domain.interfaces.DailyGoalRepository

class SeedTodayGoalUseCase(
    private val dailyGoalRepository: DailyGoalRepository,
) {
    suspend operator fun invoke() {
        dailyGoalRepository.seedTodayGoal()
    }
}
