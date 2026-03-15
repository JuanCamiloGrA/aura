package com.humans.aura.core.domain.interfaces

interface AppLaunchRepository {
    suspend fun hasCompletedInitialStopwatchBootstrap(): Boolean

    suspend fun markInitialStopwatchBootstrapCompleted()
}
