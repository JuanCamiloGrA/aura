package com.humans.aura.core.domain.interfaces

import com.humans.aura.core.domain.models.DaySummaryReflection

interface DaySummaryReflectionCodec {
    fun parse(summaryText: String?): DaySummaryReflection?

    fun encode(reflection: DaySummaryReflection): String
}
