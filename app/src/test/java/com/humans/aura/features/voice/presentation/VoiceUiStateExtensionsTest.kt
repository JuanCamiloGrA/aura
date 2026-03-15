package com.humans.aura.features.voice.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceUiStateExtensionsTest {

    @Test
    fun is_listening_is_true_for_recording_state() {
        assertTrue(VoiceUiState(stage = VoiceUiStage.Recording).isListening)
        assertFalse(VoiceUiState(stage = VoiceUiStage.Idle).isListening)
    }

    @Test
    fun is_cancelled_is_true_only_for_cancelled_state() {
        assertTrue(VoiceUiState(stage = VoiceUiStage.Cancelled).isCancelled)
        assertFalse(VoiceUiState(stage = VoiceUiStage.Error).isCancelled)
    }
}
