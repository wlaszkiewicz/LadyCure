package com.example.ladycure.presentation.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.AppointmentRepository
import com.example.ladycure.data.repository.UserRepository
import com.example.ladycure.domain.model.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    var role by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(true)
        private set

    var isLoadingAdditional by mutableStateOf(false)
        private set

    var error by mutableStateOf("")
        private set

    var activeParticipants by mutableStateOf<List<ChatParticipantInfo>>(emptyList())
        private set

    var allPossibleParticipants by mutableStateOf<List<ChatParticipantInfo>>(emptyList())
        private set

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val roleResult = userRepository.getUserRole()
            if (roleResult.isSuccess) {
                role = roleResult.getOrNull() ?: ""
            } else {
                error = roleResult.exceptionOrNull()?.message ?: "Unknown error"
            }

            val activeResult = appointmentRepository.getActiveChatParticipants()
            if (activeResult.isSuccess) {
                activeParticipants = activeResult.getOrNull()
                    ?.sortedByDescending { it.lastMessageTime ?: 0 }
                    ?: emptyList()
            } else {
                error = activeResult.exceptionOrNull()?.message ?: "Failed to load active chats"
            }
            isLoading = false
        }
    }

    fun loadPossibleParticipants() {
        if (isLoadingAdditional || allPossibleParticipants.isNotEmpty()) return
        viewModelScope.launch {
            isLoadingAdditional = true
            try {
                val result = if (Role.DOCTOR == Role.fromValue(role)) {
                    appointmentRepository.getPatientsFromAppointmentsWithUids()
                } else {
                    appointmentRepository.getDoctorsFromAppointmentsWithUids()
                }
                if (result.isSuccess) {
                    allPossibleParticipants = result.getOrNull()?.distinct() ?: emptyList()
                } else {
                    error = result.exceptionOrNull()?.message ?: "Failed to load participants"
                }
            } finally {
                isLoadingAdditional = false
            }
        }
    }

    fun clearError() {
        error = ""
    }
}
