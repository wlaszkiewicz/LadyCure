package com.example.ladycure.presentation.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatParticipantItemViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    var profilePictureUrl by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun loadData(uid: String) {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            try {
                profilePictureUrl = chatRepository.getUserProfilePicture(uid)
            } finally {
                isLoading = false
            }
        }
    }
}
