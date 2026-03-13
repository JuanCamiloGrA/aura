package com.humans.aura.core.services.time

import org.junit.Assert.assertTrue
import org.junit.Test

class SystemTimeProviderTest {

    @Test
    fun current_day_start_is_not_after_current_time() {
        val provider = SystemTimeProvider()

        assertTrue(provider.currentDayStartEpochMillis() <= provider.currentTimeMillis())
    }
}
