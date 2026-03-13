package com.humans.aura.features.stopwatch.domain

import com.humans.aura.core.domain.interfaces.ActivityRepository

class ObserveRecentActivitiesUseCase(
    private val activityRepository: ActivityRepository,
) {
    operator fun invoke(limit: Int = 5) = activityRepository.observeRecentActivities(limit)
}
