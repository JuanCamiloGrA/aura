package com.humans.aura.features.stopwatch.domain

import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.interfaces.TimeProvider

class PredictNextActivityTitleUseCase(
    private val activityRepository: ActivityRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(): ActivityPrediction? =
        activityRepository.predictNextTitle(timeProvider.currentTimeMillis())
}
