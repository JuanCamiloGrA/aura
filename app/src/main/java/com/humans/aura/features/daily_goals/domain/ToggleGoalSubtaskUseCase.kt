package com.humans.aura.features.daily_goals.domain

import com.humans.aura.core.domain.interfaces.DailyGoalRepository

class ToggleGoalSubtaskUseCase(
    private val dailyGoalRepository: DailyGoalRepository,
) {
    suspend operator fun invoke(subtaskId: Long, isCompleted: Boolean) {
        dailyGoalRepository.toggleSubtask(subtaskId, isCompleted)
    }
}
