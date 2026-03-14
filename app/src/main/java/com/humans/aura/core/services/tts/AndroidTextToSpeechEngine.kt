package com.humans.aura.core.services.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.humans.aura.core.domain.interfaces.TextToSpeechEngine
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class AndroidTextToSpeechEngine(
    private val speaker: TextToSpeechSpeaker,
) : TextToSpeechEngine {

    constructor(
        context: Context,
        textToSpeechFactory: (Context, TextToSpeech.OnInitListener?) -> TextToSpeech = ::TextToSpeech,
    ) : this(
        speaker = AndroidPlatformTextToSpeechSpeaker(context, textToSpeechFactory),
    )

    override suspend fun speak(text: String) {
        speaker.speak(text)
    }

    override fun stop() {
        speaker.stop()
    }
}

interface TextToSpeechSpeaker {
    suspend fun speak(text: String)

    fun stop()
}

private class AndroidPlatformTextToSpeechSpeaker(
    context: Context,
    textToSpeechFactory: (Context, TextToSpeech.OnInitListener?) -> TextToSpeech,
) : TextToSpeechSpeaker {
    private val isInitialized = CompletableDeferred<Boolean>()
    private val textToSpeech = textToSpeechFactory(context, TextToSpeech.OnInitListener { status ->
        val initialized = status == TextToSpeech.SUCCESS
        if (!isInitialized.isCompleted) {
            isInitialized.complete(initialized)
        }
    }).apply {
        language = Locale.ENGLISH
    }

    override suspend fun speak(text: String) {
        if (text.isBlank()) return
        val initialized = isInitialized.await()
        if (!initialized) return

        withContext(Dispatchers.Main.immediate) {
            suspendCancellableCoroutine { continuation ->
                val utteranceId = "aura-tts-${System.nanoTime()}"
                textToSpeech.setOnUtteranceProgressListener(
                    object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) = Unit

                        override fun onDone(completedUtteranceId: String?) {
                            if (completedUtteranceId == utteranceId && continuation.isActive) {
                                continuation.resume(Unit)
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(completedUtteranceId: String?) {
                            if (completedUtteranceId == utteranceId && continuation.isActive) {
                                continuation.resume(Unit)
                            }
                        }

                        override fun onError(completedUtteranceId: String?, errorCode: Int) {
                            if (completedUtteranceId == utteranceId && continuation.isActive) {
                                continuation.resume(Unit)
                            }
                        }
                    },
                )

                val result = textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                if (result == TextToSpeech.ERROR && continuation.isActive) {
                    continuation.resume(Unit)
                }
                continuation.invokeOnCancellation { textToSpeech.stop() }
            }
        }
    }

    override fun stop() {
        textToSpeech.stop()
    }
}
