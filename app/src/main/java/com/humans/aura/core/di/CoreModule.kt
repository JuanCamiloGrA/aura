package com.humans.aura.core.di

import com.humans.aura.core.domain.interfaces.IntentMediator
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.events.DefaultIntentMediator
import com.humans.aura.core.services.time.SystemTimeProvider
import org.koin.dsl.module

val coreModule = module {
    single<IntentMediator> { DefaultIntentMediator() }
    single<TimeProvider> { SystemTimeProvider() }
}
