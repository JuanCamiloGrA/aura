package com.humans.aura.features.day_summary.domain

import com.humans.aura.core.domain.interfaces.DaySummaryRepository

class ObserveLatestSummaryUseCase(
    private val daySummaryRepository: DaySummaryRepository,
) {
    operator fun invoke() = daySummaryRepository.observeLatestSummary()
}
