package com.humans.aura.core.domain.interfaces

import com.humans.aura.core.domain.models.DaySummaryContext

interface ConversationContextRepository {
    suspend fun buildContextForDay(dayStartEpochMillis: Long): DaySummaryContext

    suspend fun buildChatContext(limit: Int = 7): DaySummaryContext
}
