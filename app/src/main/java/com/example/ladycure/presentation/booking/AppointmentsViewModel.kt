package com.example.ladycure.presentation.booking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.AppointmentRepository
import com.example.ladycure.data.repository.UserRepository
import com.example.ladycure.domain.model.Appointment
import com.example.ladycure.domain.model.Appointment.Status
import com.example.ladycure.domain.model.AppointmentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AppointmentViewModel(
    private val userRepo: UserRepository = UserRepository(),
    private val appointmentRepo: AppointmentRepository = AppointmentRepository()
) : ViewModel() {

    // State variables
    var futureAppointments by mutableStateOf<List<Appointment>>(emptyList())
        private set
    var pastAppointments by mutableStateOf<List<Appointment>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    var selectedAppointment by mutableStateOf<Appointment?>(null)
        private set
    var showEditStatusDialog by mutableStateOf(false)
        private set

    // Filter state variables - now using lists for multiple selections
    var showFilters by mutableStateOf(false)
        private set
    var selectedSpecializations by mutableStateOf<List<String>>(emptyList())
        private set
    var selectedDoctors by mutableStateOf<List<String>>(emptyList())
        private set
    var selectedDate by mutableStateOf<LocalDate?>(null)
        private set
    var selectedTypes by mutableStateOf<List<AppointmentType>>(emptyList())
        private set
    var selectedPatients by mutableStateOf<List<String>>(emptyList())
        private set
    var role by mutableStateOf<String?>(null)
        private set

    init {
        loadAppointments()
    }

    fun loadAppointments() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            try {
                val roleResult = userRepo.getUserRole()
                if (roleResult.isSuccess) {
                    role = roleResult.getOrNull()
                    val appointmentsResult = appointmentRepo.getAppointments(role!!)
                    if (appointmentsResult.isSuccess) {
                        val allAppointments = appointmentsResult.getOrNull() ?: emptyList()
                        updateAppointmentsLists(
                            allAppointments
                        )
                    } else {
                        error = appointmentsResult.exceptionOrNull()?.message
                            ?: "Failed to load appointments"
                    }
                } else {
                    error = roleResult.exceptionOrNull()?.message ?: "Failed to load user role"
                }
            } catch (e: Exception) {
                error = e.message ?: "An error occurred loading appointments"
            } finally {
                isLoading = false
            }
        }
    }

    private fun updateAppointmentsLists(allAppointments: List<Appointment>) {
        futureAppointments = allAppointments.filter {
            it.date.isAfter(LocalDate.now()) ||
                    (it.date == LocalDate.now() && it.time.isAfter(LocalTime.now()))
        }.sortedWith(compareBy({ it.date }, { it.time }))

        pastAppointments = allAppointments.filter {
            it.date.isBefore(LocalDate.now()) ||
                    (it.date == LocalDate.now() && it.time.isBefore(LocalTime.now()))
        }.sortedWith(compareBy({ it.date }, { it.time })).reversed()
    }

    internal fun groupAppointmentsByMonth(appointments: List<Appointment>): Map<String, List<Appointment>> {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        return appointments.groupBy {
            it.date.format(formatter)
        }.toSortedMap(compareByDescending {
            LocalDate.parse("01 $it", DateTimeFormatter.ofPattern("dd MMMM yyyy"))
        })
    }

    fun updateError(message: String?) {
        error = message
    }

    fun selectAppointment(appointment: Appointment) {
        selectedAppointment = appointment
    }

    fun toggleEditStatusDialog(show: Boolean) {
        showEditStatusDialog = show
    }

    fun toggleFilters(show: Boolean) {
        showFilters = show
    }

    // Updated filter functions to handle multiple selections
    fun toggleSpecializationFilter(specialization: String) {
        selectedSpecializations = if (selectedSpecializations.contains(specialization)) {
            selectedSpecializations - specialization
        } else {
            selectedSpecializations + specialization
        }
    }

    fun toggleDoctorFilter(doctor: String) {
        selectedDoctors = if (selectedDoctors.contains(doctor)) {
            selectedDoctors - doctor
        } else {
            selectedDoctors + doctor
        }
    }

    fun setDateFilter(date: LocalDate?) {
        selectedDate = date
    }

    fun toggleTypeFilter(type: AppointmentType) {
        selectedTypes = if (selectedTypes.contains(type)) {
            selectedTypes - type
        } else {
            selectedTypes + type
        }
    }

    fun togglePatientFilter(patient: String) {
        selectedPatients = if (selectedPatients.contains(patient)) {
            selectedPatients - patient
        } else {
            selectedPatients + patient
        }
    }

    fun clearAllFilters() {
        selectedSpecializations = emptyList()
        selectedDoctors = emptyList()
        selectedDate = null
        selectedTypes = emptyList()
        selectedPatients = emptyList()
    }

    fun updateAppointmentStatus(status: Status) {
        selectedAppointment?.let { appointment ->
            viewModelScope.launch(Dispatchers.IO) {
                val result = appointmentRepo.updateAppointmentStatus(
                    appointmentId = appointment.appointmentId,
                    status = status.displayName
                )
                if (result.isFailure) {
                    error = result.exceptionOrNull()?.message
                } else {
                    futureAppointments = futureAppointments.map {
                        if (it.appointmentId == appointment.appointmentId) {
                            it.copy(status = status)
                        } else {
                            it
                        }
                    }
                }
            }
        }
        showEditStatusDialog = false
    }

    fun updateAppointmentComment(
        appointmentId: String,
        newComment: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = appointmentRepo.updateAppointmentComment(appointmentId, newComment)
            if (result.isFailure) {
                error = result.exceptionOrNull()?.message ?: "Failed to update comment"
            } else {
                futureAppointments = futureAppointments.map {
                    if (it.appointmentId == appointmentId) {
                        it.copy(comments = newComment)
                    } else {
                        it
                    }
                }
            }
        }
    }

    fun cancelAppointment(appointmentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = appointmentRepo.cancelAppointment(appointmentId)
            if (result.isSuccess) {
                futureAppointments = futureAppointments.map {
                    if (it.appointmentId == appointmentId) {
                        it.copy(status = Status.CANCELLED)
                    } else {
                        it
                    }
                }
            } else {
                error = "Failed to cancel appointment: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    // Updated filtered lists to handle multiple selections
    val filteredFutureAppointments: List<Appointment>
        get() = if (role == "user") {
            futureAppointments.filter { appointment ->
                (selectedSpecializations.isEmpty() || selectedSpecializations.contains(appointment.type.speciality)) &&
                        (selectedDoctors.isEmpty() || selectedDoctors.contains(appointment.doctorName)) &&
                        (selectedDate == null || appointment.date == selectedDate)
            }
        } else {
            futureAppointments.filter { appointment ->
                (selectedTypes.isEmpty() || selectedTypes.contains(appointment.type)) &&
                        (selectedPatients.isEmpty() || selectedPatients.contains(appointment.patientName)) &&
                        (selectedDate == null || appointment.date == selectedDate)
            }
        }

    val filteredPastAppointments: List<Appointment>
        get() = if (role == "user") {
            pastAppointments.filter { appointment ->
                (selectedSpecializations.isEmpty() || selectedSpecializations.contains(appointment.type.speciality)) &&
                        (selectedDoctors.isEmpty() || selectedDoctors.contains(appointment.doctorName)) &&
                        (selectedDate == null || appointment.date == selectedDate)
            }
        } else {
            pastAppointments.filter { appointment ->
                (selectedTypes.isEmpty() || selectedTypes.contains(appointment.type)) &&
                        (selectedPatients.isEmpty() || selectedPatients.contains(appointment.patientName)) &&
                        (selectedDate == null || appointment.date == selectedDate)
            }
        }

    // Computed properties for filter options
    val allSpecializations: List<String>
        get() = (futureAppointments + pastAppointments).map { it.type.speciality }.distinct()

    val allDoctors: List<String>
        get() = (futureAppointments + pastAppointments).map { it.doctorName }.distinct()

    val allPatients: List<String>
        get() = (futureAppointments + pastAppointments).map { it.patientName }.distinct()

    val allTypes: List<AppointmentType>
        get() = (futureAppointments + pastAppointments).map { it.type }.distinct()
}
