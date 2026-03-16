package com.humans.aura.features.daily_goals.domain

import com.humans.aura.core.domain.interfaces.DailyGoalRepository
import com.humans.aura.core.domain.interfaces.WallpaperController

class UpdateTodayGoalTitleUseCase(
    private val dailyGoalRepository: DailyGoalRepository,
    private val wallpaperController: WallpaperController,
) {
    suspend operator fun invoke(title: String) {
        val normalizedTitle = title.trim()
        require(normalizedTitle.isNotEmpty()) { "Main title cannot be blank" }

        dailyGoalRepository.updateTodayGoalTitle(normalizedTitle)
        wallpaperController.setWorkModeWallpaper(normalizedTitle)
    }
}
