package com.humans.aura.features.voice.domain

import com.humans.aura.core.domain.interfaces.AudioTranscriber
import com.humans.aura.core.domain.models.AudioTranscription
import com.humans.aura.core.domain.models.RecordedAudio
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TranscribeAudioUseCaseTest {

    @Test
    fun invoke_delegates_to_audio_transcriber() = runTest {
        val transcriber = FakeAudioTranscriber()
        val audio = RecordedAudio(filePath = "clip.m4a", mimeType = "audio/mp4", displayName = "clip.m4a")

        val result = TranscribeAudioUseCase(transcriber).invoke(audio)

        assertEquals(audio, transcriber.receivedAudio)
        assertEquals("Go out for lunch", result.transcription)
        assertEquals(95, result.confidence)
    }

    private class FakeAudioTranscriber : AudioTranscriber {
        var receivedAudio: RecordedAudio? = null

        override suspend fun transcribe(audio: RecordedAudio): AudioTranscription {
            receivedAudio = audio
            return AudioTranscription(
                transcription = "Go out for lunch",
                confidence = 95,
            )
        }
    }
}
