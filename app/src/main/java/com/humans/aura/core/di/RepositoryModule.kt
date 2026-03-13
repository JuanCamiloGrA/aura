package com.humans.aura.core.di

import com.humans.aura.core.domain.interfaces.ActivityRepository
import com.humans.aura.core.domain.interfaces.DailyGoalRepository
import com.humans.aura.features.daily_goals.data.RoomDailyGoalRepository
import com.humans.aura.features.stopwatch.data.RoomActivityRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<ActivityRepository> { RoomActivityRepository(get(), get(), get()) }
    single<DailyGoalRepository> { RoomDailyGoalRepository(get(), get()) }
}
