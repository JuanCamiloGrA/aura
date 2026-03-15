package com.humans.aura.features.voice.domain

import com.humans.aura.core.domain.models.AudioTranscription
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShouldAcceptTranscriptionUseCaseTest {

    @Test
    fun invoke_returns_true_when_transcription_is_confident_and_not_blank() {
        val useCase = ShouldAcceptTranscriptionUseCase(minimumConfidence = 70)

        assertTrue(useCase(AudioTranscription("Go out for lunch", 95)))
    }

    @Test
    fun invoke_returns_false_when_confidence_is_low_or_transcription_blank() {
        val useCase = ShouldAcceptTranscriptionUseCase(minimumConfidence = 70)

        assertFalse(useCase(AudioTranscription("Go out for lunch", 45)))
        assertFalse(useCase(AudioTranscription("   ", 95)))
    }
}
