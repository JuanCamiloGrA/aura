package com.humans.aura.features.voice.presentation

data class VoiceUiState(
    val isListening: Boolean = false,
    val isCancelled: Boolean = false,
    val transcript: String = "",
    val errorMessage: String? = null,
)
