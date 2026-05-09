package com.example.ladycure.presentation.home.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.AppointmentRepository
import com.example.ladycure.domain.model.Appointment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppointmentCardViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    var fullAppointment by mutableStateOf<Appointment?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var cancelSuccess by mutableStateOf(false)
        private set

    fun loadAppointment(appointmentId: String) {
        viewModelScope.launch {
            isLoading = true
            val result = appointmentRepository.getAppointmentById(appointmentId)
            if (result.isSuccess) {
                fullAppointment = result.getOrNull()
            } else {
                error = result.exceptionOrNull()?.message ?: "Failed to load appointment details"
            }
            isLoading = false
        }
    }

    fun cancelAppointment(appointmentId: String) {
        viewModelScope.launch {
            val result = appointmentRepository.cancelAppointment(appointmentId)
            if (result.isFailure) {
                error = "Update failed: ${result.exceptionOrNull()?.message}"
            } else {
                cancelSuccess = true
            }
        }
    }

    fun clearError() { error = null }
    fun clearCancelSuccess() { cancelSuccess = false }
}
