package com.humans.aura.features.voice.domain

import com.humans.aura.core.domain.interfaces.TextToSpeechEngine

class SpeakAssistantReplyUseCase(
    private val textToSpeechEngine: TextToSpeechEngine,
) {
    suspend operator fun invoke(text: String) {
        textToSpeechEngine.speak(text)
    }
}
