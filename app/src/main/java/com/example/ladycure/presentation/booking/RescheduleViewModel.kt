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

class RescheduleViewModel(
    private val appointmentRepo: AppointmentRepository = AppointmentRepository(),
    private val doctorRepo: DoctorRepository = DoctorRepository()
) : ViewModel() {

    // State variables
    var isLoading by mutableStateOf(true)
        private set
    var error by mutableStateOf("")
        internal set
    var appointment by mutableStateOf<Appointment?>(null)
        private set
    var doctor by mutableStateOf<Doctor?>(null)
        private set
    var doctorAvailability by mutableStateOf<List<DoctorAvailability>>(emptyList())
        private set

    // UI state
    var selectedDate by mutableStateOf<LocalDate?>(null)
        private set
    var selectedTimeSlot by mutableStateOf<LocalTime?>(null)
        private set
    var showRescheduleDialog by mutableStateOf(false)
        private set
    var showRescheduleSuccessDialog by mutableStateOf(false)
        private set

    fun loadAppointmentData(appointmentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            try {
                // Load appointment
                val appointmentResult = appointmentRepo.getAppointmentById(appointmentId)
                if (appointmentResult.isSuccess) {
                    appointment = appointmentResult.getOrNull()

                    // Load doctor and availability in parallel
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

    fun selectDate(date: LocalDate?) {
        selectedDate = date
        selectedTimeSlot = null
    }

    fun selectTimeSlot(time: LocalTime?) {
        selectedTimeSlot = time
        showRescheduleDialog = true
    }

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

    fun dismissRescheduleDialog() {
        showRescheduleDialog = false
    }

    fun dismissSuccessDialog() {
        showRescheduleSuccessDialog = false
    }

    // Helper properties
    val availableDates: List<LocalDate>
        get() = doctorAvailability
            .mapNotNull { it.date }
            .filter { it.isAfter(LocalDate.now()) || it.isEqual(LocalDate.now()) }
            .distinct()
            .sorted()

    fun getTimeSlotsForSelectedDate(): List<LocalTime> {
        return if (selectedDate == null) emptyList() else {
            filterTimeSlotsForDate(selectedDate!!, doctorAvailability)
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