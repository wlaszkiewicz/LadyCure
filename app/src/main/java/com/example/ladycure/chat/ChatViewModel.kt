package com.example.ladycure.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    fun initializeChat(chatId: String, participants: List<String>) {
        viewModelScope.launch {
            val result = chatRepository.createChat(chatId, participants)
            if (result.isFailure) {
                Log.e("ChatViewModel", "failed to send message: ${result.exceptionOrNull()?.message}")
            }
        }
    }
}