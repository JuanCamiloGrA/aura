package com.humans.aura.core.domain.models

sealed interface VoiceCaptureState {
    data object Idle : VoiceCaptureState

    data object Recording : VoiceCaptureState

    data class Error(
        val message: String,
    ) : VoiceCaptureState
}
