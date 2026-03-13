package com.humans.aura.core.di

import androidx.work.WorkManager
import com.humans.aura.core.domain.interfaces.IntentMediator
import com.humans.aura.core.domain.interfaces.SyncScheduler
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.interfaces.WallpaperController
import com.humans.aura.core.events.DefaultIntentMediator
import com.humans.aura.core.services.sync.WorkManagerSyncScheduler
import com.humans.aura.core.services.time.SystemTimeProvider
import com.humans.aura.core.services.wallpaper.AndroidWallpaperController
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val coreModule = module {
    single<IntentMediator> { DefaultIntentMediator() }
    single<TimeProvider> { SystemTimeProvider() }
    single { WorkManager.getInstance(androidApplication()) }
    single<SyncScheduler> { WorkManagerSyncScheduler(get()) }
    single<WallpaperController> { AndroidWallpaperController() }
}
