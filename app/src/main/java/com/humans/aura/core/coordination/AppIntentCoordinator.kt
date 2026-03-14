package com.humans.aura.core.coordination

import com.humans.aura.core.domain.interfaces.IntentMediator
import com.humans.aura.core.domain.models.AppIntent
import com.humans.aura.features.day_closure.domain.HandleSleepIntentUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AppIntentCoordinator(
    private val intentMediator: IntentMediator,
    private val handleSleepIntentUseCase: HandleSleepIntentUseCase,
    private val appScope: CoroutineScope,
) {
    private var coordinatorJob: Job? = null

    fun start() {
        if (coordinatorJob != null) return

        coordinatorJob = appScope.launch {
            intentMediator.intents.collect { intent ->
                when (intent) {
                    is AppIntent.SleepLogged -> handleSleepIntentUseCase(intent)
                }
            }
        }
    }

    fun stop() {
        coordinatorJob?.cancel()
        coordinatorJob = null
    }
}
