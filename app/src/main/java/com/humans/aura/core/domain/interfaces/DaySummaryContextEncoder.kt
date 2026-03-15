package com.humans.aura.core.domain.interfaces

import com.humans.aura.core.domain.models.DaySummaryContext

interface DaySummaryContextEncoder {
    fun encode(context: DaySummaryContext): String
}
