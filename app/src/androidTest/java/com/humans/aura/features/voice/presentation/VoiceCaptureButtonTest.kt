package com.humans.aura.features.voice.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
                    uiState = VoiceUiState(stage = VoiceUiStage.Listening),
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
                    uiState = VoiceUiState(stage = VoiceUiStage.Listening),
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
                    uiState = VoiceUiState(stage = VoiceUiStage.Cancelled),
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
    fun partial_transcript_state_is_rendered() {
        composeRule.setContent {
            AuraTheme {
                VoiceCaptureButton(
                    uiState = VoiceUiState(stage = VoiceUiStage.PartialReady, partialTranscript = "hello there"),
                    onStartCapture = {},
                    onCancelCapture = {},
                    onReleaseCapture = {},
                )
            }
        }

        composeRule.onNodeWithText("Listening... hello there").assertIsDisplayed()
    }

    @Test
    fun permission_state_requests_permission_hint() {
        composeRule.setContent {
            AuraTheme {
                VoiceCaptureButton(
                    uiState = VoiceUiState(stage = VoiceUiStage.PermissionDenied, errorMessage = "Microphone permission denied"),
                    onStartCapture = {},
                    onCancelCapture = {},
                    onReleaseCapture = {},
                )
            }
        }

        composeRule.onNodeWithText("Enable microphone access").assertIsDisplayed()
        composeRule.onNodeWithTag("voice_permission_hint").assertIsDisplayed()
    }

    @Test
    fun permission_denied_tap_requests_permission_instead_of_starting_capture() {
        var permissionRequests = 0
        var starts = 0

        composeRule.setContent {
            AuraTheme {
                VoiceCaptureButton(
                    uiState = VoiceUiState(stage = VoiceUiStage.PermissionDenied),
                    onRequestPermission = { permissionRequests += 1 },
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

        assertEquals(1, permissionRequests)
        assertEquals(0, starts)
    }

    @Test
    fun idle_transcript_and_error_states_are_rendered() {
        composeRule.setContent {
            AuraTheme {
                androidx.compose.foundation.layout.Column {
                    VoiceCaptureButton(
                        uiState = VoiceUiState(
                            stage = VoiceUiStage.Idle,
                            transcript = "Plan the next block",
                        ),
                        onStartCapture = {},
                        onCancelCapture = {},
                        onReleaseCapture = {},
                    )
                    VoiceCaptureButton(
                        uiState = VoiceUiState(
                            stage = VoiceUiStage.Error,
                            errorMessage = "Mic error",
                        ),
                        onStartCapture = {},
                        onCancelCapture = {},
                        onReleaseCapture = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText("Ready: Plan the next block").assertIsDisplayed()
        composeRule.onNodeWithText("Mic error").assertIsDisplayed()
        composeRule.onAllNodesWithText("Tap again to grant permission").assertCountEquals(0)
    }

    @Test
    fun transcribing_sending_and_speaking_states_are_rendered() {
        composeRule.setContent {
            AuraTheme {
                androidx.compose.foundation.layout.Column {
                    VoiceCaptureButton(
                        uiState = VoiceUiState(stage = VoiceUiStage.Transcribing),
                        onStartCapture = {},
                        onCancelCapture = {},
                        onReleaseCapture = {},
                    )
                    VoiceCaptureButton(
                        uiState = VoiceUiState(stage = VoiceUiStage.Sending),
                        onStartCapture = {},
                        onCancelCapture = {},
                        onReleaseCapture = {},
                    )
                    VoiceCaptureButton(
                        uiState = VoiceUiState(stage = VoiceUiStage.Speaking),
                        onStartCapture = {},
                        onCancelCapture = {},
                        onReleaseCapture = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText("Transcribing...").assertIsDisplayed()
        composeRule.onNodeWithText("Sending...").assertIsDisplayed()
        composeRule.onNodeWithText("AURA is speaking").assertIsDisplayed()
    }
}
