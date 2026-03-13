package com.humans.aura.core.domain.models

sealed interface AppIntent {
    data object SleepLogged : AppIntent
}
