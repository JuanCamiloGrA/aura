package com.humans.aura.features.stopwatch.presentation

import com.humans.aura.core.domain.models.Activity
import com.humans.aura.features.stopwatch.domain.ActivityPrediction

data class StopwatchUiState(
    val currentActivity: Activity? = null,
    val recentActivities: List<Activity> = emptyList(),
    val draftTitle: String = "",
    val prediction: ActivityPrediction? = null,
    val runningDurationLabel: String = "00:00:00",
    val isLoading: Boolean = false,
    val isLogging: Boolean = false,
)
