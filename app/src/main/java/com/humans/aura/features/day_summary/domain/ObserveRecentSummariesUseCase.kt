package com.humans.aura.features.day_summary.domain

import com.humans.aura.core.domain.interfaces.DaySummaryRepository

class ObserveRecentSummariesUseCase(
    private val daySummaryRepository: DaySummaryRepository,
) {
    operator fun invoke(limit: Int = 7) = daySummaryRepository.observeRecentSummaries(limit)
}
