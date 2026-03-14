package com.humans.aura.features.day_summary.presentation

import com.humans.aura.core.domain.models.DaySummary

data class DaySummaryUiState(
    val latestSummary: DaySummary? = null,
    val recentSummaries: List<DaySummary> = emptyList(),
    val isLoading: Boolean = false,
)
