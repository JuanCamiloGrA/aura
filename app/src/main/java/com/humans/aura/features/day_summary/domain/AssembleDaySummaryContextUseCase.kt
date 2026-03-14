package com.humans.aura.features.day_summary.domain

import com.humans.aura.core.domain.interfaces.ConversationContextRepository
import com.humans.aura.core.domain.interfaces.TimeProvider

class AssembleDaySummaryContextUseCase(
    private val conversationContextRepository: ConversationContextRepository,
    private val timeProvider: TimeProvider,
) {
    open suspend operator fun invoke(dayStartEpochMillis: Long = timeProvider.currentDayStartEpochMillis()) =
        conversationContextRepository.buildContextForDay(dayStartEpochMillis)
}
