package com.humans.aura.features.voice.domain

import com.humans.aura.core.domain.interfaces.TextToSpeechEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SpeakAssistantReplyUseCaseTest {

    @Test
    fun invoke_delegates_to_tts_engine() = runTest {
        val engine = FakeTextToSpeechEngine()

        SpeakAssistantReplyUseCase(engine).invoke("You did well today")

        assertEquals(listOf("You did well today"), engine.spokenTexts)
    }

    private class FakeTextToSpeechEngine : TextToSpeechEngine {
        val spokenTexts = mutableListOf<String>()

        override suspend fun speak(text: String) {
            spokenTexts += text
        }

        override fun stop() = Unit
    }
}
