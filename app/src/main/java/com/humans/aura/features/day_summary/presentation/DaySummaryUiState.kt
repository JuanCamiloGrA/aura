package com.humans.aura.features.day_summary.presentation

import com.humans.aura.core.domain.models.DaySummary
import com.humans.aura.core.domain.models.SummaryGenerationStatus

data class DaySummaryUiState(
    val latestSummary: DaySummary? = null,
    val recentSummaries: List<DaySummary> = emptyList(),
    val isLoading: Boolean = false,
)

val DaySummaryUiState.latestStatus: SummaryGenerationStatus?
    get() = latestSummary?.generationStatus
