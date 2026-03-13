package com.humans.aura.features.daily_goals.presentation

import com.humans.aura.core.domain.models.DailyGoal

data class DailyGoalsUiState(
    val goal: DailyGoal? = null,
    val isLoading: Boolean = false,
)
