package com.humans.aura.core.di

import org.junit.Test
import org.koin.dsl.module
import org.koin.test.verify.verify
import org.koin.test.verify.definition
import org.koin.test.verify.injectedParameters

class AppModulesTest {

    @Test
    fun app_modules_verify() {
        module {
            includes(*appModules.toTypedArray())
        }.verify(
            injections = injectedParameters(
                definition<com.humans.aura.core.services.preferences.SharedPreferencesAppLaunchRepository>(android.content.Context::class),
                definition<com.humans.aura.core.services.storage.AndroidBackupDocumentRepository>(android.content.Context::class),
                definition<com.humans.aura.core.services.speech.AndroidAudioRecorder>(android.content.Context::class),
                definition<com.humans.aura.core.services.tts.AndroidTextToSpeechEngine>(android.content.Context::class),
                definition<com.humans.aura.core.services.wallpaper.AndroidWallpaperController>(android.content.Context::class),
            ),
        )
    }
}
