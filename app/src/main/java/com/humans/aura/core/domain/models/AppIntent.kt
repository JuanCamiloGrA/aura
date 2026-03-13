package com.humans.aura.core.domain.models

sealed interface AppIntent {
    data class SleepLogged(
        val activityTitle: String,
        val occurredAtEpochMillis: Long,
    ) : AppIntent
}
