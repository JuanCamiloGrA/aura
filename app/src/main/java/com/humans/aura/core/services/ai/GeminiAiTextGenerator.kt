package com.humans.aura.core.services.ai

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
    private val apiKeyProvider: GeminiApiKeyProvider,
    private val modelSelector: GeminiModelSelector,
    private val json: Json,
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    },
) : AiTextGenerator {

    override suspend fun generate(request: AiRequest): AiResponse {
        val modelName = modelSelector.modelFor(request.task)
        val response: GeminiGenerateContentResponse = try {
            client.post(
                urlString = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent",
            ) {
                header("x-goog-api-key", apiKeyProvider.requireApiKey())
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
                message = error.message ?: "Network error while contacting Gemini",
                cause = error,
            )
        } catch (error: ResponseException) {
            val statusCode = error.response.status.value
            if (statusCode == 429 || statusCode >= 500) {
                throw AiGenerationException.Retryable(
                    message = "Gemini request failed with HTTP $statusCode",
                    cause = error,
                )
            } else {
                throw AiGenerationException.NonRetryable(
                    message = "Gemini request failed with HTTP $statusCode",
                    cause = error,
                )
            }
        } catch (error: IllegalStateException) {
            throw AiGenerationException.NonRetryable(
                message = error.message ?: "Invalid Gemini response",
                cause = error,
            )
        }

        val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text.orEmpty()
        return AiResponse(text = text, modelName = modelName)
    }
}

fun interface GeminiApiKeyProvider {
    fun requireApiKey(): String
}

class GeminiModelSelector {
    fun modelFor(task: com.humans.aura.core.domain.models.AiTask): String = when (task) {
        com.humans.aura.core.domain.models.AiTask.DAY_SUMMARY -> "gemini-2.5-flash"
        com.humans.aura.core.domain.models.AiTask.CHAT,
        com.humans.aura.core.domain.models.AiTask.TRANSLATION,
        -> "gemini-2.5-flash-lite"
    }
}

@Serializable
private data class GeminiGenerateContentRequest(
    @SerialName("system_instruction")
    val systemInstruction: GeminiContent,
    val contents: List<GeminiContent>,
)

@Serializable
private data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate> = emptyList(),
)

@Serializable
private data class GeminiCandidate(
    val content: GeminiContent,
)

@Serializable
private data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>,
)

@Serializable
private data class GeminiPart(
    val text: String,
)
