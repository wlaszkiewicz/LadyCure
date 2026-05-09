package com.example.ladycure.presentation.availability

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.AuthRepository
import com.example.ladycure.data.repository.DoctorRepository
import com.example.ladycure.domain.model.DoctorAvailability
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AvailabilityListViewModel @Inject constructor(
    private val doctorRepository: DoctorRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    var availabilities by mutableStateOf<List<DoctorAvailability>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun getCurrentUserId(): String? = authRepository.getCurrentUserId()

    fun loadAvailability(doctorId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val result = doctorRepository.getDoctorAvailability(doctorId)
                if (result.isSuccess) {
                    availabilities = result.getOrThrow()
                } else {
                    error = "Error loading availability data"
                }
            } catch (e: Exception) {
                error = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() {
        error = null
    }
}
