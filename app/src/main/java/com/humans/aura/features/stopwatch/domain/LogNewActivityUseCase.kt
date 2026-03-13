package com.humans.aura.features.stopwatch.domain

import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.models.Activity

class LogNewActivityUseCase(
    private val activityRepository: ActivityRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(title: String): Activity {
        val normalizedTitle = title.trim()
        require(normalizedTitle.isNotEmpty()) { "Activity title cannot be blank" }

        return activityRepository.logNewActivity(
            LogNewActivityCommand(
                title = normalizedTitle,
                timestampEpochMillis = timeProvider.currentTimeMillis(),
            ),
        )
    }
}
