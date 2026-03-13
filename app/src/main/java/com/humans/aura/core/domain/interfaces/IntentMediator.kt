package com.humans.aura.core.domain.interfaces

import com.humans.aura.core.domain.models.AppIntent
import kotlinx.coroutines.flow.SharedFlow

interface IntentMediator {
    val intents: SharedFlow<AppIntent>

    suspend fun emit(intent: AppIntent)
}
