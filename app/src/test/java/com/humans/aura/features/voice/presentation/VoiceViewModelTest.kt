package com.humans.aura.features.voice.presentation

import com.humans.aura.MainDispatcherRule
import com.humans.aura.core.domain.interfaces.AudioRecorder
import com.humans.aura.core.domain.interfaces.AudioTranscriber
import com.humans.aura.core.domain.models.AiGenerationException
import com.humans.aura.core.domain.models.AudioTranscription
import com.humans.aura.core.domain.models.RecordedAudio
import com.humans.aura.core.domain.models.VoiceCaptureState
import com.humans.aura.features.voice.domain.ShouldAcceptTranscriptionUseCase
import com.humans.aura.features.voice.domain.TranscribeAudioUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun start_capture_sets_recording_and_calls_recorder() = runTest {
        val recorder = FakeAudioRecorder()
        val viewModel = createViewModel(recorder = recorder)
        advanceUntilIdle()

        viewModel.startCapture()

        assertEquals(1, recorder.startCalls)
        assertEquals(VoiceUiStage.Recording, viewModel.uiState.value.stage)
    }

    @Test
    fun cancel_capture_marks_cancelled_and_calls_recorder() = runTest {
        val recorder = FakeAudioRecorder()
        val viewModel = createViewModel(recorder = recorder)
        advanceUntilIdle()

        viewModel.cancelCapture()

        assertEquals(1, recorder.cancelCalls)
        assertEquals(VoiceUiStage.Cancelled, viewModel.uiState.value.stage)
    }

    @Test
    fun finish_capture_transcribes_and_emits_callback() = runTest {
        val audio = RecordedAudio("clip.m4a", "audio/mp4", "clip.m4a")
        val recorder = FakeAudioRecorder(stopResult = audio)
        val transcriber = FakeAudioTranscriber(response = AudioTranscription("Go out for lunch", 95))
        val viewModel = createViewModel(recorder = recorder, transcriber = transcriber)
        var sentTranscript: String? = null
        advanceUntilIdle()

        viewModel.finishCapture { sentTranscript = it }
        advanceUntilIdle()

        assertEquals(1, recorder.stopCalls)
        assertEquals(audio, transcriber.receivedAudio)
        assertEquals("Go out for lunch", sentTranscript)
        assertEquals(VoiceUiStage.Idle, viewModel.uiState.value.stage)
    }

    @Test
    fun finish_capture_without_recording_shows_error() = runTest {
        val recorder = FakeAudioRecorder(stopResult = null)
        val transcriber = FakeAudioTranscriber()
        val viewModel = createViewModel(recorder = recorder, transcriber = transcriber)
        advanceUntilIdle()

        viewModel.finishCapture()
        advanceUntilIdle()

        assertEquals(VoiceUiStage.Error, viewModel.uiState.value.stage)
        assertEquals("No recording captured", viewModel.uiState.value.errorMessage)
        assertNull(transcriber.receivedAudio)
    }

    @Test
    fun low_confidence_transcription_does_not_send_callback() = runTest {
        val audio = RecordedAudio("clip.m4a", "audio/mp4", "clip.m4a")
        val recorder = FakeAudioRecorder(stopResult = audio)
        val transcriber = FakeAudioTranscriber(response = AudioTranscription("Maybe lunch", 42))
        val viewModel = createViewModel(recorder = recorder, transcriber = transcriber)
        var sentTranscript: String? = null
        advanceUntilIdle()

        viewModel.finishCapture { sentTranscript = it }
        advanceUntilIdle()

        assertEquals(VoiceUiStage.LowConfidence, viewModel.uiState.value.stage)
        assertEquals(42, viewModel.uiState.value.confidence)
        assertEquals(null, sentTranscript)
    }

    @Test
    fun retryable_transcription_error_maps_to_friendly_error() = runTest {
        val audio = RecordedAudio("clip.m4a", "audio/mp4", "clip.m4a")
        val recorder = FakeAudioRecorder(stopResult = audio)
        val transcriber = FakeAudioTranscriber(error = AiGenerationException.Retryable("offline"))
        val viewModel = createViewModel(recorder = recorder, transcriber = transcriber)
        advanceUntilIdle()

        viewModel.finishCapture()
        advanceUntilIdle()

        assertEquals(VoiceUiStage.Error, viewModel.uiState.value.stage)
        assertEquals("Connection issue while transcribing. Hold to try again.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun recording_state_clears_previous_error() = runTest {
        val recorder = FakeAudioRecorder()
        val viewModel = createViewModel(recorder = recorder)
        advanceUntilIdle()

        recorder.emit(VoiceCaptureState.Error("temporary error"))
        recorder.emit(VoiceCaptureState.Recording)
        advanceUntilIdle()

        assertEquals(VoiceUiStage.Recording, viewModel.uiState.value.stage)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun permission_denial_maps_to_permission_state() = runTest {
        val recorder = FakeAudioRecorder()
        val viewModel = createViewModel(recorder = recorder)
        advanceUntilIdle()

        recorder.emit(VoiceCaptureState.Error("Microphone permission denied"))
        advanceUntilIdle()

        assertEquals(VoiceUiStage.PermissionDenied, viewModel.uiState.value.stage)
        assertEquals("Microphone permission denied", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun speaking_state_can_be_toggled() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setSpeaking(true)
        assertEquals(VoiceUiStage.Speaking, viewModel.uiState.value.stage)

        viewModel.setSpeaking(false)
        assertEquals(VoiceUiStage.Idle, viewModel.uiState.value.stage)
    }

    @Test
    fun start_capture_cancels_existing_finish_job() = runTest {
        val audio = RecordedAudio("clip.m4a", "audio/mp4", "clip.m4a")
        val recorder = FakeAudioRecorder(stopResult = audio)
        val transcriber = FakeAudioTranscriber(
            response = AudioTranscription("Go out for lunch", 95),
            delayResponse = true,
        )
        val viewModel = createViewModel(recorder = recorder, transcriber = transcriber)
        advanceUntilIdle()

        viewModel.finishCapture()
        viewModel.startCapture()
        transcriber.release()
        advanceUntilIdle()

        assertEquals(VoiceUiStage.Recording, viewModel.uiState.value.stage)
    }

    @Test
    fun granted_permission_resets_to_idle() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onPermissionResult(true)

        assertEquals(VoiceUiStage.Idle, viewModel.uiState.value.stage)
    }

    @Test
    fun denied_permission_sets_permission_denied_state() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onPermissionResult(false)

        assertEquals(VoiceUiStage.PermissionDenied, viewModel.uiState.value.stage)
    }

    private fun createViewModel(
        recorder: FakeAudioRecorder = FakeAudioRecorder(),
        transcriber: FakeAudioTranscriber = FakeAudioTranscriber(),
    ): VoiceViewModel = VoiceViewModel(
        audioRecorder = recorder,
        transcribeAudioUseCase = TranscribeAudioUseCase(transcriber),
        shouldAcceptTranscriptionUseCase = ShouldAcceptTranscriptionUseCase(),
    )

    private class FakeAudioRecorder(
        private val stopResult: RecordedAudio? = null,
    ) : AudioRecorder {
        private val state = MutableStateFlow<VoiceCaptureState>(VoiceCaptureState.Idle)
        override val captureState: Flow<VoiceCaptureState> = state
        var startCalls = 0
        var stopCalls = 0
        var cancelCalls = 0

        override fun startRecording() {
            startCalls += 1
        }

        override fun stopRecording(): RecordedAudio? {
            stopCalls += 1
            return stopResult
        }

        override fun cancelRecording() {
            cancelCalls += 1
        }

        fun emit(value: VoiceCaptureState) {
            state.value = value
        }
    }

    private class FakeAudioTranscriber(
        private val response: AudioTranscription = AudioTranscription("translated", 100),
        private val error: Throwable? = null,
        private val delayResponse: Boolean = false,
    ) : AudioTranscriber {
        var receivedAudio: RecordedAudio? = null
        private val gate = CompletableDeferred<Unit>().apply {
            if (!delayResponse) complete(Unit)
        }

        override suspend fun transcribe(audio: RecordedAudio): AudioTranscription {
            receivedAudio = audio
            gate.await()
            error?.let { throw it }
            return response
        }

        fun release() {
            if (!gate.isCompleted) {
                gate.complete(Unit)
            }
        }
    }
}
