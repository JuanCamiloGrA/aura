package com.humans.aura.features.voice.presentation

data class VoiceUiState(
    val stage: VoiceUiStage = VoiceUiStage.Idle,
    val transcript: String = "",
    val partialTranscript: String = "",
    val detectedLanguageCode: String = "en",
    val errorMessage: String? = null,
)

enum class VoiceUiStage {
    Idle,
    Listening,
    PartialReady,
    Transcribing,
    Sending,
    Speaking,
    Cancelled,
    PermissionDenied,
    Error,
}

val VoiceUiState.isListening: Boolean
    get() = stage == VoiceUiStage.Listening || stage == VoiceUiStage.PartialReady

val VoiceUiState.isCancelled: Boolean
    get() = stage == VoiceUiStage.Cancelled
