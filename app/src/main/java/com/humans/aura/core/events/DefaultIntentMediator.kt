package com.humans.aura.core.events

import com.humans.aura.core.domain.interfaces.IntentMediator
import com.humans.aura.core.domain.models.AppIntent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class DefaultIntentMediator : IntentMediator {

    private val mutableIntents = MutableSharedFlow<AppIntent>(extraBufferCapacity = 1)

    override val intents: SharedFlow<AppIntent> = mutableIntents.asSharedFlow()

    override suspend fun emit(intent: AppIntent) {
        mutableIntents.emit(intent)
    }
}
