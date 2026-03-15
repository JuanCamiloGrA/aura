package com.humans.aura

import android.app.Application
import androidx.work.Configuration
import com.humans.aura.core.coordination.AppIntentCoordinator
import com.humans.aura.core.di.appModules
import com.humans.aura.core.domain.interfaces.TextToSpeechEngine
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
        koinApplication.koin.get<AppIntentCoordinator>().start()
    }

    override fun onTerminate() {
        koinApplication.koin.get<AppIntentCoordinator>().stop()
        koinApplication.koin.get<TextToSpeechEngine>().shutdown()
        super.onTerminate()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(koinApplication.koin.get<AuraWorkerFactory>())
            .build()
}
