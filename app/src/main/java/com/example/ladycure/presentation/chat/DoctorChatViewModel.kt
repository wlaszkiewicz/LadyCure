package com.example.ladycure.presentation.chat

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.ChatRepository
import com.example.ladycure.data.repository.DoctorRepository
import com.example.ladycure.domain.model.Doctor
import com.example.ladycure.domain.model.Message
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DoctorChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val doctorRepository: DoctorRepository
) : ViewModel() {

    val currentUserId: String = chatRepository.getCurrentUserId()

    var messages by mutableStateOf<List<Message>>(emptyList())
        private set

    var isSending by mutableStateOf(false)
        private set

    var otherUserProfilePictureUrl by mutableStateOf<String?>(null)
        private set

    var currentUserProfilePictureUrl by mutableStateOf<String?>(null)
        private set

    var otherUserPhoneNumber by mutableStateOf<String?>(null)
        private set

    var otherUserRole by mutableStateOf<String?>(null)
        private set

    var currentDoctor by mutableStateOf<Doctor?>(null)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun loadChatData(chatId: String, otherUserId: String) {
        viewModelScope.launch {
            chatRepository.getMessages(chatId) { messageList ->
                messages = messageList
            }
            otherUserProfilePictureUrl = chatRepository.getUserProfilePicture(otherUserId)
            currentUserProfilePictureUrl = chatRepository.getUserProfilePicture(currentUserId)
            chatRepository.getSpecificUserData(otherUserId).onSuccess { userData ->
                otherUserPhoneNumber = userData?.get("phone") as? String
                otherUserRole = userData?.get("role") as? String
            }.onFailure { e ->
                error = "Failed to load user data: ${e.message}"
            }
        }
    }

    fun fetchDoctorProfile(otherUserId: String) {
        viewModelScope.launch {
            val result = doctorRepository.getDoctors()
            result.onSuccess { doctors ->
                currentDoctor = doctors.find { it.id == otherUserId }
            }.onFailure {
                error = "Failed to load doctor profile"
            }
        }
    }

    fun sendMessage(
        chatId: String,
        text: String,
        attachmentUri: Uri?,
        attachmentFileName: String?,
        attachmentMimeType: String?,
        otherUserId: String
    ) {
        viewModelScope.launch {
            isSending = true
            try {
                val userName = chatRepository.getCurrentUserName()
                val message = Message(
                    sender = currentUserId,
                    senderName = userName,
                    recipient = otherUserId,
                    text = text,
                    timestamp = Timestamp.now(),
                    attachmentUrl = if (attachmentUri != null) {
                        chatRepository.uploadFile(attachmentUri)
                    } else null,
                    attachmentFileName = attachmentFileName,
                    attachmentMimeType = attachmentMimeType
                )
                chatRepository.sendMessage(chatId, message)
            } catch (e: Exception) {
                error = "Message could not be sent: ${e.message}"
            } finally {
                isSending = false
            }
        }
    }

    fun clearError() { error = null }
}
