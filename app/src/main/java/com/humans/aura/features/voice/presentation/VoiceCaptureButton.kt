package com.humans.aura.features.voice.presentation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import kotlin.math.abs

private const val CANCEL_THRESHOLD_X = 96f

@Composable
fun VoiceCaptureButton(
    viewModel: VoiceViewModel = koinViewModel(),
    onSendTranscript: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = viewModel::onPermissionResult,
    )
    VoiceCaptureButton(
        uiState = uiState,
        onRequestPermission = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
        onStartCapture = viewModel::startCapture,
        onCancelCapture = viewModel::cancelCapture,
        onReleaseCapture = { viewModel.finishCapture(onSendTranscript) },
    )
}

@Composable
fun VoiceCaptureButton(
    uiState: VoiceUiState,
    onRequestPermission: () -> Unit = {},
    onStartCapture: () -> Unit,
    onCancelCapture: () -> Unit,
    onReleaseCapture: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                color = when (uiState.stage) {
                    VoiceUiStage.Listening,
                    VoiceUiStage.PartialReady,
                    -> MaterialTheme.colorScheme.primaryContainer

                    VoiceUiStage.PermissionDenied,
                    VoiceUiStage.Error,
                    -> MaterialTheme.colorScheme.errorContainer

                    else -> MaterialTheme.colorScheme.secondaryContainer
                },
                shape = RoundedCornerShape(20.dp),
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (uiState.stage == VoiceUiStage.PermissionDenied) {
                        onRequestPermission()
                        return@awaitEachGesture
                    }
                    onStartCapture()
                    var cancelled = false
                    var pointer = down.id
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == pointer } ?: break
                        val dragDistance = change.position - down.position
                        if (!cancelled && abs(dragDistance.x) >= CANCEL_THRESHOLD_X && dragDistance.x < 0f) {
                            cancelled = true
                            onCancelCapture()
                        }
                        if (!change.pressed) {
                            if (!cancelled) {
                                onReleaseCapture()
                            }
                            break
                        }
                    }
                }
            }
            .padding(horizontal = 18.dp)
            .testTag("voice_capture_button"),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = when (uiState.stage) {
                    VoiceUiStage.Cancelled -> "Cancelled"
                    VoiceUiStage.Listening -> "Release to send or swipe left to cancel"
                    VoiceUiStage.PartialReady -> "Listening... ${uiState.partialTranscript}"
                    VoiceUiStage.Transcribing -> "Transcribing..."
                    VoiceUiStage.Sending -> "Sending..."
                    VoiceUiStage.Speaking -> "AURA is speaking"
                    VoiceUiStage.PermissionDenied -> "Enable microphone access"
                    VoiceUiStage.Error -> uiState.errorMessage ?: "Voice error"
                    VoiceUiStage.Idle -> if (uiState.transcript.isNotBlank()) "Ready: ${uiState.transcript}" else "Hold to talk"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = when (uiState.stage) {
                    VoiceUiStage.PermissionDenied,
                    VoiceUiStage.Error,
                    -> MaterialTheme.colorScheme.onErrorContainer

                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                },
                fontWeight = FontWeight.SemiBold,
            )

            if (uiState.stage == VoiceUiStage.PermissionDenied) {
                Text(
                    text = "Tap again to grant permission",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.testTag("voice_permission_hint"),
                )
            }
        }
    }
}
