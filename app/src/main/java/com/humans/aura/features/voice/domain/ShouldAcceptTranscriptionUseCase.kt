package com.humans.aura.features.voice.domain

import com.humans.aura.core.domain.models.AudioTranscription

class ShouldAcceptTranscriptionUseCase(
    private val minimumConfidence: Int = DEFAULT_MINIMUM_CONFIDENCE,
) {
    operator fun invoke(transcription: AudioTranscription): Boolean =
        transcription.transcription.isNotBlank() && transcription.confidence >= minimumConfidence

    companion object {
        const val DEFAULT_MINIMUM_CONFIDENCE = 70
    }
}
