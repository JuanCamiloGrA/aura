package com.humans.aura.features.assistant_chat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.humans.aura.core.domain.models.ChatMessage
import com.humans.aura.core.domain.models.ChatRole
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
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Assistant",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Ask for next steps, a reset, or a quick reflection.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (uiState.isLoading || uiState.isSending) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (uiState.lastErrorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(20.dp),
                        )
                        .padding(14.dp),
                ) {
                    Text(
                        text = uiState.lastErrorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            if (uiState.messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(26.dp),
                        )
                        .padding(18.dp),
                ) {
                    Text(
                        text = "Ask AURA about your day, goals, or what to do next.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(uiState.messages, key = ChatMessage::id) { message ->
                        ChatBubble(message)
                    }
                }
            }

            OutlinedTextField(
                value = uiState.draftMessage,
                onValueChange = onDraftChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("assistant_chat_input"),
                label = { Text("Message AURA") },
                supportingText = { Text("Short prompts work best.") },
            )

            Button(
                onClick = onSendMessage,
                enabled = uiState.draftMessage.isNotBlank() && !uiState.isSending,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("assistant_chat_send_button"),
            ) {
                Text(if (uiState.isSending) "Thinking..." else "Send")
            }

            voiceCaptureButton()
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == ChatRole.USER
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(24.dp),
            )
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (isUser) "You" else "AURA",
                style = MaterialTheme.typography.labelLarge,
                color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = message.originalText,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
