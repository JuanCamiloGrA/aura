package com.humans.aura.features.daily_goals.domain

import com.humans.aura.core.domain.interfaces.DailyGoalRepository

class ObserveTodayGoalUseCase(
    private val dailyGoalRepository: DailyGoalRepository,
) {
    operator fun invoke() = dailyGoalRepository.observeTodayGoal()
}
