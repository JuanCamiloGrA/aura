package com.humans.aura.core.di

import com.humans.aura.features.daily_goals.domain.ClearTodayGoalUseCase
import com.humans.aura.features.daily_goals.domain.ObserveTodayGoalUseCase
import com.humans.aura.features.daily_goals.domain.SeedTodayGoalUseCase
import com.humans.aura.features.stopwatch.domain.ClearActivitiesUseCase
import com.humans.aura.features.stopwatch.domain.ObserveCurrentActivityUseCase
import com.humans.aura.features.stopwatch.domain.ObserveRecentActivitiesUseCase
import com.humans.aura.features.stopwatch.domain.SeedStopwatchSampleDataUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { ObserveCurrentActivityUseCase(get()) }
    factory { ObserveRecentActivitiesUseCase(get()) }
    factory { SeedStopwatchSampleDataUseCase(get()) }
    factory { ClearActivitiesUseCase(get()) }
    factory { ObserveTodayGoalUseCase(get()) }
    factory { SeedTodayGoalUseCase(get()) }
    factory { ClearTodayGoalUseCase(get()) }
}
