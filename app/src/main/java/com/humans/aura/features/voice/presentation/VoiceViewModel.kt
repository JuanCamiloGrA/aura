package com.humans.aura.features.voice.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humans.aura.core.domain.interfaces.AudioRecorder
import com.humans.aura.core.domain.models.AiGenerationException
import com.humans.aura.core.domain.models.VoiceCaptureState
import com.humans.aura.features.voice.domain.ShouldAcceptTranscriptionUseCase
import com.humans.aura.features.voice.domain.TranscribeAudioUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VoiceViewModel(
    private val audioRecorder: AudioRecorder,
    private val transcribeAudioUseCase: TranscribeAudioUseCase,
    private val shouldAcceptTranscriptionUseCase: ShouldAcceptTranscriptionUseCase,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = mutableUiState.asStateFlow()

    private var finishCaptureJob: Job? = null

    init {
        viewModelScope.launch {
            audioRecorder.captureState.collect(::handleCaptureState)
        }
    }

    fun startCapture() {
        finishCaptureJob?.cancel()
        mutableUiState.value = VoiceUiState(stage = VoiceUiStage.Recording)
        audioRecorder.startRecording()
    }

    fun cancelCapture() {
        finishCaptureJob?.cancel()
        audioRecorder.cancelRecording()
        mutableUiState.value = VoiceUiState(stage = VoiceUiStage.Cancelled)
    }

    fun finishCapture(onTranscriptReady: (String) -> Unit = {}) {
        val capturedAudio = audioRecorder.stopRecording()
        finishCaptureJob?.cancel()
        finishCaptureJob = viewModelScope.launch {
            if (capturedAudio == null) {
                mutableUiState.value = mutableUiState.value.copy(
                    stage = VoiceUiStage.Error,
                    errorMessage = "No recording captured",
                )
                return@launch
            }

            mutableUiState.value = mutableUiState.value.copy(
                stage = VoiceUiStage.Transcribing,
                confidence = null,
                errorMessage = null,
            )

            runCatching {
                transcribeAudioUseCase(capturedAudio)
            }.onSuccess { transcription ->
                if (!shouldAcceptTranscriptionUseCase(transcription)) {
                    mutableUiState.value = mutableUiState.value.copy(
                        stage = VoiceUiStage.LowConfidence,
                        transcript = transcription.transcription,
                        confidence = transcription.confidence,
                        errorMessage = "I couldn't catch that clearly. Hold to try again.",
                    )
                    return@onSuccess
                }

                mutableUiState.value = mutableUiState.value.copy(
                    stage = VoiceUiStage.Sending,
                    transcript = transcription.transcription,
                    confidence = transcription.confidence,
                    errorMessage = null,
                )
                onTranscriptReady(transcription.transcription)
                mutableUiState.value = mutableUiState.value.copy(stage = VoiceUiStage.Idle)
            }.onFailure { error ->
                mutableUiState.value = mutableUiState.value.copy(
                    stage = if (error.message?.contains("permission", ignoreCase = true) == true) {
                        VoiceUiStage.PermissionDenied
                    } else {
                        VoiceUiStage.Error
                    },
                    errorMessage = error.userFacingMessage(),
                )
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            mutableUiState.value = VoiceUiState(stage = VoiceUiStage.Idle)
        } else {
            mutableUiState.value = VoiceUiState(
                stage = VoiceUiStage.PermissionDenied,
                errorMessage = "Microphone permission required",
            )
        }
    }

    fun setSpeaking(isSpeaking: Boolean) {
        mutableUiState.value = mutableUiState.value.copy(
            stage = if (isSpeaking) VoiceUiStage.Speaking else VoiceUiStage.Idle,
        )
    }

    private fun handleCaptureState(state: VoiceCaptureState) {
        mutableUiState.value = when (state) {
            VoiceCaptureState.Idle -> mutableUiState.value.copy(
                stage = if (mutableUiState.value.stage == VoiceUiStage.Cancelled) VoiceUiStage.Cancelled else VoiceUiStage.Idle,
            )

            VoiceCaptureState.Recording -> mutableUiState.value.copy(
                stage = VoiceUiStage.Recording,
                errorMessage = null,
            )

            is VoiceCaptureState.Error -> {
                val stage = if (state.message.contains("permission", ignoreCase = true)) {
                    VoiceUiStage.PermissionDenied
                } else {
                    VoiceUiStage.Error
                }
                mutableUiState.value.copy(
                    stage = stage,
                    errorMessage = state.message,
                )
            }
        }
    }

    private fun Throwable.userFacingMessage(): String = when (this) {
        is AiGenerationException.Retryable -> "Connection issue while transcribing. Hold to try again."
        else -> message ?: "We couldn't transcribe that recording. Hold to try again."
    }
}
