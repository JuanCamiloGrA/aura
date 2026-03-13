package com.humans.aura.core.services.time

import com.humans.aura.core.domain.interfaces.TimeProvider

class SystemTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
