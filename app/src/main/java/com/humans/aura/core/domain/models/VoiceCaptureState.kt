package com.humans.aura.core.domain.models

sealed interface VoiceCaptureState {
    data object Idle : VoiceCaptureState

    data object Listening : VoiceCaptureState

    data class TranscriptReady(
        val transcript: String,
        val detectedLanguageCode: String,
        val isPartial: Boolean = false,
    ) : VoiceCaptureState

    data class Error(
        val message: String,
    ) : VoiceCaptureState
}
