package com.humans.aura.core.services.ai

import com.humans.aura.core.domain.interfaces.AiCredentialsProvider
import com.humans.aura.core.domain.models.AiGenerationException
import com.humans.aura.core.domain.models.AudioTranscription
import com.humans.aura.core.domain.models.RecordedAudio
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.IOException

class GeminiAudioTranscriberTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Test
    fun transcribe_uploads_audio_and_returns_structured_transcription() = runTest {
        val tempFile = createAudioFile()
        val requests = mutableListOf<String>()
        val client = HttpClient(MockEngine { request ->
            requests += request.url.toString()
            when {
                request.url.toString().endsWith("/upload/v1beta/files") -> respond(
                    content = "",
                    status = HttpStatusCode.Created,
                    headers = headersOf("X-Goog-Upload-URL", "https://upload.example.com/session"),
                )

                request.url.toString() == "https://upload.example.com/session" -> respond(
                    content = """
                        {"file":{"name":"files/abc","uri":"https://generativelanguage.googleapis.com/v1beta/files/abc","mimeType":"audio/mp4"}}
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = jsonHeaders(),
                )

                request.url.toString().endsWith("/v1beta/files/abc") -> respond(
                    content = """
                        {"name":"files/abc","uri":"https://generativelanguage.googleapis.com/v1beta/files/abc","mimeType":"audio/mp4","state":"ACTIVE"}
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = jsonHeaders(),
                )

                request.url.toString().contains(":generateContent") -> respond(
                    content = """
                        {"candidates":[{"content":{"parts":[{"text":"{\"transcription\":\"Go out for lunch\",\"confidence\":95}"}]}}]}
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = jsonHeaders(),
                )

                request.method.value == "DELETE" -> respond(
                    content = "",
                    status = HttpStatusCode.NoContent,
                    headers = headersOf(),
                )

                else -> error("Unexpected request: ${request.url}")
            }
        }) {
            install(ContentNegotiation) {
                json(json)
            }
            expectSuccess = true
        }

        val transcriber = GeminiAudioTranscriber(
            credentialsProvider = AiCredentialsProvider { "api-key" },
            modelSelector = AiModelSelector(),
            json = json,
            client = client,
        )

        val result = transcriber.transcribe(
            RecordedAudio(
                filePath = tempFile.absolutePath,
                mimeType = "audio/mp4",
                displayName = tempFile.name,
            ),
        )

        assertEquals(AudioTranscription("Go out for lunch", 95), result)
        assertTrue(requests.any { it.contains("gemini-flash-lite-latest:generateContent") })
        assertFalse(tempFile.exists())
    }

    @Test
    fun transcribe_fails_when_audio_file_missing() = runTest {
        val transcriber = GeminiAudioTranscriber(
            credentialsProvider = AiCredentialsProvider { "api-key" },
            modelSelector = AiModelSelector(),
            json = json,
            client = simpleClient(),
        )

        val error = runCatching {
            transcriber.transcribe(RecordedAudio("missing.m4a", "audio/mp4", "missing.m4a"))
        }.exceptionOrNull()

        assertTrue(error is AiGenerationException.NonRetryable)
        assertEquals("Recorded audio file is empty or missing", error?.message)
    }

    @Test
    fun transcribe_maps_io_error_to_retryable_exception() = runTest {
        val tempFile = createAudioFile()
        val client = HttpClient(MockEngine { throw IOException("offline") }) {
            install(ContentNegotiation) {
                json(json)
            }
        }
        val transcriber = GeminiAudioTranscriber(
            credentialsProvider = AiCredentialsProvider { "api-key" },
            modelSelector = AiModelSelector(),
            json = json,
            client = client,
        )

        val error = runCatching {
            transcriber.transcribe(RecordedAudio(tempFile.absolutePath, "audio/mp4", tempFile.name))
        }.exceptionOrNull()

        assertTrue(error is AiGenerationException.Retryable)
        assertFalse(tempFile.exists())
    }

    private fun createAudioFile(): File = kotlin.io.path.createTempFile(suffix = ".m4a").toFile().apply {
        writeBytes(byteArrayOf(1, 2, 3))
    }

    private fun simpleClient(): HttpClient = HttpClient(MockEngine { error("unused") }) {
        install(ContentNegotiation) {
            json(json)
        }
        expectSuccess = true
    }

    private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
}
