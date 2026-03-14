package com.humans.aura.features.voice.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import com.humans.aura.core.presentation.theme.AuraTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class VoiceCaptureButtonTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun hold_starts_capture() {
        var starts = 0

        composeRule.setContent {
            AuraTheme {
                VoiceCaptureButton(
                    uiState = VoiceUiState(),
                    onStartCapture = { starts += 1 },
                    onCancelCapture = {},
                    onReleaseCapture = {},
                )
            }
        }

        composeRule.onNodeWithTag("voice_capture_button").performTouchInput {
            down(center)
            advanceEventTime(50)
            up()
        }

        assertEquals(1, starts)
    }

    @Test
    fun release_sends_capture() {
        var releases = 0

        composeRule.setContent {
            AuraTheme {
                VoiceCaptureButton(
                    uiState = VoiceUiState(isListening = true),
                    onStartCapture = {},
                    onCancelCapture = {},
                    onReleaseCapture = { releases += 1 },
                )
            }
        }

        composeRule.onNodeWithTag("voice_capture_button").performTouchInput {
            down(center)
            advanceEventTime(50)
            up()
        }

        assertEquals(1, releases)
    }

    @Test
    fun swipe_left_cancels_capture() {
        var cancels = 0

        composeRule.setContent {
            AuraTheme {
                VoiceCaptureButton(
                    uiState = VoiceUiState(isListening = true),
                    onStartCapture = {},
                    onCancelCapture = { cancels += 1 },
                    onReleaseCapture = {},
                )
            }
        }

        composeRule.onNodeWithTag("voice_capture_button").performTouchInput {
            down(center)
            moveBy(Offset(-150f, 0f))
            advanceEventTime(50)
            up()
        }

        assertEquals(1, cancels)
    }

    @Test
    fun cancelled_state_is_rendered() {
        composeRule.setContent {
            AuraTheme {
                VoiceCaptureButton(
                    uiState = VoiceUiState(isCancelled = true),
                    onStartCapture = {},
                    onCancelCapture = {},
                    onReleaseCapture = {},
                )
            }
        }

        composeRule.onNodeWithTag("voice_capture_button").assertIsDisplayed()
        composeRule.onNodeWithText("Cancelled").assertIsDisplayed()
    }

    @Test
    fun transcript_state_is_rendered() {
        composeRule.setContent {
            AuraTheme {
                VoiceCaptureButton(
                    uiState = VoiceUiState(transcript = "hello there"),
                    onStartCapture = {},
                    onCancelCapture = {},
                    onReleaseCapture = {},
                )
            }
        }

        composeRule.onNodeWithText("Ready: hello there").assertIsDisplayed()
    }

    @Test
    fun error_state_is_rendered() {
        composeRule.setContent {
            AuraTheme {
                VoiceCaptureButton(
                    uiState = VoiceUiState(errorMessage = "Microphone permission denied"),
                    onStartCapture = {},
                    onCancelCapture = {},
                    onReleaseCapture = {},
                )
            }
        }

        composeRule.onNodeWithText("Microphone permission denied").assertIsDisplayed()
    }
}
