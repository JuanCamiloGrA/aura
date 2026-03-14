package com.humans.aura.features.assistant_chat.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humans.aura.features.voice.presentation.VoiceCaptureButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun AssistantChatSection(
    viewModel: AssistantChatViewModel = koinViewModel(),
    voiceCaptureButton: @Composable ((String) -> Unit) -> Unit = { onSend -> VoiceCaptureButton(onSendTranscript = onSend) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AssistantChatSection(
        uiState = uiState,
        onDraftChanged = viewModel::onDraftChanged,
        onSendMessage = viewModel::sendMessage,
        voiceCaptureButton = { voiceCaptureButton(viewModel::sendVoiceMessage) },
    )
}

@Composable
fun AssistantChatSection(
    uiState: AssistantChatUiState,
    onDraftChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    voiceCaptureButton: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("assistant_chat_section"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Assistant chat",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (uiState.messages.isEmpty()) {
                Text(
                    text = "Ask AURA about your day, goals, or what to do next.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                uiState.messages.forEach { message ->
                    Text(
                        text = "${message.role.name.lowercase().replaceFirstChar(Char::uppercase)}: ${message.originalText}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            OutlinedTextField(
                value = uiState.draftMessage,
                onValueChange = onDraftChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("assistant_chat_input"),
                label = { Text("Message AURA") },
            )

            Button(
                onClick = onSendMessage,
                enabled = uiState.draftMessage.isNotBlank() && !uiState.isSending,
                modifier = Modifier.testTag("assistant_chat_send_button"),
            ) {
                Text("Send")
            }

            voiceCaptureButton()
        }
    }
}
