package com.humans.aura.features.stopwatch.domain

import com.humans.aura.core.domain.interfaces.ActivityRepository

class ObserveCurrentActivityUseCase(
    private val activityRepository: ActivityRepository,
) {
    operator fun invoke() = activityRepository.observeCurrentActivity()
}
