package com.humans.aura.core.domain.interfaces

import kotlinx.coroutines.flow.Flow

interface CurrentTimeTicker {
    fun tickEvery(intervalMillis: Long): Flow<Long>
}
