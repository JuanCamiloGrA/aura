package com.humans.aura.features.voice.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoicePermissionStateTest {

    @Test
    fun defaults_to_denied_without_rationale() {
        val state = VoicePermissionState()

        assertFalse(state.hasPermission)
        assertFalse(state.shouldShowRationale)
    }

    @Test
    fun stores_explicit_permission_values() {
        val state = VoicePermissionState(
            hasPermission = true,
            shouldShowRationale = true,
        )

        assertTrue(state.hasPermission)
        assertTrue(state.shouldShowRationale)
    }
}
