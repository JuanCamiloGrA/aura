package com.humans.aura.core.services.ai

import com.humans.aura.core.domain.interfaces.AiCredentialsProvider
import com.humans.aura.core.domain.interfaces.AiTextGenerator
import com.humans.aura.core.domain.models.AiGenerationException
import com.humans.aura.core.domain.models.AiRequest
import com.humans.aura.core.domain.models.AiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException

class GeminiAiTextGenerator(
    private val credentialsProvider: AiCredentialsProvider,
    private val modelSelector: AiModelSelector,
    private val json: Json,
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    },
) : AiTextGenerator {

    override suspend fun generate(request: AiRequest): AiResponse {
        val apiKey = credentialsProvider.requireApiKey().trim()
        if (apiKey.isBlank()) {
            throw AiGenerationException.NonRetryable("AI API key is missing")
        }
        val modelName = modelSelector.modelFor(request.task)
        val response: GeminiGenerateContentResponse = try {
            client.post(
                urlString = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent",
            ) {
                header("x-goog-api-key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(
                    GeminiGenerateContentRequest(
                        systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = request.systemInstruction))),
                        contents = buildList {
                            request.conversationHistory.forEach { message ->
                                add(
                                    GeminiContent(
                                        role = if (message.role == com.humans.aura.core.domain.models.ChatRole.USER) "user" else "model",
                                        parts = listOf(GeminiPart(text = message.normalizedEnglishText)),
                                    ),
                                )
                            }
                            add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = request.prompt))))
                        },
                    ),
                )
            }.body()
        } catch (error: IOException) {
            throw AiGenerationException.Retryable(
                message = error.message ?: "Network error while contacting AI provider",
                cause = error,
            )
        } catch (error: ResponseException) {
            val statusCode = error.response.status.value
            if (statusCode == 429 || statusCode >= 500) {
                throw AiGenerationException.Retryable(
                    message = "AI request failed with HTTP $statusCode",
                    cause = error,
                )
            } else {
                throw AiGenerationException.NonRetryable(
                    message = "AI request failed with HTTP $statusCode",
                    cause = error,
                )
            }
        } catch (error: IllegalStateException) {
            throw AiGenerationException.NonRetryable(
                message = error.message ?: "Invalid AI response",
                cause = error,
            )
        }

        val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text.orEmpty()
        if (text.isBlank()) {
            throw AiGenerationException.NonRetryable("AI provider returned an empty response")
        }
        return AiResponse(text = text, modelName = modelName)
    }
}

class AiModelSelector {
    fun modelFor(task: com.humans.aura.core.domain.models.AiTask): String = when (task) {
        com.humans.aura.core.domain.models.AiTask.DAY_SUMMARY,
        com.humans.aura.core.domain.models.AiTask.CHAT,
        -> "gemini-flash-latest"
        com.humans.aura.core.domain.models.AiTask.AUDIO_TRANSCRIPTION,
        com.humans.aura.core.domain.models.AiTask.TRANSLATION,
        -> "gemini-flash-lite-latest"
    }
}

@Serializable
internal data class GeminiGenerateContentRequest(
    @SerialName("system_instruction")
    val systemInstruction: GeminiContent,
    val contents: List<GeminiContent>,
)

@Serializable
internal data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate> = emptyList(),
)

@Serializable
internal data class GeminiCandidate(
    val content: GeminiContent,
)

@Serializable
internal data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>,
)

@Serializable
internal data class GeminiPart(
    val text: String? = null,
    @SerialName("file_data")
    val fileData: GeminiFileData? = null,
)

@Serializable
internal data class GeminiFileData(
    @SerialName("file_uri")
    val fileUri: String,
    @SerialName("mime_type")
    val mimeType: String,
)
