package com.humans.aura.features.voice.domain

import com.humans.aura.core.domain.interfaces.AiTextGenerator
import com.humans.aura.core.domain.models.AiRequest
import com.humans.aura.core.domain.models.AiResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class NormalizeTranscriptToEnglishUseCaseTest {

    @Test
    fun invoke_returns_blank_input_without_calling_ai() = runTest {
        val generator = FakeAiTextGenerator()

        val result = NormalizeTranscriptToEnglishUseCase(generator).invoke("   ")

        assertEquals("   ", result)
        assertEquals(0, generator.requests.size)
    }

    @Test
    fun invoke_translates_non_blank_transcript() = runTest {
        val generator = FakeAiTextGenerator(response = AiResponse("I am going to sleep", "gemini-test"))

        val result = NormalizeTranscriptToEnglishUseCase(generator).invoke("me voy a dormir")

        assertEquals("I am going to sleep", result)
        assertEquals("me voy a dormir", generator.requests.single().prompt)
    }

    private class FakeAiTextGenerator(
        private val response: AiResponse = AiResponse("translated", "gemini-test"),
    ) : AiTextGenerator {
        val requests = mutableListOf<AiRequest>()

        override suspend fun generate(request: AiRequest): AiResponse {
            requests += request
            return response
        }
    }
}
