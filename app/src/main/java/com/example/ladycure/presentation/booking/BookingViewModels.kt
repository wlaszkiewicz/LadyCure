package com.example.ladycure.presentation.booking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.DoctorRepository
import com.example.ladycure.domain.model.Doctor
import com.example.ladycure.domain.model.DoctorAvailability
import com.example.ladycure.domain.model.Speciality
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class BookingViewModel(
    private val doctorRepo: DoctorRepository = DoctorRepository()
) : ViewModel() {

    // State variables
    var isLoading by mutableStateOf(true)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        internal set
    var doctors by mutableStateOf(emptyList<Doctor>())
        private set
    var doctorAvailabilities by mutableStateOf(emptyList<DoctorAvailability>())
        private set

    // UI state
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
                // Get doctors first
                val doctorsResult = doctorRepo.getDoctorsBySpeciality(speciality.displayName)
                if (doctorsResult.isSuccess) {
                    doctors = doctorsResult.getOrNull()?.filter { it.city == city } ?: emptyList()

                    // Then get their availabilities
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

    // Helper functions
    val availableDates: List<LocalDate>
        get() = doctorAvailabilities
            .mapNotNull { it.date }
            .filter { it.isAfter(LocalDate.now()) || it.isEqual(LocalDate.now()) }
            .distinct()
            .sorted()

    fun getTimeSlotsForSelectedDate(appointmentDuration: Int): List<String> {
        return if (selectedDate == null) emptyList() else {
            filterTimeSlotsForDate(selectedDate!!, doctorAvailabilities, appointmentDuration)
        }
    }

    fun getAvailableDoctorsForSlot(appointmentDuration: Int): List<Doctor> {
        return if (selectedTimeSlot == null || selectedDate == null) emptyList() else {
            filterAvailableDoctors(
                doctors,
                selectedDate!!,
                selectedTimeSlot!!,
                doctorAvailabilities,
                appointmentDuration
            )
        }
    }

    fun createTimestamp(): Timestamp {
        return Timestamp(
            selectedDate!!.atTime(selectedTimeSlot!!)
                .atZone(ZoneId.systemDefault()).toInstant()
        )
    }

    private fun filterTimeSlotsForDate(
        date: LocalDate,
        availabilities: List<DoctorAvailability>,
        appointmentDuration: Int
    ): List<String> {
        val now = LocalTime.now()
        val today = LocalDate.now()
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

        val currentTimeWithBuffer = now.plusMinutes(30)

        val validDoctorIds = getDoctorsWithEnoughSlots(date, appointmentDuration, availabilities)

        val validSlots = availabilities
            .filter { it.date == date && it.doctorId in validDoctorIds }
            .flatMap { it.availableSlots }
            .distinct()
            .sorted()

        val requiredSlots = appointmentDuration / 15
        val availableStartSlots = mutableListOf<LocalTime>()

        for (doctorId in validDoctorIds) {
            val doctorSlots = availabilities
                .filter { it.doctorId == doctorId && it.date == date }
                .flatMap { it.availableSlots }
                .sorted()

            for (i in 0..(doctorSlots.size - requiredSlots)) {
                val startSlot = doctorSlots[i]

                if (date == today) {
                    if (!startSlot.isAfter(currentTimeWithBuffer)) {
                        continue
                    }
                }

                var hasConsecutive = true
                for (j in 1 until requiredSlots) {
                    val expectedSlot = startSlot.plusMinutes((15 * j).toLong())
                    if (doctorSlots.getOrNull(i + j) != expectedSlot) {
                        hasConsecutive = false
                        break
                    }
                }

                if (hasConsecutive && !availableStartSlots.contains(startSlot)) {
                    availableStartSlots.add(startSlot)
                }
            }
        }

        return availableStartSlots.sorted()
            .map { it.format(timeFormatter) }
    }

    private fun filterAvailableDoctors(
        doctors: List<Doctor>,
        date: LocalDate,
        timeSlot: LocalTime,
        availabilities: List<DoctorAvailability>,
        appointmentDuration: Int
    ): List<Doctor> {
        val requiredSlots = appointmentDuration / 15

        val availableDoctorIds = availabilities
            .filter { availability ->
                availability.date == date &&
                        availability.availableSlots.contains(timeSlot)
            }
            .filter { availability ->
                // Check if this doctor has all required slots
                val doctorSlots = availabilities
                    .filter { it.doctorId == availability.doctorId && it.date == date }
                    .flatMap { it.availableSlots }
                    .sorted()

                val startIndex = doctorSlots.indexOf(timeSlot)
                if (startIndex == -1 || startIndex + requiredSlots > doctorSlots.size) {
                    false
                } else {
                    (1 until requiredSlots).all { i ->
                        doctorSlots[startIndex + i] == timeSlot.plusMinutes((15 * i).toLong())
                    }
                }
            }
            .map { it.doctorId }
            .toSet()

        return doctors.filter { doctor ->
            availableDoctorIds.contains(doctor.id)
        }
    }

    private fun getDoctorsWithEnoughSlots(
        date: LocalDate,
        appointmentDuration: Int,
        availabilities: List<DoctorAvailability>
    ): Set<String> {
        val requiredSlots = appointmentDuration / 15
        val validDoctorIds = mutableSetOf<String>()

        // Group availabilities by doctor
        val availabilitiesByDoctor = availabilities
            .filter { it.date == date }
            .groupBy { it.doctorId }

        // Check each doctor's slots
        for ((doctorId, doctorAvailabilities) in availabilitiesByDoctor) {
            // Get all slots for this doctor on this date
            val allSlots = doctorAvailabilities
                .flatMap { it.availableSlots }
                .sorted()

            // Check for consecutive slots
            for (i in 0..(allSlots.size - requiredSlots)) {
                val startSlot = allSlots[i]
                var hasConsecutive = true

                // Check next slots - using toLong() for minutes
                for (j in 1 until requiredSlots) {
                    val expectedSlot = startSlot.plusMinutes((15 * j).toLong())
                    if (allSlots.getOrNull(i + j) != expectedSlot) {
                        hasConsecutive = false
                        break
                    }
                }

                if (hasConsecutive) {
                    validDoctorIds.add(doctorId)
                    break // Doctor is valid if they have at least one valid block
                }
            }
        }

        return validDoctorIds
    }
}