package com.humans.aura.features.voice.presentation

data class VoiceUiState(
    val stage: VoiceUiStage = VoiceUiStage.Idle,
    val transcript: String = "",
    val confidence: Int? = null,
    val errorMessage: String? = null,
)

enum class VoiceUiStage {
    Idle,
    Recording,
    Transcribing,
    Sending,
    Speaking,
    LowConfidence,
    Cancelled,
    PermissionDenied,
    Error,
}

val VoiceUiState.isListening: Boolean
    get() = stage == VoiceUiStage.Recording

val VoiceUiState.isCancelled: Boolean
    get() = stage == VoiceUiStage.Cancelled
