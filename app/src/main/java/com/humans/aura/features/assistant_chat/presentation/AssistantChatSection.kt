package com.humans.aura.features.assistant_chat.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.humans.aura.core.domain.models.ChatMessage
import com.humans.aura.core.domain.models.ChatRole
import com.humans.aura.core.presentation.components.MarkdownMessageText
import com.humans.aura.features.voice.presentation.VoiceCaptureButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun AssistantChatSection(
    viewModel: AssistantChatViewModel = koinViewModel(),
    voiceCaptureButton: @Composable ((String) -> Unit) -> Unit = { onTranscribed -> VoiceCaptureButton(onSendTranscript = onTranscribed) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AssistantChatSection(
        uiState = uiState,
        onDraftChanged = viewModel::onDraftChanged,
        onClearChat = viewModel::clearChat,
        onSendMessage = viewModel::sendMessage,
        voiceCaptureButton = { voiceCaptureButton(viewModel::sendVoiceMessage) },
    )
}

@Composable
fun AssistantChatSection(
    uiState: AssistantChatUiState,
    onDraftChanged: (String) -> Unit,
    onClearChat: () -> Unit,
    onSendMessage: () -> Unit,
    voiceCaptureButton: @Composable () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when messages change
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("assistant_chat_section"),
    ) {

        // ── Loading Bar ─────────────────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.isLoading || uiState.isSending,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp)),
                color = MaterialTheme.colorScheme.onBackground,
                trackColor = MaterialTheme.colorScheme.outline,
            )
        }

        // ── Error Banner ────────────────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.lastErrorMessage != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Text(
                    text = uiState.lastErrorMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }

        if (!uiState.status.isAiConfigured) {
            AiConfigurationCard(
                status = uiState.status,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }

        // ── Messages / Empty State ──────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            if (uiState.messages.isEmpty()) {
                // ── Empty State ──────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "AURA",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Ask AURA about your day, goals, or what to do next.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                // ── Chat Messages ────────────────────────────────────────
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 16.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.messages, key = ChatMessage::id) { message ->
                        ChatBubble(message)
                    }
                }
            }
        }

        // ── Thinking Indicator ──────────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.isSending,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Text(
                text = "Thinking...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
        }

        // ── Bottom Composer ─────────────────────────────────────────────
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline,
            thickness = 0.5.dp,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = {
                        keyboardController?.hide()
                        onClearChat()
                    },
                    enabled = !uiState.isSending && (
                        uiState.messages.isNotEmpty() ||
                            uiState.draftMessage.isNotBlank() ||
                            uiState.lastErrorMessage != null
                    ),
                    modifier = Modifier.testTag("assistant_chat_clear_button"),
                ) {
                    Text("Clear Chat")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // ── Text Input ──────────────────────────────────────────
                BasicTextField(
                    value = uiState.draftMessage,
                    onValueChange = onDraftChanged,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    singleLine = false,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        keyboardController?.hide()
                        onSendMessage()
                    }),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("assistant_chat_input"),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(16.dp),
                                )
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                        ) {
                            if (uiState.draftMessage.isEmpty()) {
                                Text(
                                    text = "Message AURA",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                )
                            }
                            innerTextField()
                        }
                    },
                )

                // ── Send Button ─────────────────────────────────────────
                Button(
                    onClick = {
                        keyboardController?.hide()
                        onSendMessage()
                    },
                    enabled = uiState.draftMessage.isNotBlank() && !uiState.isSending,
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("assistant_chat_send_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.outline,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Text(
                        text = "SEND",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                }
            }

            // ── Voice Button Slot ───────────────────────────────────────
            voiceCaptureButton()
        }
    }
}

@Composable
private fun AiConfigurationCard(
    status: AssistantChatStatus,
    modifier: Modifier = Modifier,
) {
    if (status.isAiConfigured) return
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("assistant_chat_ai_status"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "AI key missing",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = "Add your AI API key to local properties so AI features can respond.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

// ── Chat Bubble ─────────────────────────────────────────────────────────────

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == ChatRole.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(
                    color = if (isUser) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp,
                    ),
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = if (isUser) "You" else "AURA",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isUser) {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )

            MarkdownMessageText(
                markdown = message.originalText,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
            )
        }
    }
}
