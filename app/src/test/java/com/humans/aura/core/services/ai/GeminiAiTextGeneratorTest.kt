package com.humans.aura.core.services.ai

import com.humans.aura.core.domain.models.AiGenerationException
import com.humans.aura.core.domain.models.AiRequest
import com.humans.aura.core.domain.models.AiTask
import com.humans.aura.core.domain.models.ChatMessage
import com.humans.aura.core.domain.models.ChatRole
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class GeminiAiTextGeneratorTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Test
    fun generate_returns_text_and_selected_model() = runTest {
        val client = mockClient(
            status = HttpStatusCode.OK,
            body = """
                {"candidates":[{"content":{"parts":[{"text":"Hello back"}]}}]}
            """.trimIndent(),
        )
        val generator = GeminiAiTextGenerator(
            apiKeyProvider = GeminiApiKeyProvider { "api-key" },
            modelSelector = GeminiModelSelector(),
            json = json,
            client = client,
        )

        val response = generator.generate(
            AiRequest(
                task = AiTask.CHAT,
                systemInstruction = "system",
                prompt = "prompt",
                conversationHistory = listOf(ChatMessage(1, 1, ChatRole.USER, "hola", "hello", "es", 1L, false)),
            ),
        )

        assertEquals("Hello back", response.text)
        assertEquals("gemini-2.5-flash-lite", response.modelName)
    }

    @Test
    fun generate_maps_retryable_http_error() = runTest {
        val generator = GeminiAiTextGenerator(
            apiKeyProvider = GeminiApiKeyProvider { "api-key" },
            modelSelector = GeminiModelSelector(),
            json = json,
            client = mockClient(HttpStatusCode.TooManyRequests, "{}"),
        )

        val error = runCatching {
            generator.generate(AiRequest(AiTask.CHAT, "system", "prompt"))
        }.exceptionOrNull()

        assertTrue(error is AiGenerationException.Retryable)
    }

    @Test
    fun generate_maps_non_retryable_http_error() = runTest {
        val generator = GeminiAiTextGenerator(
            apiKeyProvider = GeminiApiKeyProvider { "api-key" },
            modelSelector = GeminiModelSelector(),
            json = json,
            client = mockClient(HttpStatusCode.BadRequest, "{}"),
        )

        val error = runCatching {
            generator.generate(AiRequest(AiTask.CHAT, "system", "prompt"))
        }.exceptionOrNull()

        assertTrue(error is AiGenerationException.NonRetryable)
    }

    @Test
    fun generate_maps_io_error_to_retryable_exception() = runTest {
        val client = HttpClient(MockEngine { throw IOException("offline") }) {
            install(ContentNegotiation) {
                json(json)
            }
        }
        val generator = GeminiAiTextGenerator(
            apiKeyProvider = GeminiApiKeyProvider { "api-key" },
            modelSelector = GeminiModelSelector(),
            json = json,
            client = client,
        )

        val error = runCatching {
            generator.generate(AiRequest(AiTask.DAY_SUMMARY, "system", "prompt"))
        }.exceptionOrNull()

        assertTrue(error is AiGenerationException.Retryable)
    }

    @Test
    fun model_selector_uses_expected_models() {
        val selector = GeminiModelSelector()

        assertEquals("gemini-2.5-flash", selector.modelFor(AiTask.DAY_SUMMARY))
        assertEquals("gemini-2.5-flash-lite", selector.modelFor(AiTask.CHAT))
        assertEquals("gemini-2.5-flash-lite", selector.modelFor(AiTask.TRANSLATION))
    }

    private fun mockClient(
        status: HttpStatusCode,
        body: String,
    ): HttpClient = HttpClient(MockEngine {
        respond(
            content = body,
            status = status,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )
    }) {
        install(ContentNegotiation) {
            json(json)
        }
        expectSuccess = true
    }
}
