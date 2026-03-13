package com.humans.aura.features.daily_goals.domain

import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.interfaces.TimeProvider

class ObserveTodayActivitiesUseCase(
    private val activityRepository: ActivityRepository,
    private val timeProvider: TimeProvider,
) {
    operator fun invoke() = activityRepository.observeActivitiesForDay(timeProvider.currentDayStartEpochMillis())
}
