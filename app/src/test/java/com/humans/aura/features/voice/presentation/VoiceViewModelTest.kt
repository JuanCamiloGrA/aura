package com.humans.aura.features.voice.presentation

import com.humans.aura.MainDispatcherRule
import com.humans.aura.core.domain.interfaces.AiTextGenerator
import com.humans.aura.core.domain.interfaces.SpeechRecognizer
import com.humans.aura.core.domain.models.AiRequest
import com.humans.aura.core.domain.models.AiResponse
import com.humans.aura.core.domain.models.VoiceCaptureState
import com.humans.aura.features.voice.domain.NormalizeTranscriptToEnglishUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

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
        assertEquals(true, viewModel.uiState.value.isListening)
    }

    @Test
    fun cancel_capture_marks_cancelled_and_calls_recognizer() = runTest {
        val recognizer = FakeSpeechRecognizer()
        val viewModel = VoiceViewModel(recognizer, NormalizeTranscriptToEnglishUseCase(FakeAiTextGenerator()))
        advanceUntilIdle()

        viewModel.cancelCapture()

        assertEquals(1, recognizer.cancelCalls)
        assertEquals(true, viewModel.uiState.value.isCancelled)
    }

    @Test
    fun partial_transcript_keeps_listening() = runTest {
        val recognizer = FakeSpeechRecognizer()
        val viewModel = VoiceViewModel(recognizer, NormalizeTranscriptToEnglishUseCase(FakeAiTextGenerator()))
        advanceUntilIdle()

        recognizer.emit(VoiceCaptureState.TranscriptReady("hola", "es", isPartial = true))
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.isListening)
        assertEquals("hola", viewModel.uiState.value.transcript)
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
        assertEquals("hello", viewModel.uiState.value.transcript)
        assertEquals("hello", sentTranscript)
    }

    @Test
    fun finish_capture_ignores_blank_transcript() = runTest {
        val recognizer = FakeSpeechRecognizer()
        val ai = FakeAiTextGenerator()
        val viewModel = VoiceViewModel(recognizer, NormalizeTranscriptToEnglishUseCase(ai))
        advanceUntilIdle()

        viewModel.finishCapture()
        advanceUntilIdle()

        assertEquals(1, recognizer.stopCalls)
        assertEquals(0, ai.requests.size)
    }

    @Test
    fun error_state_is_reflected_in_ui() = runTest {
        val recognizer = FakeSpeechRecognizer()
        val viewModel = VoiceViewModel(recognizer, NormalizeTranscriptToEnglishUseCase(FakeAiTextGenerator()))
        advanceUntilIdle()

        recognizer.emit(VoiceCaptureState.Error("Microphone permission denied"))
        advanceUntilIdle()

        assertEquals("Microphone permission denied", viewModel.uiState.value.errorMessage)
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
    ) : AiTextGenerator {
        val requests = mutableListOf<AiRequest>()

        override suspend fun generate(request: AiRequest): AiResponse {
            requests += request
            return response
        }
    }
}
