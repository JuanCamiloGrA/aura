package com.humans.aura.core.domain.interfaces

import com.humans.aura.core.domain.models.VoiceCaptureState
import kotlinx.coroutines.flow.Flow

interface SpeechRecognizer {
    val captureState: Flow<VoiceCaptureState>

    fun startListening()

    fun stopListening()

    fun cancelListening()
}
