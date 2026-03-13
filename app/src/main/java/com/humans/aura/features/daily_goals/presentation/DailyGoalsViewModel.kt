package com.humans.aura.features.daily_goals.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humans.aura.features.daily_goals.domain.ClearTodayGoalUseCase
import com.humans.aura.features.daily_goals.domain.ObserveTodayGoalUseCase
import com.humans.aura.features.daily_goals.domain.SeedTodayGoalUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DailyGoalsViewModel(
    observeTodayGoalUseCase: ObserveTodayGoalUseCase,
    private val seedTodayGoalUseCase: SeedTodayGoalUseCase,
    private val clearTodayGoalUseCase: ClearTodayGoalUseCase,
) : ViewModel() {

    val uiState: StateFlow<DailyGoalsUiState> = observeTodayGoalUseCase()
        .map { goal ->
            DailyGoalsUiState(goal = goal, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DailyGoalsUiState(isLoading = true),
        )

    fun seedTodayGoal() {
        viewModelScope.launch {
            seedTodayGoalUseCase()
        }
    }

    fun clearTodayGoal() {
        viewModelScope.launch {
            clearTodayGoalUseCase()
        }
    }
}
