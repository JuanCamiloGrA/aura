package com.humans.aura.features.voice.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humans.aura.core.domain.interfaces.SpeechRecognizer
import com.humans.aura.core.domain.models.VoiceCaptureState
import com.humans.aura.features.voice.domain.NormalizeTranscriptToEnglishUseCase
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VoiceViewModel(
    private val speechRecognizer: SpeechRecognizer,
    private val normalizeTranscriptToEnglishUseCase: NormalizeTranscriptToEnglishUseCase,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            speechRecognizer.captureState.collect { state ->
                mutableUiState.value = when (state) {
                    VoiceCaptureState.Idle -> mutableUiState.value.copy(isListening = false)
                    VoiceCaptureState.Listening -> VoiceUiState(isListening = true)
                    is VoiceCaptureState.Error -> VoiceUiState(errorMessage = state.message)
                    is VoiceCaptureState.TranscriptReady -> mutableUiState.value.copy(
                        isListening = state.isPartial,
                        transcript = state.transcript,
                        errorMessage = null,
                    )
                }
            }
        }
    }

    fun startCapture() {
        mutableUiState.value = VoiceUiState(isListening = true)
        speechRecognizer.startListening()
    }

    fun cancelCapture() {
        speechRecognizer.cancelListening()
        mutableUiState.value = VoiceUiState(isCancelled = true)
    }

    fun finishCapture(onTranscriptReady: (String) -> Unit = {}) {
        speechRecognizer.stopListening()
        val transcript = mutableUiState.value.transcript
        if (transcript.isBlank()) {
            return
        }
        viewModelScope.launch {
            val normalized = normalizeTranscriptToEnglishUseCase(transcript)
            mutableUiState.value = VoiceUiState(transcript = normalized)
            onTranscriptReady(normalized)
        }
    }
}
