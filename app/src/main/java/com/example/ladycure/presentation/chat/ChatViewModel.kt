package com.example.ladycure.presentation.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.ChatRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for managing chat-related operations.
 *
 * This ViewModel interacts with the [ChatRepository] to perform operations like initializing chats.
 */
class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    /**
     * Initializes a chat with the given [chatId] and [participants].
     *
     * If the chat does not exist, it will be created.
     *
     * @param chatId The unique identifier for the chat.
     * @param participants The list of participants in the chat.
     */
    fun initializeChat(chatId: String, participants: List<String>) {
        viewModelScope.launch {
            val result = chatRepository.createChatIfNotExists(chatId, participants)
            if (result.isFailure) {
                Log.e(
                    "ChatViewModel",
                    "Failed to initiate chat $chatId: ${result.exceptionOrNull()?.message}",
                    result.exceptionOrNull()
                )
            } else {
                Log.d("ChatViewModel", "Chat initiated successfully: $chatId")
            }
        }
    }
}