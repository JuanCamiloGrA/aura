package com.humans.aura

import android.app.Application
import androidx.work.Configuration
import com.humans.aura.core.di.appModules
import com.humans.aura.core.services.sync.AuraWorkerFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

class AuraApplication : Application(), Configuration.Provider {

    private val koinApplication: KoinApplication by lazy {
        startKoin {
            androidContext(this@AuraApplication)
            modules(appModules)
        }
    }

    override fun onCreate() {
        super.onCreate()
        koinApplication
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(koinApplication.koin.get<AuraWorkerFactory>())
            .build()
}
