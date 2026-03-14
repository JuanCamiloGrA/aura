package com.humans.aura.features.day_summary.domain

import com.humans.aura.core.domain.interfaces.DaySummaryRepository
import com.humans.aura.core.domain.interfaces.TimeProvider

class CreatePendingDaySummaryUseCase(
    private val daySummaryRepository: DaySummaryRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke() = daySummaryRepository.createPendingSummary(timeProvider.currentDayStartEpochMillis())
}
