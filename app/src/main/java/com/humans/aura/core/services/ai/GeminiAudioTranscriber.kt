package com.humans.aura.core.services.ai

import com.humans.aura.core.domain.interfaces.AiCredentialsProvider
import com.humans.aura.core.domain.interfaces.AudioTranscriber
import com.humans.aura.core.domain.models.AiGenerationException
import com.humans.aura.core.domain.models.AiTask
import com.humans.aura.core.domain.models.AudioTranscription
import com.humans.aura.core.domain.models.RecordedAudio
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.IOException

class GeminiAudioTranscriber(
    private val credentialsProvider: AiCredentialsProvider,
    private val modelSelector: AiModelSelector,
    private val json: Json,
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        expectSuccess = true
    },
) : AudioTranscriber {

    override suspend fun transcribe(audio: RecordedAudio): AudioTranscription {
        val apiKey = credentialsProvider.requireApiKey().trim()
        if (apiKey.isBlank()) {
            throw AiGenerationException.NonRetryable("AI API key is missing")
        }

        val localFile = File(audio.filePath)
        if (!localFile.exists() || localFile.length() <= 0L) {
            throw AiGenerationException.NonRetryable("Recorded audio file is empty or missing")
        }

        var uploadedFileName: String? = null
        try {
            val uploadUrl = startUpload(apiKey, localFile, audio)
            val uploadedFile = uploadFile(uploadUrl, localFile, audio)
            uploadedFileName = uploadedFile.name
            val readyFile = waitUntilFileIsReady(apiKey, uploadedFile)
            val response: GeminiGenerateContentResponse = client.post(
                urlString = "https://generativelanguage.googleapis.com/v1beta/models/${modelSelector.modelFor(AiTask.AUDIO_TRANSCRIPTION)}:generateContent",
            ) {
                header("x-goog-api-key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(
                    GeminiGenerateAudioRequest(
                        systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = SYSTEM_INSTRUCTION))),
                        contents = listOf(
                            GeminiContent(
                                parts = listOf(
                                    GeminiPart(fileData = GeminiFileData(fileUri = readyFile.uri, mimeType = readyFile.mimeType)),
                                    GeminiPart(text = USER_PROMPT),
                                ),
                            ),
                        ),
                        generationConfig = GeminiGenerationConfig(
                            responseMimeType = "application/json",
                            responseSchema = GeminiSchema(
                                type = "OBJECT",
                                properties = mapOf(
                                    "transcription" to GeminiSchema(type = "STRING"),
                                    "confidence" to GeminiSchema(type = "INTEGER"),
                                ),
                                required = listOf("transcription", "confidence"),
                            ),
                        ),
                    ),
                )
            }.body()

            val responseText = response.candidates
                .firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                .orEmpty()
            if (responseText.isBlank()) {
                throw AiGenerationException.NonRetryable("AI provider returned an empty transcription response")
            }

            val structuredResponse = json.decodeFromString<GeminiStructuredTranscriptionResponse>(responseText)
            return AudioTranscription(
                transcription = structuredResponse.transcription.trim(),
                confidence = structuredResponse.confidence.coerceIn(0, 100),
            )
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
        } catch (error: IllegalArgumentException) {
            throw AiGenerationException.NonRetryable(
                message = error.message ?: "Invalid AI response",
                cause = error,
            )
        } finally {
            uploadedFileName?.let { deleteUploadedFile(apiKey, it) }
            localFile.delete()
        }
    }

    private suspend fun startUpload(
        apiKey: String,
        localFile: File,
        audio: RecordedAudio,
    ): String {
        val response = client.post(urlString = "$UPLOAD_BASE_URL/files") {
            header("x-goog-api-key", apiKey)
            header("X-Goog-Upload-Protocol", "resumable")
            header("X-Goog-Upload-Command", "start")
            header("X-Goog-Upload-Header-Content-Length", localFile.length().toString())
            header("X-Goog-Upload-Header-Content-Type", audio.mimeType)
            contentType(ContentType.Application.Json)
            setBody(GeminiUploadRequest(file = GeminiUploadMetadata(displayName = audio.displayName)))
        }

        return response.headers["X-Goog-Upload-URL"]
            ?: response.headers["x-goog-upload-url"]
            ?: throw AiGenerationException.NonRetryable("AI provider did not return an upload URL")
    }

    private suspend fun uploadFile(
        uploadUrl: String,
        localFile: File,
        audio: RecordedAudio,
    ): GeminiFileReference {
        val responseText = client.post(urlString = uploadUrl) {
            header("Content-Length", localFile.length().toString())
            header("X-Goog-Upload-Offset", "0")
            header("X-Goog-Upload-Command", "upload, finalize")
            contentType(ContentType.parse(audio.mimeType))
            setBody(localFile.readBytes())
        }.bodyAsText()

        val payload = json.parseToJsonElement(responseText).jsonObject
        val filePayload = payload["file"]?.jsonObject
            ?: throw AiGenerationException.NonRetryable("AI provider upload response did not include a file")
        return parseFileReference(filePayload)
    }

    private suspend fun waitUntilFileIsReady(
        apiKey: String,
        uploadedFile: GeminiFileReference,
    ): GeminiFileReference {
        repeat(MAX_FILE_STATUS_POLLS) {
            val fileResponse = client.get(urlString = "https://generativelanguage.googleapis.com/v1beta/${uploadedFile.name}") {
                header("x-goog-api-key", apiKey)
            }.bodyAsText()
            val payload = json.parseToJsonElement(fileResponse).jsonObject
            val fileReference = parseFileReference(payload)
            when (fileReference.state) {
                null,
                "ACTIVE",
                -> return fileReference

                "PROCESSING" -> delay(FILE_STATUS_POLL_INTERVAL_MS)
                "FAILED" -> throw AiGenerationException.NonRetryable("AI provider could not process the recorded audio")
                else -> throw AiGenerationException.NonRetryable("AI provider file is in an unsupported state: ${fileReference.state}")
            }
        }

        throw AiGenerationException.Retryable("Timed out while preparing the recorded audio")
    }

    private suspend fun deleteUploadedFile(apiKey: String, fileName: String) {
        runCatching {
            client.delete(urlString = "https://generativelanguage.googleapis.com/v1beta/$fileName") {
                header("x-goog-api-key", apiKey)
            }
        }
    }

    private fun parseFileReference(payload: JsonObject): GeminiFileReference = GeminiFileReference(
        name = payload.stringValue("name")
            ?: throw AiGenerationException.NonRetryable("AI provider file response did not include a name"),
        uri = payload.stringValue("uri")
            ?: throw AiGenerationException.NonRetryable("AI provider file response did not include a URI"),
        mimeType = payload.stringValue("mime_type", "mimeType") ?: DEFAULT_AUDIO_MIME_TYPE,
        state = payload.stringValue("state"),
    )

    private fun JsonObject.stringValue(vararg names: String): String? =
        names.firstNotNullOfOrNull { key -> this[key]?.jsonPrimitive?.contentOrNull }

    private data class GeminiFileReference(
        val name: String,
        val uri: String,
        val mimeType: String,
        val state: String? = null,
    )

    companion object {
        private const val DEFAULT_AUDIO_MIME_TYPE = "audio/mp4"
        private const val FILE_STATUS_POLL_INTERVAL_MS = 1_000L
        private const val MAX_FILE_STATUS_POLLS = 15
        private const val UPLOAD_BASE_URL = "https://generativelanguage.googleapis.com/upload/v1beta"
        private const val SYSTEM_INSTRUCTION = "You transcribe short audio clips for a task capture application. Return the spoken content in concise English. If the speaker uses another language, translate it to natural English. Be conservative with confidence when the speech is unclear, incomplete, or ambiguous."
        private const val USER_PROMPT = "Transcribe the uploaded audio and return only the JSON object that matches the schema."
    }
}

@Serializable
private data class GeminiUploadRequest(
    val file: GeminiUploadMetadata,
)

@Serializable
private data class GeminiUploadMetadata(
    @SerialName("display_name")
    val displayName: String,
)

@Serializable
private data class GeminiGenerateAudioRequest(
    @SerialName("system_instruction")
    val systemInstruction: GeminiContent,
    val contents: List<GeminiContent>,
    @SerialName("generation_config")
    val generationConfig: GeminiGenerationConfig,
)

@Serializable
private data class GeminiGenerationConfig(
    @SerialName("response_mime_type")
    val responseMimeType: String,
    @SerialName("response_schema")
    val responseSchema: GeminiSchema,
)

@Serializable
private data class GeminiSchema(
    val type: String,
    val properties: Map<String, GeminiSchema> = emptyMap(),
    val required: List<String> = emptyList(),
)

@Serializable
private data class GeminiStructuredTranscriptionResponse(
    val transcription: String = "",
    val confidence: Int = 0,
)
