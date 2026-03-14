package com.humans.aura.core.di

import androidx.room.Room
import com.humans.aura.core.services.database.AuraDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AuraDatabase::class.java,
            "aura.db",
        ).addMigrations(AuraDatabase.MIGRATION_1_2, AuraDatabase.MIGRATION_2_3).build()
    }

    single { get<AuraDatabase>().activityDao() }
    single { get<AuraDatabase>().dailyGoalDao() }
    single { get<AuraDatabase>().daySummaryDao() }
    single { get<AuraDatabase>().chatDao() }
}
