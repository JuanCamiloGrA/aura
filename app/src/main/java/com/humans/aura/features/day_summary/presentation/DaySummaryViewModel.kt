package com.humans.aura.features.day_summary.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humans.aura.features.day_summary.domain.ObserveLatestSummaryUseCase
import com.humans.aura.features.day_summary.domain.ObserveRecentSummariesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DaySummaryViewModel(
    observeLatestSummaryUseCase: ObserveLatestSummaryUseCase,
    observeRecentSummariesUseCase: ObserveRecentSummariesUseCase,
) : ViewModel() {

    val uiState: StateFlow<DaySummaryUiState> = combine(
        observeLatestSummaryUseCase(),
        observeRecentSummariesUseCase(),
    ) { latestSummary, recentSummaries ->
        DaySummaryUiState(
            latestSummary = latestSummary,
            recentSummaries = recentSummaries,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DaySummaryUiState(isLoading = true),
    )
}
