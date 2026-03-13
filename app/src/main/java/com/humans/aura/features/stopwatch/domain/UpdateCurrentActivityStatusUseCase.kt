package com.humans.aura.features.stopwatch.domain

import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.models.ActivityStatus

class UpdateCurrentActivityStatusUseCase(
    private val activityRepository: ActivityRepository,
) {
    suspend operator fun invoke(status: ActivityStatus) {
        require(status != ActivityStatus.ACTIVE) { "Current activity status must be terminal" }
        activityRepository.updateCurrentActivityStatus(status)
    }
}
