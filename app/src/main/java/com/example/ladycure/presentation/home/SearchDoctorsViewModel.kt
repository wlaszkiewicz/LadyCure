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
class SearchDoctorsViewModel @Inject constructor(
    private val doctorRepository: DoctorRepository
) : ViewModel() {

    var searchQuery by mutableStateOf("")
        private set

    var allDoctors by mutableStateOf(emptyList<Doctor>())
        private set

    var error by mutableStateOf("")
        private set

    val filteredDoctors: List<Doctor>
        get() {
            val queryWords = searchQuery.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
            return if (queryWords.isNotEmpty()) {
                allDoctors.filter { doctor ->
                    queryWords.all { word ->
                        doctor.name.contains(word, ignoreCase = true) ||
                                doctor.surname.contains(word, ignoreCase = true) ||
                                doctor.speciality.displayName.contains(word, ignoreCase = true)
                    }
                }
            } else {
                allDoctors
            }
        }

    init {
        loadDoctors()
    }

    private fun loadDoctors() {
        viewModelScope.launch {
            val result = doctorRepository.getDoctors()
            if (result.isSuccess) {
                allDoctors = result.getOrNull()!!
            } else {
                error = result.exceptionOrNull()?.message ?: "Unknown error"
            }
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun clearError() {
        error = ""
    }
}
