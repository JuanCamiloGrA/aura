package com.humans.aura.features.voice.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humans.aura.core.domain.interfaces.SpeechRecognizer
import com.humans.aura.core.domain.models.VoiceCaptureState
import com.humans.aura.features.voice.domain.NormalizeTranscriptToEnglishUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VoiceViewModel(
    private val speechRecognizer: SpeechRecognizer,
    private val normalizeTranscriptToEnglishUseCase: NormalizeTranscriptToEnglishUseCase,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = mutableUiState.asStateFlow()

    private var pendingFinalTranscript: String? = null
    private var pendingLanguageCode: String = "en"
    private var finishCaptureJob: Job? = null

    init {
        viewModelScope.launch {
            speechRecognizer.captureState.collect(::handleCaptureState)
        }
    }

    fun startCapture() {
        finishCaptureJob?.cancel()
        pendingFinalTranscript = null
        pendingLanguageCode = "en"
        mutableUiState.value = VoiceUiState(stage = VoiceUiStage.Listening)
        speechRecognizer.startListening()
    }

    fun cancelCapture() {
        finishCaptureJob?.cancel()
        speechRecognizer.cancelListening()
        pendingFinalTranscript = null
        mutableUiState.value = VoiceUiState(stage = VoiceUiStage.Cancelled)
    }

    fun finishCapture(onTranscriptReady: (String) -> Unit = {}) {
        speechRecognizer.stopListening()
        finishCaptureJob?.cancel()
        finishCaptureJob = viewModelScope.launch {
            val finalTranscript = pendingFinalTranscript?.takeIf { it.isNotBlank() }
            if (finalTranscript == null) {
                mutableUiState.value = mutableUiState.value.copy(
                    stage = VoiceUiStage.Error,
                    errorMessage = "No final transcript captured",
                )
                return@launch
            }

            mutableUiState.value = mutableUiState.value.copy(
                stage = VoiceUiStage.Transcribing,
                transcript = finalTranscript,
                partialTranscript = "",
                errorMessage = null,
            )

            val normalized = normalizeTranscriptToEnglishUseCase(finalTranscript)
            mutableUiState.value = mutableUiState.value.copy(
                stage = VoiceUiStage.Sending,
                transcript = normalized,
                partialTranscript = "",
                errorMessage = null,
            )
            onTranscriptReady(normalized)
            mutableUiState.value = mutableUiState.value.copy(stage = VoiceUiStage.Idle)
            pendingFinalTranscript = null
        }
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            mutableUiState.value = VoiceUiState(stage = VoiceUiStage.Idle)
        } else {
            mutableUiState.value = VoiceUiState(
                stage = VoiceUiStage.PermissionDenied,
                errorMessage = "Microphone permission denied",
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

            VoiceCaptureState.Listening -> mutableUiState.value.copy(
                stage = VoiceUiStage.Listening,
                errorMessage = null,
            )

            is VoiceCaptureState.TranscriptReady -> {
                pendingLanguageCode = state.detectedLanguageCode
                if (state.isPartial) {
                    mutableUiState.value.copy(
                        stage = VoiceUiStage.PartialReady,
                        partialTranscript = state.transcript,
                        detectedLanguageCode = state.detectedLanguageCode,
                        errorMessage = null,
                    )
                } else {
                    pendingFinalTranscript = state.transcript
                    mutableUiState.value.copy(
                        stage = VoiceUiStage.Listening,
                        transcript = state.transcript,
                        partialTranscript = "",
                        detectedLanguageCode = state.detectedLanguageCode,
                        errorMessage = null,
                    )
                }
            }

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
}
