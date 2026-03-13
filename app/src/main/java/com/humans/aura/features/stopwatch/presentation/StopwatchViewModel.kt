package com.humans.aura.features.stopwatch.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humans.aura.features.stopwatch.domain.ClearActivitiesUseCase
import com.humans.aura.features.stopwatch.domain.ObserveCurrentActivityUseCase
import com.humans.aura.features.stopwatch.domain.ObserveRecentActivitiesUseCase
import com.humans.aura.features.stopwatch.domain.SeedStopwatchSampleDataUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StopwatchViewModel(
    observeCurrentActivityUseCase: ObserveCurrentActivityUseCase,
    observeRecentActivitiesUseCase: ObserveRecentActivitiesUseCase,
    private val seedStopwatchSampleDataUseCase: SeedStopwatchSampleDataUseCase,
    private val clearActivitiesUseCase: ClearActivitiesUseCase,
) : ViewModel() {

    val uiState: StateFlow<StopwatchUiState> = combine(
        observeCurrentActivityUseCase(),
        observeRecentActivitiesUseCase(),
    ) { currentActivity, recentActivities ->
        StopwatchUiState(
            currentActivity = currentActivity,
            recentActivities = recentActivities,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StopwatchUiState(isLoading = true),
    )

    fun seedSampleData() {
        viewModelScope.launch {
            seedStopwatchSampleDataUseCase()
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            clearActivitiesUseCase()
        }
    }
}
