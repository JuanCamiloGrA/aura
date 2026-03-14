package com.humans.aura.core.services.speech

import app.cash.turbine.test
import com.humans.aura.core.domain.models.VoiceCaptureState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AndroidSpeechRecognizerTest {

    @Test
    fun start_listening_emits_permission_error_when_microphone_not_granted() = runTest {
        val client = FakeRecognitionClient()
        val recognizer = AndroidSpeechRecognizer(
            hasRecordAudioPermission = { false },
            recognitionClient = client,
        )

        recognizer.captureState.test {
            assertEquals(VoiceCaptureState.Idle, awaitItem())

            recognizer.startListening()

            assertEquals(VoiceCaptureState.Error("Microphone permission denied"), awaitItem())
            assertEquals(0, client.startCalls)
        }
    }

    @Test
    fun start_listening_starts_client_when_permission_granted() = runTest {
        val client = FakeRecognitionClient()
        val recognizer = AndroidSpeechRecognizer(
            hasRecordAudioPermission = { true },
            recognitionClient = client,
        )

        recognizer.captureState.test {
            assertEquals(VoiceCaptureState.Idle, awaitItem())

            recognizer.startListening()

            assertEquals(VoiceCaptureState.Listening, awaitItem())
            assertEquals(1, client.startCalls)
        }
    }

    @Test
    fun ready_for_speech_emits_listening_state() = runTest {
        val client = FakeRecognitionClient()
        val recognizer = AndroidSpeechRecognizer(
            hasRecordAudioPermission = { true },
            recognitionClient = client,
        )

        recognizer.captureState.test {
            assertEquals(VoiceCaptureState.Idle, awaitItem())

            client.emitReadyForSpeech()

            assertEquals(VoiceCaptureState.Listening, awaitItem())
        }
    }

    @Test
    fun partial_results_emit_partial_transcript_only_for_non_blank_values() = runTest {
        val client = FakeRecognitionClient()
        val recognizer = AndroidSpeechRecognizer(
            hasRecordAudioPermission = { true },
            recognitionClient = client,
        )

        recognizer.captureState.test {
            assertEquals(VoiceCaptureState.Idle, awaitItem())

            client.emitPartialResults(listOf("   "))
            client.emitPartialResults(listOf("hola"))

            assertEquals(
                VoiceCaptureState.TranscriptReady(
                    transcript = "hola",
                    detectedLanguageCode = "auto",
                    isPartial = true,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun results_emit_final_transcript_even_when_empty() = runTest {
        val client = FakeRecognitionClient()
        val recognizer = AndroidSpeechRecognizer(
            hasRecordAudioPermission = { true },
            recognitionClient = client,
        )

        recognizer.captureState.test {
            assertEquals(VoiceCaptureState.Idle, awaitItem())

            client.emitResults(emptyList())

            assertEquals(
                VoiceCaptureState.TranscriptReady(
                    transcript = "",
                    detectedLanguageCode = "auto",
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun errors_use_resolver_message() = runTest {
        val client = FakeRecognitionClient()
        val recognizer = AndroidSpeechRecognizer(
            hasRecordAudioPermission = { true },
            recognitionClient = client,
            errorMessageResolver = { "resolved-$it" },
        )

        recognizer.captureState.test {
            assertEquals(VoiceCaptureState.Idle, awaitItem())

            client.emitError(7)

            assertEquals(VoiceCaptureState.Error("resolved-7"), awaitItem())
        }
    }

    @Test
    fun stop_and_cancel_delegate_to_client_and_cancel_resets_idle() = runTest {
        val client = FakeRecognitionClient()
        val recognizer = AndroidSpeechRecognizer(
            hasRecordAudioPermission = { true },
            recognitionClient = client,
        )

        recognizer.captureState.test {
            assertEquals(VoiceCaptureState.Idle, awaitItem())

            recognizer.stopListening()
            recognizer.cancelListening()

            assertEquals(1, client.stopCalls)
            assertEquals(1, client.cancelCalls)
            expectNoEvents()
        }
    }

    private class FakeRecognitionClient : RecognitionClient {
        private var listener: RecognitionClient.Listener? = null
        var startCalls = 0
        var stopCalls = 0
        var cancelCalls = 0

        override fun setListener(listener: RecognitionClient.Listener) {
            this.listener = listener
        }

        override fun startListening() {
            startCalls += 1
        }

        override fun stopListening() {
            stopCalls += 1
        }

        override fun cancel() {
            cancelCalls += 1
        }

        fun emitReadyForSpeech() {
            listener?.onReadyForSpeech()
        }

        fun emitResults(transcripts: List<String>) {
            listener?.onResults(transcripts)
        }

        fun emitPartialResults(transcripts: List<String>) {
            listener?.onPartialResults(transcripts)
        }

        fun emitError(error: Int) {
            listener?.onError(error)
        }
    }
}
