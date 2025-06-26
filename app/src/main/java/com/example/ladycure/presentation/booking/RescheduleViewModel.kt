package com.example.ladycure.presentation.booking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.AppointmentRepository
import com.example.ladycure.data.repository.DoctorRepository
import com.example.ladycure.domain.model.Appointment
import com.example.ladycure.domain.model.Doctor
import com.example.ladycure.domain.model.DoctorAvailability
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * ViewModel for the reschedule appointment screen.
 *
 * Handles fetching appointment and doctor data, managing UI state for date and time selection,
 * and orchestrating the rescheduling process.
 *
 * @param appointmentRepo The repository for appointment-related operations.
 * @param doctorRepo The repository for doctor-related operations.
 */
class RescheduleViewModel(
    private val appointmentRepo: AppointmentRepository = AppointmentRepository(),
    private val doctorRepo: DoctorRepository = DoctorRepository()
) : ViewModel() {

    /** Indicates whether data is currently being loaded. */
    var isLoading by mutableStateOf(true)
        private set

    /** Holds any error messages that occur during operations. */
    var error by mutableStateOf("")
        internal set

    /** The appointment data being displayed and potentially rescheduled. */
    var appointment by mutableStateOf<Appointment?>(null)
        private set

    /** The doctor associated with the appointment. */
    var doctor by mutableStateOf<Doctor?>(null)
        private set

    /** The list of available dates and time slots for the doctor. */
    var doctorAvailability by mutableStateOf<List<DoctorAvailability>>(emptyList())
        private set

    /** The currently selected date for rescheduling. */
    var selectedDate by mutableStateOf<LocalDate?>(null)
        private set

    /** The currently selected time slot for rescheduling. */
    var selectedTimeSlot by mutableStateOf<LocalTime?>(null)
        private set

    /** Controls the visibility of the reschedule confirmation dialog. */
    var showRescheduleDialog by mutableStateOf(false)
        private set

    /** Controls the visibility of the reschedule success dialog. */
    var showRescheduleSuccessDialog by mutableStateOf(false)
        private set

    /**
     * Loads the appointment data, along with the associated doctor and their availability.
     *
     * @param appointmentId The ID of the appointment to load.
     */
    fun loadAppointmentData(appointmentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            try {
                val appointmentResult = appointmentRepo.getAppointmentById(appointmentId)
                if (appointmentResult.isSuccess) {
                    appointment = appointmentResult.getOrNull()

                    val doctorDeferred = async {
                        appointment?.doctorId?.let {
                            doctorRepo.getDoctorById(it).getOrNull()
                        }
                    }
                    val availabilityDeferred = async {
                        appointment?.doctorId?.let {
                            doctorRepo.getDoctorAvailability(it).getOrNull() ?: emptyList()
                        }
                    }

                    doctor = doctorDeferred.await()
                    doctorAvailability = availabilityDeferred.await() ?: emptyList()
                } else {
                    error =
                        appointmentResult.exceptionOrNull()?.message ?: "Failed to load appointment"
                }
            } catch (e: Exception) {
                error = e.message ?: "An error occurred"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Sets the selected date for rescheduling.
     *
     * @param date The date selected by the user.
     */
    fun selectDate(date: LocalDate?) {
        selectedDate = date
        selectedTimeSlot = null
    }

    /**
     * Sets the selected time slot for rescheduling and shows the reschedule confirmation dialog.
     *
     * @param time The time slot selected by the user.
     */
    fun selectTimeSlot(time: LocalTime?) {
        selectedTimeSlot = time
        showRescheduleDialog = true
    }

    /**
     * Initiates the appointment rescheduling process.
     *
     * @param appointmentId The ID of the appointment to reschedule.
     * @param onSuccess A callback to be invoked if the reschedule is successful.
     * @param onError A callback to be invoked if an error occurs during rescheduling.
     */
    fun rescheduleAppointment(
        appointmentId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (selectedDate != null && selectedTimeSlot != null) {
                val result = appointmentRepo.rescheduleAppointment(
                    appointmentId,
                    selectedTimeSlot!!,
                    selectedDate!!
                )
                if (result.isSuccess) {
                    showRescheduleDialog = false
                    showRescheduleSuccessDialog = true
                    onSuccess()
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Failed to reschedule")
                }
            }
        }
    }

    /** Dismisses the reschedule confirmation dialog. */
    fun dismissRescheduleDialog() {
        showRescheduleDialog = false
    }

    /** Dismisses the reschedule success dialog. */
    fun dismissSuccessDialog() {
        showRescheduleSuccessDialog = false
    }

    /**
     * Returns a sorted list of available dates for the doctor, excluding past dates.
     * Dates are distinct.
     */
    val availableDates: List<LocalDate>
        get() = doctorAvailability
            .mapNotNull { it.date }
            .filter { it.isAfter(LocalDate.now()) || it.isEqual(LocalDate.now()) }
            .distinct()
            .sorted()

    /**
     * Returns a list of available time slots for the [selectedDate].
     *
     * @return A list of [LocalTime] representing available slots, or an empty list if no date is selected.
     */
    fun getTimeSlotsForSelectedDate(): List<LocalTime> {
        return if (selectedDate == null) emptyList() else {
            filterTimeSlotsForDate(selectedDate!!, doctorAvailability)
        }
    }

    /**
     * Creates a Firebase [Timestamp] from the [selectedDate] and [selectedTimeSlot].
     *
     * @return A [Timestamp] object representing the selected date and time.
     */
    fun createTimestamp(): Timestamp {
        return Timestamp(
            selectedDate!!.atTime(selectedTimeSlot!!)
                .atZone(ZoneId.systemDefault()).toInstant()
        )
    }

    /**
     * Filters and sorts the available time slots for a given date.
     *
     * @param date The date for which to filter time slots.
     * @param availabilities The list of doctor availabilities.
     * @return A sorted list of [LocalTime] objects representing available slots for the given date.
     */
    private fun filterTimeSlotsForDate(
        date: LocalDate,
        availabilities: List<DoctorAvailability>
    ): List<LocalTime> {
        val now = LocalTime.now()
        return availabilities
            .filter { it.date == date }
            .flatMap { it.availableSlots }
            .filter { date != LocalDate.now() || it.isAfter(now) }
            .distinct()
            .sorted()
    }
}