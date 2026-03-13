package com.humans.aura.features.daily_goals.presentation

import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.DailyGoal

data class DailyGoalsUiState(
    val goal: DailyGoal? = null,
    val mainTitleInput: String = "",
    val subtaskInputs: List<String> = listOf("", "", ""),
    val todayActivities: List<Activity> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
)
