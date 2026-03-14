package com.humans.aura.features.voice.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.geometry.Offset
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
    VoiceCaptureButton(
        uiState = uiState,
        onStartCapture = viewModel::startCapture,
        onCancelCapture = viewModel::cancelCapture,
        onReleaseCapture = { viewModel.finishCapture(onSendTranscript) },
    )
}

@Composable
fun VoiceCaptureButton(
    uiState: VoiceUiState,
    onStartCapture: () -> Unit,
    onCancelCapture: () -> Unit,
    onReleaseCapture: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                color = if (uiState.isListening) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(20.dp),
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
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
        Text(
            text = when {
                uiState.isCancelled -> "Cancelled"
                uiState.isListening -> "Release to send or swipe left to cancel"
                uiState.transcript.isNotBlank() -> "Ready: ${uiState.transcript}"
                uiState.errorMessage != null -> uiState.errorMessage
                else -> "Hold to talk"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
