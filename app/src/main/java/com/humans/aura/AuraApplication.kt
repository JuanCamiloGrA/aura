package com.humans.aura

import android.app.Application
import com.humans.aura.core.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AuraApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@AuraApplication)
            modules(appModules)
        }
    }
}
