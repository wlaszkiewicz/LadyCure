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

/**
 * ViewModel for managing doctor booking related states and operations.
 *
 * @param doctorRepo The repository for doctor-related data.
 */
class BookingViewModel(
    private val doctorRepo: DoctorRepository = DoctorRepository()
) : ViewModel() {

    /**
     * Indicates whether data is currently being loaded.
     */
    var isLoading by mutableStateOf(true)
        private set

    /**
     * Holds any error message that occurs during data operations.
     */
    var errorMessage by mutableStateOf<String?>(null)
        internal set

    /**
     * The list of doctors loaded.
     */
    var doctors by mutableStateOf(emptyList<Doctor>())
        private set

    /**
     * The list of doctor availabilities loaded.
     */
    var doctorAvailabilities by mutableStateOf(emptyList<DoctorAvailability>())
        private set

    /**
     * The currently selected date for booking.
     */
    var selectedDate by mutableStateOf<LocalDate?>(null)
        private set

    /**
     * The currently selected time slot for booking.
     */
    var selectedTimeSlot by mutableStateOf<LocalTime?>(null)
        private set

    /**
     * Controls the visibility of doctors available for the selected time slot.
     */
    var showDoctorsForSlot by mutableStateOf(false)
        private set


    /**
     * Loads doctors and their availabilities based on speciality and city.
     *
     * @param speciality The speciality to filter doctors by.
     * @param city The city to filter doctors by.
     */
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

    /**
     * Loads a single doctor and their availability by ID.
     *
     * @param doctorId The ID of the doctor to load.
     */
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

    /**
     * Sets the selected date for booking.
     *
     * @param date The date to select.
     */
    fun selectDate(date: LocalDate?) {
        selectedDate = date
        selectedTimeSlot = null
    }

    /**
     * Sets the selected time slot for booking.
     *
     * @param time The time slot to select.
     */
    fun selectTimeSlot(time: LocalTime?) {
        selectedTimeSlot = time
        showDoctorsForSlot = true
    }

    /**
     * Toggles the visibility of doctors for the selected slot.
     *
     * @param show Boolean indicating whether to show doctors for the slot.
     */
    fun toggleShowDoctorsForSlot(show: Boolean) {
        showDoctorsForSlot = show
    }

    /**
     * Returns a list of available dates for appointments, filtered to be today or in the future.
     */
    val availableDates: List<LocalDate>
        get() = doctorAvailabilities
            .mapNotNull { it.date }
            .filter { it.isAfter(LocalDate.now()) || it.isEqual(LocalDate.now()) }
            .distinct()
            .sorted()

    /**
     * Returns a list of available time slots for the selected date, formatted as strings.
     *
     * @param appointmentDuration The duration of the appointment in minutes.
     * @return A list of formatted time slot strings.
     */
    fun getTimeSlotsForSelectedDate(appointmentDuration: Int): List<String> {
        return if (selectedDate == null) emptyList() else {
            filterTimeSlotsForDate(selectedDate!!, doctorAvailabilities, appointmentDuration)
        }
    }

    /**
     * Returns a list of doctors available for the selected date and time slot.
     *
     * @param appointmentDuration The duration of the appointment in minutes.
     * @return A list of available doctors.
     */
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

    /**
     * Creates a Firebase Timestamp object from the selected date and time slot.
     *
     * @return A [Timestamp] object.
     */
    fun createTimestamp(): Timestamp {
        return Timestamp(
            selectedDate!!.atTime(selectedTimeSlot!!)
                .atZone(ZoneId.systemDefault()).toInstant()
        )
    }

    /**
     * Filters time slots for a given date based on doctor availability and appointment duration.
     *
     * @param date The date to filter time slots for.
     * @param availabilities The list of doctor availabilities.
     * @param appointmentDuration The duration of the appointment in minutes.
     * @return A list of formatted time slot strings.
     */
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

    /**
     * Filters the list of doctors to find those available for a specific date and time slot.
     *
     * @param doctors The list of all doctors.
     * @param date The selected date.
     * @param timeSlot The selected time slot.
     * @param availabilities The list of doctor availabilities.
     * @param appointmentDuration The duration of the appointment in minutes.
     * @return A list of doctors available for the specified slot.
     */
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

    /**
     * Identifies doctors who have enough consecutive slots for a given appointment duration on a specific date.
     *
     * @param date The date to check for availabilities.
     * @param appointmentDuration The duration of the appointment in minutes.
     * @param availabilities The list of doctor availabilities.
     * @return A set of doctor IDs who have enough consecutive slots.
     */
    private fun getDoctorsWithEnoughSlots(
        date: LocalDate,
        appointmentDuration: Int,
        availabilities: List<DoctorAvailability>
    ): Set<String> {
        val requiredSlots = appointmentDuration / 15
        val validDoctorIds = mutableSetOf<String>()

        val availabilitiesByDoctor = availabilities
            .filter { it.date == date }
            .groupBy { it.doctorId }

        for ((doctorId, doctorAvailabilities) in availabilitiesByDoctor) {
            val allSlots = doctorAvailabilities
                .flatMap { it.availableSlots }
                .sorted()

            for (i in 0..(allSlots.size - requiredSlots)) {
                val startSlot = allSlots[i]
                var hasConsecutive = true

                for (j in 1 until requiredSlots) {
                    val expectedSlot = startSlot.plusMinutes((15 * j).toLong())
                    if (allSlots.getOrNull(i + j) != expectedSlot) {
                        hasConsecutive = false
                        break
                    }
                }

                if (hasConsecutive) {
                    validDoctorIds.add(doctorId)
                    break
                }
            }
        }

        return validDoctorIds
    }
}