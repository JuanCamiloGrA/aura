package com.humans.aura.core.services.time

import com.humans.aura.core.domain.interfaces.CurrentTimeTicker
import com.humans.aura.core.domain.interfaces.TimeProvider
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

class SystemCurrentTimeTicker(
    private val timeProvider: TimeProvider,
) : CurrentTimeTicker {
    override fun tickEvery(intervalMillis: Long): Flow<Long> = flow {
        emit(timeProvider.currentTimeMillis())
        while (currentCoroutineContext().isActive) {
            delay(intervalMillis)
            emit(timeProvider.currentTimeMillis())
        }
    }
}
