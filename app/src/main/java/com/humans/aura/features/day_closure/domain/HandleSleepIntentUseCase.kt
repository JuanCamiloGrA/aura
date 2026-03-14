package com.humans.aura.features.day_closure.domain

import com.humans.aura.core.domain.interfaces.DaySummaryRepository
import com.humans.aura.core.domain.interfaces.SyncScheduler
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.interfaces.WallpaperController
import com.humans.aura.core.domain.models.AppIntent

class HandleSleepIntentUseCase(
    private val daySummaryRepository: DaySummaryRepository,
    private val timeProvider: TimeProvider,
    private val wallpaperController: WallpaperController,
    private val syncScheduler: SyncScheduler,
) {
    suspend operator fun invoke(intent: AppIntent.SleepLogged) {
        daySummaryRepository.createPendingSummary(timeProvider.currentDayStartEpochMillis())
        wallpaperController.setNightModeWallpaper()
        syncScheduler.scheduleDayClosureSync()
    }
}
