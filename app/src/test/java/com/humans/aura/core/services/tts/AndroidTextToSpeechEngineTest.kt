package com.humans.aura.core.services.tts

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AndroidTextToSpeechEngineTest {

    @Test
    fun speak_delegates_to_speaker() = runTest {
        val speaker = FakeTextToSpeechSpeaker()
        val engine = AndroidTextToSpeechEngine(speaker)

        engine.speak("hello")

        assertEquals(listOf("hello"), speaker.spokenTexts)
    }

    @Test
    fun stop_delegates_to_speaker() {
        val speaker = FakeTextToSpeechSpeaker()
        val engine = AndroidTextToSpeechEngine(speaker)

        engine.stop()

        assertEquals(1, speaker.stopCalls)
    }

    private class FakeTextToSpeechSpeaker : TextToSpeechSpeaker {
        val spokenTexts = mutableListOf<String>()
        var stopCalls = 0

        override fun speak(text: String) {
            spokenTexts += text
        }

        override fun stop() {
            stopCalls += 1
        }
    }
}
