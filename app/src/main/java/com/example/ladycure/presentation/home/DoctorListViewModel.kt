package com.example.ladycure.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.DoctorRepository
import com.example.ladycure.domain.model.Doctor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DoctorListViewModel @Inject constructor(
    private val doctorRepository: DoctorRepository
) : ViewModel() {

    var doctors by mutableStateOf<List<Doctor>>(emptyList())
        private set

    var swipeableDoctors by mutableStateOf<List<Doctor>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun loadDoctors(speciality: String) {
        viewModelScope.launch {
            isLoading = true
            val result = doctorRepository.getDoctorsBySpeciality(speciality)
            if (result.isSuccess) {
                doctors = result.getOrDefault(emptyList())
                swipeableDoctors = doctors.shuffled()
            } else {
                error = "Failed to load doctors: ${result.exceptionOrNull()?.message}"
            }
            isLoading = false
        }
    }

    fun clearError() {
        error = null
    }
}
