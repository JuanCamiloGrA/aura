package com.humans.aura.features.stopwatch.domain

import com.humans.aura.core.domain.interfaces.ActivityRepository

class SeedStopwatchSampleDataUseCase(
    private val activityRepository: ActivityRepository,
) {
    suspend operator fun invoke() {
        activityRepository.seedPhaseZeroData()
    }
}
