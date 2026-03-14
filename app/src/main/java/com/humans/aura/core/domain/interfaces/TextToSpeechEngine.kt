package com.humans.aura.core.domain.interfaces

interface TextToSpeechEngine {
    suspend fun speak(text: String)

    fun stop()
}
