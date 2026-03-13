package com.humans.aura.features.day_closure.domain

import com.humans.aura.core.domain.interfaces.DailyGoalRepository
import com.humans.aura.core.domain.interfaces.SyncScheduler
import com.humans.aura.core.domain.interfaces.WallpaperController
import com.humans.aura.core.domain.models.AppIntent

class HandleSleepIntentUseCase(
    private val dailyGoalRepository: DailyGoalRepository,
    private val wallpaperController: WallpaperController,
    private val syncScheduler: SyncScheduler,
) {
    suspend operator fun invoke(intent: AppIntent.SleepLogged) {
        dailyGoalRepository.markAiGenerationPending()
        wallpaperController.setNightModeWallpaper()
        syncScheduler.scheduleDayClosureSync()
    }
}
