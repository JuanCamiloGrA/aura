package com.humans.aura.core.services.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.humans.aura.core.domain.interfaces.TextToSpeechEngine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

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

    override fun shutdown() {
        speaker.shutdown()
    }
}

interface TextToSpeechSpeaker {
    suspend fun speak(text: String)

    fun stop()

    fun shutdown()
}

private class AndroidPlatformTextToSpeechSpeaker(
    context: Context,
    textToSpeechFactory: (Context, TextToSpeech.OnInitListener?) -> TextToSpeech,
) : TextToSpeechSpeaker {
    private var initialized = false
    private val textToSpeech: TextToSpeech?

    init {
        textToSpeech = textToSpeechFactory(context, TextToSpeech.OnInitListener { status ->
            val initialized = status == TextToSpeech.SUCCESS
            this.initialized = initialized
            if (initialized) {
                textToSpeech?.language = Locale.US
            }
        })
    }

    override suspend fun speak(text: String) {
        val engine = textToSpeech ?: throw IllegalStateException("Text to speech is unavailable")
        if (text.isBlank()) return
        if (!initialized) {
            throw IllegalStateException("Text to speech is not initialized")
        }
        suspendCancellableCoroutine { continuation ->
            val utteranceId = "aura-tts-${System.nanoTime()}"
            engine.setOnUtteranceProgressListener(
                object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) = Unit

                    override fun onDone(utteranceId: String?) {
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(IllegalStateException("Text to speech failed"))
                        }
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(IllegalStateException("Text to speech failed: $errorCode"))
                        }
                    }
                },
            )
            val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            if (result == TextToSpeech.ERROR && continuation.isActive) {
                continuation.resumeWithException(IllegalStateException("Text to speech failed"))
            }
            continuation.invokeOnCancellation { engine.stop() }
        }
    }

    override fun stop() {
        textToSpeech?.stop()
    }

    override fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}
