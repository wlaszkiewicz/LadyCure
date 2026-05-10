package com.example.ladycure.presentation.booking

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.DoctorRepository
import com.example.ladycure.domain.model.Doctor
import com.example.ladycure.domain.model.DoctorAvailability
import com.example.ladycure.domain.model.Speciality
import com.example.ladycure.domain.usecase.GetAvailableSlotsUseCase
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val doctorRepo: DoctorRepository,
    private val getAvailableSlotsUseCase: GetAvailableSlotsUseCase
) : ViewModel() {

    var isLoading by mutableStateOf(true)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        internal set
    var doctors by mutableStateOf(emptyList<Doctor>())
        private set
    var doctorAvailabilities by mutableStateOf(emptyList<DoctorAvailability>())
        private set

    var selectedDate by mutableStateOf<LocalDate?>(null)
        private set
    var selectedTimeSlot by mutableStateOf<LocalTime?>(null)
        private set
    var showDoctorsForSlot by mutableStateOf(false)
        private set


    fun loadDoctorsBySpeciality(speciality: Speciality, city: String) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            try {
                val doctorsResult = doctorRepo.getDoctorsBySpeciality(speciality.displayName)
                if (doctorsResult.isSuccess) {
                    doctors = doctorsResult.getOrNull()?.filter { it.city == city } ?: emptyList()

                    doctorAvailabilities = doctorRepo.getAllDoctorAvailabilitiesBySpeciality(
                        speciality.displayName, city
                    )
                }
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Failed to load data: ${e.message}"
                isLoading = false
            }
        }
    }

    fun loadDoctorById(doctorId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            try {
                val result = doctorRepo.getDoctorById(doctorId)
                if (result.isSuccess) {
                    val doctor = result.getOrNull()
                    if (doctor != null) {
                        doctors = listOf(doctor)
                        val availabilityResult = doctorRepo.getDoctorAvailability(doctorId)
                        if (availabilityResult.isSuccess) {
                            doctorAvailabilities = availabilityResult.getOrNull() ?: emptyList()
                        } else {
                            errorMessage = availabilityResult.exceptionOrNull()?.message
                        }
                    }
                } else {
                    errorMessage = result.exceptionOrNull()?.message
                }
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                isLoading = false
            }
        }
    }

    fun selectDate(date: LocalDate?) {
        selectedDate = date
        selectedTimeSlot = null
    }

    fun selectTimeSlot(time: LocalTime?) {
        selectedTimeSlot = time
        showDoctorsForSlot = true
    }

    fun toggleShowDoctorsForSlot(show: Boolean) {
        showDoctorsForSlot = show
    }

    val availableDates: List<LocalDate>
        get() = doctorAvailabilities
            .mapNotNull { it.date }
            .filter { it.isAfter(LocalDate.now()) || it.isEqual(LocalDate.now()) }
            .distinct()
            .sorted()

    fun getTimeSlotsForSelectedDate(appointmentDuration: Int): List<String> {
        return if (selectedDate == null) emptyList()
        else getAvailableSlotsUseCase(selectedDate!!, doctorAvailabilities, appointmentDuration)
    }

    fun getAvailableDoctorsForSlot(appointmentDuration: Int): List<Doctor> {
        return if (selectedTimeSlot == null || selectedDate == null) emptyList()
        else getAvailableSlotsUseCase.getAvailableDoctors(
            doctors, selectedDate!!, selectedTimeSlot!!, doctorAvailabilities, appointmentDuration
        )
    }

    fun createTimestamp(): Timestamp {
        return Timestamp(
            selectedDate!!.atTime(selectedTimeSlot!!)
                .atZone(ZoneId.systemDefault()).toInstant()
        )
    }
}