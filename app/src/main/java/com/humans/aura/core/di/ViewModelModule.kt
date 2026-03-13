package com.humans.aura.core.di

import com.humans.aura.features.daily_goals.presentation.DailyGoalsViewModel
import com.humans.aura.features.stopwatch.presentation.StopwatchViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::StopwatchViewModel)
    viewModelOf(::DailyGoalsViewModel)
}
