package com.humans.aura.core.di

import com.humans.aura.features.daily_goals.domain.ClearTodayGoalUseCase
import com.humans.aura.features.daily_goals.domain.ObserveTodayActivitiesUseCase
import com.humans.aura.features.daily_goals.domain.ObserveTodayGoalUseCase
import com.humans.aura.features.daily_goals.domain.SaveTodayGoalUseCase
import com.humans.aura.features.day_closure.domain.HandleSleepIntentUseCase
import com.humans.aura.features.stopwatch.domain.ClearActivitiesUseCase
import com.humans.aura.features.stopwatch.domain.LogNewActivityUseCase
import com.humans.aura.features.stopwatch.domain.ObserveCurrentActivityUseCase
import com.humans.aura.features.stopwatch.domain.ObserveRecentActivitiesUseCase
import com.humans.aura.features.stopwatch.domain.PredictNextActivityTitleUseCase
import com.humans.aura.features.stopwatch.domain.UpdateCurrentActivityStatusUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { ObserveCurrentActivityUseCase(get()) }
    factory { ObserveRecentActivitiesUseCase(get()) }
    factory { LogNewActivityUseCase(get(), get()) }
    factory { PredictNextActivityTitleUseCase(get(), get()) }
    factory { UpdateCurrentActivityStatusUseCase(get()) }
    factory { ClearActivitiesUseCase(get()) }
    factory { ObserveTodayGoalUseCase(get()) }
    factory { ObserveTodayActivitiesUseCase(get(), get()) }
    factory { SaveTodayGoalUseCase(get()) }
    factory { ClearTodayGoalUseCase(get()) }
    factory { HandleSleepIntentUseCase(get(), get(), get()) }
}
