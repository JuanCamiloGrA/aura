package com.humans.aura.features.stopwatch.presentation

import com.humans.aura.core.domain.models.Activity

data class StopwatchUiState(
    val currentActivity: Activity? = null,
    val recentActivities: List<Activity> = emptyList(),
    val isLoading: Boolean = false,
)
