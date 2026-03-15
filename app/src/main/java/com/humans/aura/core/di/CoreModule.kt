package com.humans.aura.core.di

import androidx.work.WorkManager
import com.humans.aura.core.domain.interfaces.AiTextGenerator
import com.humans.aura.core.domain.interfaces.AppLaunchRepository
import com.humans.aura.core.domain.interfaces.CurrentTimeTicker
import com.humans.aura.core.domain.interfaces.IntentMediator
import com.humans.aura.core.domain.interfaces.SpeechRecognizer
import com.humans.aura.core.domain.interfaces.SyncScheduler
import com.humans.aura.core.domain.interfaces.TextToSpeechEngine
import com.humans.aura.core.domain.interfaces.TimeProvider
import com.humans.aura.core.domain.interfaces.WallpaperController
import com.humans.aura.core.coordination.AppIntentCoordinator
import com.humans.aura.core.events.DefaultIntentMediator
import com.humans.aura.core.services.ai.BuildConfigGeminiApiKeyProvider
import com.humans.aura.core.services.ai.GeminiAiTextGenerator
import com.humans.aura.core.services.ai.GeminiApiKeyProvider
import com.humans.aura.core.services.ai.GeminiModelSelector
import com.humans.aura.core.services.speech.AndroidSpeechRecognizer
import com.humans.aura.core.services.sync.AuraWorkerFactory
import com.humans.aura.core.services.sync.WorkManagerSyncScheduler
import com.humans.aura.core.services.time.SystemCurrentTimeTicker
import com.humans.aura.core.services.time.SystemTimeProvider
import com.humans.aura.core.services.tts.AndroidTextToSpeechEngine
import com.humans.aura.core.services.wallpaper.AndroidWallpaperController
import com.humans.aura.core.services.preferences.SharedPreferencesAppLaunchRepository
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import kotlinx.serialization.json.Json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val coreModule = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single<IntentMediator> { DefaultIntentMediator() }
    single<TimeProvider> { SystemTimeProvider() }
    single<CurrentTimeTicker> { SystemCurrentTimeTicker(get()) }
    single<AppLaunchRepository> { SharedPreferencesAppLaunchRepository(androidApplication()) }
    single {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
    single { WorkManager.getInstance(androidApplication()) }
    single<SyncScheduler> { WorkManagerSyncScheduler(get()) }
    single<WallpaperController> { AndroidWallpaperController(androidApplication()) }
    single<GeminiApiKeyProvider> { BuildConfigGeminiApiKeyProvider() }
    single { GeminiModelSelector() }
    single<AiTextGenerator> { GeminiAiTextGenerator(get(), get(), get()) }
    single<SpeechRecognizer> { AndroidSpeechRecognizer(androidApplication()) }
    single<TextToSpeechEngine> { AndroidTextToSpeechEngine(androidApplication()) }
    single { AppIntentCoordinator(get(), get(), get()) }
    single { AuraWorkerFactory(get()) }
}
