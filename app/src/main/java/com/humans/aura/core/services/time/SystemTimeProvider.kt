package com.humans.aura.core.services.time

import com.humans.aura.core.domain.interfaces.TimeProvider
import java.time.Instant
import java.time.ZoneId

class SystemTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()

    override fun currentDayStartEpochMillis(): Long =
        Instant.ofEpochMilli(currentTimeMillis())
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
}
