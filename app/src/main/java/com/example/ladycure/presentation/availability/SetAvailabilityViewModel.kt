package com.example.ladycure.presentation.availability

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.AuthRepository
import com.example.ladycure.data.repository.DoctorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SetAvailabilityViewModel @Inject constructor(
    private val doctorRepository: DoctorRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    var state by mutableStateOf(AvailabilityScreenState())
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var saveSuccess by mutableStateOf<String?>(null)
        private set

    fun getCurrentUserId(): String? = authRepository.getCurrentUserId()

    fun updateState(newState: AvailabilityScreenState) {
        state = newState
    }

    fun loadAvailabilities(doctorId: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                val result = doctorRepository.getDoctorAvailability(doctorId)
                if (result.isSuccess) {
                    state = state.copy(existingAvailabilities = result.getOrThrow())
                } else {
                    error = "Error loading existing availabilities"
                }
            } catch (e: Exception) {
                error = "Error loading existing availabilities: ${e.message}"
            } finally {
                state = state.copy(isLoading = false)
            }
        }
    }

    fun saveAvailabilities(
        doctorId: String,
        dates: Set<LocalDate>,
        startTime: LocalTime,
        endTime: LocalTime
    ) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                doctorRepository.updateAvailabilities(
                    dates = dates.toList(),
                    startTime = startTime,
                    endTime = endTime,
                    doctorId = doctorId
                )
                saveSuccess = "Availability saved successfully!"
                val newAvailabilities = doctorRepository.getDoctorAvailability(doctorId).getOrThrow()
                state = state.copy(existingAvailabilities = newAvailabilities)
            } catch (e: Exception) {
                error = "Error saving availability: ${e.message}"
            } finally {
                state = state.copy(isLoading = false)
            }
        }
    }

    fun clearError() { error = null }
    fun clearSaveSuccess() { saveSuccess = null }
}
