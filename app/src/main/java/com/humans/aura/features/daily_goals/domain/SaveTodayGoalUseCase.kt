package com.humans.aura.features.daily_goals.domain

import com.humans.aura.core.domain.interfaces.DailyGoalRepository
import com.humans.aura.core.domain.interfaces.WallpaperController
import com.humans.aura.core.domain.models.GoalSubtaskDraft

class SaveTodayGoalUseCase(
    private val dailyGoalRepository: DailyGoalRepository,
    private val wallpaperController: WallpaperController,
) {
    suspend operator fun invoke(
        mainTitle: String,
        subtasks: List<GoalSubtaskDraft>,
    ) {
        require(mainTitle.isNotBlank()) { "Main title cannot be blank" }

        val cleanedSubtasks = subtasks.filter { it.title.isNotBlank() }
        dailyGoalRepository.saveTodayGoal(
            mainTitle = mainTitle,
            subtasks = cleanedSubtasks,
        )
        wallpaperController.setWorkModeWallpaper(mainTitle.trim())
    }
}
