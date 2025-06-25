package com.example.ladycure.presentation.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.ChatRepository
import kotlinx.coroutines.launch

class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

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