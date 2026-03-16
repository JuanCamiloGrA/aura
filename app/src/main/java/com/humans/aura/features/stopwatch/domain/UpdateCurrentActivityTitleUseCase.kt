package com.humans.aura.features.stopwatch.domain

import com.humans.aura.core.domain.interfaces.ActivityRepository

class UpdateCurrentActivityTitleUseCase(
    private val activityRepository: ActivityRepository,
) {
    suspend operator fun invoke(title: String) {
        val normalizedTitle = title.trim()
        require(normalizedTitle.isNotEmpty()) { "Activity title cannot be blank" }

        activityRepository.updateCurrentActivityTitle(normalizedTitle)
    }
}
