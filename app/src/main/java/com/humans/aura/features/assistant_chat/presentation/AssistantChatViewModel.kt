package com.humans.aura.features.assistant_chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humans.aura.features.assistant_chat.domain.EnsureChatSessionUseCase
import com.humans.aura.features.assistant_chat.domain.ObserveChatMessagesUseCase
import com.humans.aura.features.assistant_chat.domain.SendChatMessageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AssistantChatViewModel(
    private val ensureChatSessionUseCase: EnsureChatSessionUseCase,
    observeChatMessagesUseCase: ObserveChatMessagesUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase,
) : ViewModel() {

    private val activeSession = MutableStateFlow<com.humans.aura.core.domain.models.ChatSession?>(null)
    private val draftMessage = MutableStateFlow("")
    private val isSending = MutableStateFlow(false)
    private val activeMessages = activeSession.flatMapLatest { session ->
        if (session == null) {
            flowOf(emptyList())
        } else {
            observeChatMessagesUseCase(session.id)
        }
    }

    val uiState: StateFlow<AssistantChatUiState> = combine(
        activeSession,
        activeMessages,
        draftMessage,
        isSending,
    ) { session, messages, draft, sending ->
        AssistantChatUiState(
            activeSession = session,
            messages = messages,
            draftMessage = draft,
            isSending = sending,
            isLoading = session == null,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AssistantChatUiState(isLoading = true),
    )

    init {
        viewModelScope.launch {
            activeSession.value = ensureChatSessionUseCase()
        }
    }

    fun onDraftChanged(value: String) {
        draftMessage.value = value
    }

    fun sendMessage(message: String = draftMessage.value) {
        if (message.isBlank() || isSending.value) return
        viewModelScope.launch {
            isSending.value = true
            runCatching {
                sendChatMessageUseCase(
                    originalText = message,
                    normalizedEnglishText = message,
                    sourceLanguageCode = "en",
                )
            }.onSuccess {
                draftMessage.value = ""
            }.also {
                isSending.value = false
            }
        }
    }

    fun sendVoiceMessage(message: String) {
        sendMessage(message)
    }
}
