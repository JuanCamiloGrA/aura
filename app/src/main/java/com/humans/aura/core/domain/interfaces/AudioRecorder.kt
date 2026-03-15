package com.humans.aura.core.domain.interfaces

import com.humans.aura.core.domain.models.RecordedAudio
import com.humans.aura.core.domain.models.VoiceCaptureState
import kotlinx.coroutines.flow.Flow

interface AudioRecorder {
    val captureState: Flow<VoiceCaptureState>

    fun startRecording()

    fun stopRecording(): RecordedAudio?

    fun cancelRecording()
}
