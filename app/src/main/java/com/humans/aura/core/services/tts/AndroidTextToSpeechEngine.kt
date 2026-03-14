package com.humans.aura.core.services.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import com.humans.aura.core.domain.interfaces.TextToSpeechEngine
import java.util.Locale

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
    fun speak(text: String)

    fun stop()
}

private class AndroidPlatformTextToSpeechSpeaker(
    context: Context,
    textToSpeechFactory: (Context, TextToSpeech.OnInitListener?) -> TextToSpeech,
) : TextToSpeechSpeaker {
    private val textToSpeech = textToSpeechFactory(context, null).apply {
        language = Locale.ENGLISH
    }

    override fun speak(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "aura-tts")
    }

    override fun stop() {
        textToSpeech.stop()
    }
}
