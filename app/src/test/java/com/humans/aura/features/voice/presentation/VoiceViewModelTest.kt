package com.humans.aura.features.voice.presentation

import com.humans.aura.MainDispatcherRule
import com.humans.aura.core.domain.interfaces.AiTextGenerator
import com.humans.aura.core.domain.interfaces.SpeechRecognizer
import com.humans.aura.core.domain.models.AiRequest
import com.humans.aura.core.domain.models.AiResponse
import com.humans.aura.core.domain.models.VoiceCaptureState
import com.humans.aura.features.voice.domain.NormalizeTranscriptToEnglishUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun start_capture_sets_listening_and_calls_recognizer() = runTest {
        val recognizer = FakeSpeechRecognizer()
        val viewModel = VoiceViewModel(recognizer, NormalizeTranscriptToEnglishUseCase(FakeAiTextGenerator()))
        advanceUntilIdle()

        viewModel.startCapture()

        assertEquals(1, recognizer.startCalls)
        assertEquals(VoiceUiStage.Listening, viewModel.uiState.value.stage)
    }

    @Test
    fun cancel_capture_marks_cancelled_and_calls_recognizer() = runTest {
        val recognizer = FakeSpeechRecognizer()
        val viewModel = VoiceViewModel(recognizer, NormalizeTranscriptToEnglishUseCase(FakeAiTextGenerator()))
        advanceUntilIdle()

        viewModel.cancelCapture()

        assertEquals(1, recognizer.cancelCalls)
        assertEquals(VoiceUiStage.Cancelled, viewModel.uiState.value.stage)
    }

    @Test
    fun partial_transcript_keeps_preview_without_sending() = runTest {
        val recognizer = FakeSpeechRecognizer()
        val viewModel = VoiceViewModel(recognizer, NormalizeTranscriptToEnglishUseCase(FakeAiTextGenerator()))
        advanceUntilIdle()

        recognizer.emit(VoiceCaptureState.TranscriptReady("hola", "es", isPartial = true))
        advanceUntilIdle()

        assertEquals(VoiceUiStage.PartialReady, viewModel.uiState.value.stage)
        assertEquals("hola", viewModel.uiState.value.partialTranscript)
    }

    @Test
    fun final_transcript_normalizes_and_emits_callback() = runTest {
        val recognizer = FakeSpeechRecognizer()
        val ai = FakeAiTextGenerator(response = AiResponse("hello", "gemini-test"))
        val viewModel = VoiceViewModel(recognizer, NormalizeTranscriptToEnglishUseCase(ai))
        var sentTranscript: String? = null
        advanceUntilIdle()

        recognizer.emit(VoiceCaptureState.TranscriptReady("hola", "es"))
        advanceUntilIdle()
        viewModel.finishCapture { sentTranscript = it }
        advanceUntilIdle()

        assertEquals(1, recognizer.stopCalls)
        assertEquals(VoiceUiStage.Idle, viewModel.uiState.value.stage)
        assertEquals("hello", sentTranscript)
    }

    @Test
    fun finish_capture_without_final_transcript_shows_error() = runTest {
        val recognizer = FakeSpeechRecognizer()
        val ai = FakeAiTextGenerator()
        val viewModel = VoiceViewModel(recognizer, NormalizeTranscriptToEnglishUseCase(ai))
        recognizer.emit(VoiceCaptureState.TranscriptReady("hola", "es", isPartial = true))
        advanceUntilIdle()

        viewModel.finishCapture()
        advanceUntilIdle()

        assertEquals(1, recognizer.stopCalls)
        assertEquals(VoiceUiStage.Error, viewModel.uiState.value.stage)
        assertEquals(0, ai.requests.size)
    }

    @Test
    fun permission_denial_maps_to_permission_state() = runTest {
        val recognizer = FakeSpeechRecognizer()
        val viewModel = VoiceViewModel(recognizer, NormalizeTranscriptToEnglishUseCase(FakeAiTextGenerator()))
        advanceUntilIdle()

        recognizer.emit(VoiceCaptureState.Error("Microphone permission denied"))
        advanceUntilIdle()

        assertEquals(VoiceUiStage.PermissionDenied, viewModel.uiState.value.stage)
        assertEquals("Microphone permission denied", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun speaking_state_can_be_toggled() = runTest {
        val recognizer = FakeSpeechRecognizer()
        val viewModel = VoiceViewModel(recognizer, NormalizeTranscriptToEnglishUseCase(FakeAiTextGenerator()))
        advanceUntilIdle()

        viewModel.setSpeaking(true)
        assertEquals(VoiceUiStage.Speaking, viewModel.uiState.value.stage)

        viewModel.setSpeaking(false)
        assertEquals(VoiceUiStage.Idle, viewModel.uiState.value.stage)
    }

    @Test
    fun granted_permission_resets_to_idle() = runTest {
        val recognizer = FakeSpeechRecognizer()
        val viewModel = VoiceViewModel(recognizer, NormalizeTranscriptToEnglishUseCase(FakeAiTextGenerator()))
        advanceUntilIdle()

        viewModel.onPermissionResult(true)

        assertEquals(VoiceUiStage.Idle, viewModel.uiState.value.stage)
    }

    @Test
    fun denied_permission_sets_permission_denied_state() = runTest {
        val recognizer = FakeSpeechRecognizer()
        val viewModel = VoiceViewModel(recognizer, NormalizeTranscriptToEnglishUseCase(FakeAiTextGenerator()))
        advanceUntilIdle()

        viewModel.onPermissionResult(false)

        assertEquals(VoiceUiStage.PermissionDenied, viewModel.uiState.value.stage)
    }

    @Test
    fun idle_capture_state_resets_to_idle_when_not_cancelled() = runTest {
        val recognizer = FakeSpeechRecognizer()
        val viewModel = VoiceViewModel(recognizer, NormalizeTranscriptToEnglishUseCase(FakeAiTextGenerator()))
        advanceUntilIdle()

        recognizer.emit(VoiceCaptureState.Listening)
        recognizer.emit(VoiceCaptureState.Idle)
        advanceUntilIdle()

        assertEquals(VoiceUiStage.Idle, viewModel.uiState.value.stage)
    }

    @Test
    fun listening_state_clears_previous_error() = runTest {
        val recognizer = FakeSpeechRecognizer()
        val viewModel = VoiceViewModel(recognizer, NormalizeTranscriptToEnglishUseCase(FakeAiTextGenerator()))
        advanceUntilIdle()

        recognizer.emit(VoiceCaptureState.Error("temporary error"))
        recognizer.emit(VoiceCaptureState.Listening)
        advanceUntilIdle()

        assertEquals(VoiceUiStage.Listening, viewModel.uiState.value.stage)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun final_transcript_updates_detected_language() = runTest {
        val recognizer = FakeSpeechRecognizer()
        val viewModel = VoiceViewModel(recognizer, NormalizeTranscriptToEnglishUseCase(FakeAiTextGenerator()))
        advanceUntilIdle()

        recognizer.emit(VoiceCaptureState.TranscriptReady("hola", "es"))
        advanceUntilIdle()

        assertEquals("es", viewModel.uiState.value.detectedLanguageCode)
    }

    @Test
    fun non_permission_error_maps_to_error_stage() = runTest {
        val recognizer = FakeSpeechRecognizer()
        val viewModel = VoiceViewModel(recognizer, NormalizeTranscriptToEnglishUseCase(FakeAiTextGenerator()))
        advanceUntilIdle()

        recognizer.emit(VoiceCaptureState.Error("Speech recognition network unavailable"))
        advanceUntilIdle()

        assertEquals(VoiceUiStage.Error, viewModel.uiState.value.stage)
    }

    @Test
    fun start_capture_cancels_existing_finish_job() = runTest {
        val recognizer = FakeSpeechRecognizer()
        val ai = FakeAiTextGenerator(response = AiResponse("translated", "gemini-test"), delayResponse = true)
        val viewModel = VoiceViewModel(recognizer, NormalizeTranscriptToEnglishUseCase(ai))
        recognizer.emit(VoiceCaptureState.TranscriptReady("hola", "es"))
        advanceUntilIdle()

        viewModel.finishCapture()
        viewModel.startCapture()
        ai.release()
        advanceUntilIdle()

        assertEquals(VoiceUiStage.Listening, viewModel.uiState.value.stage)
    }

    @Test
    fun cancel_capture_cancels_existing_finish_job() = runTest {
        val recognizer = FakeSpeechRecognizer()
        val ai = FakeAiTextGenerator(response = AiResponse("translated", "gemini-test"), delayResponse = true)
        val viewModel = VoiceViewModel(recognizer, NormalizeTranscriptToEnglishUseCase(ai))
        recognizer.emit(VoiceCaptureState.TranscriptReady("hola", "es"))
        advanceUntilIdle()

        viewModel.finishCapture()
        viewModel.cancelCapture()
        ai.release()
        advanceUntilIdle()

        assertEquals(VoiceUiStage.Cancelled, viewModel.uiState.value.stage)
    }

    @Test
    fun finish_capture_default_callback_path_succeeds() = runTest {
        val recognizer = FakeSpeechRecognizer()
        val viewModel = VoiceViewModel(recognizer, NormalizeTranscriptToEnglishUseCase(FakeAiTextGenerator()))
        recognizer.emit(VoiceCaptureState.TranscriptReady("hola", "es"))
        advanceUntilIdle()

        viewModel.finishCapture()
        advanceUntilIdle()

        assertEquals(VoiceUiStage.Idle, viewModel.uiState.value.stage)
    }

    private class FakeSpeechRecognizer : SpeechRecognizer {
        private val state = MutableStateFlow<VoiceCaptureState>(VoiceCaptureState.Idle)
        override val captureState: Flow<VoiceCaptureState> = state
        var startCalls = 0
        var stopCalls = 0
        var cancelCalls = 0

        override fun startListening() {
            startCalls += 1
        }

        override fun stopListening() {
            stopCalls += 1
        }

        override fun cancelListening() {
            cancelCalls += 1
        }

        fun emit(value: VoiceCaptureState) {
            state.value = value
        }
    }

    private class FakeAiTextGenerator(
        private val response: AiResponse = AiResponse("translated", "gemini-test"),
        private val delayResponse: Boolean = false,
    ) : AiTextGenerator {
        val requests = mutableListOf<AiRequest>()
        private val gate = CompletableDeferred<Unit>().apply {
            if (!delayResponse) complete(Unit)
        }

        override suspend fun generate(request: AiRequest): AiResponse {
            requests += request
            gate.await()
            return response
        }

        fun release() {
            if (!gate.isCompleted) {
                gate.complete(Unit)
            }
        }
    }
}
