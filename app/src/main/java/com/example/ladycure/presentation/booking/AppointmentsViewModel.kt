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
import com.example.ladycure.domain.model.AppointmentSummary
import com.example.ladycure.domain.model.AppointmentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AppointmentViewModel(
    private val userRepo: UserRepository = UserRepository(),
    private val appointmentRepo: AppointmentRepository = AppointmentRepository()
) : ViewModel() {

    private var loadedPastMonths = mutableSetOf<String>()
    private val monthsToLoadInitially = 6
    var futureAppointments by mutableStateOf<List<AppointmentSummary>>(emptyList())
        private set
    var pastAppointments by mutableStateOf<List<AppointmentSummary>>(emptyList())
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

    var isLoadingDetails by mutableStateOf(false)
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

                    // Load upcoming appointments
                    val upcomingResult = appointmentRepo.getUpcomingAppointmentsSummaries()
                    if (upcomingResult.isSuccess) {
                        futureAppointments = upcomingResult.getOrNull() ?: emptyList()
                    } else {
                        error = upcomingResult.exceptionOrNull()?.message
                            ?: "Failed to load upcoming appointments"
                    }

                    // Load initial past appointments (last X months)
                    loadInitialPastAppointments()
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

    private suspend fun loadInitialPastAppointments() {
        val currentDate = LocalDate.now()
        val monthsToLoad = (0 until monthsToLoadInitially).map {
            currentDate.minusMonths(it.toLong()).format(DateTimeFormatter.ofPattern("yyyy-MM"))
        }

        val pastAppts = mutableListOf<AppointmentSummary>()
        monthsToLoad.forEach { monthKey ->
            if (!loadedPastMonths.contains(monthKey)) {
                val result = appointmentRepo.getMonthlyAppointmentSummaries(monthKey)
                if (result.isSuccess) {
                    pastAppts.addAll(result.getOrNull() ?: emptyList())
                    loadedPastMonths.add(monthKey)
                }
            }
        }

        pastAppointments = pastAppts.sortedByDescending { it.date }
    }

    fun loadMorePastAppointments() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val oldestLoadedMonth = loadedPastMonths.minByOrNull { it } ?: return@launch
                val monthToLoad = LocalDate.parse("$oldestLoadedMonth-01")
                    .minusMonths(1)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM"))

                val result = appointmentRepo.getMonthlyAppointmentSummaries(monthToLoad)
                if (result.isSuccess) {
                    val newAppointments = result.getOrNull() ?: emptyList()
                    pastAppointments = (pastAppointments + newAppointments)
                        .sortedByDescending { it.date }
                    loadedPastMonths.add(monthToLoad)
                }
            } catch (e: Exception) {
                error = "Failed to load more appointments: ${e.message}"
            }
        }
    }


    internal fun groupAppointmentsByMonth(
        appointments: List<AppointmentSummary>,
        isUpcoming: Boolean = false
    ): Map<String, List<AppointmentSummary>> {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        return appointments.groupBy {
            it.date.format(formatter)
        }.toSortedMap(
            if (isUpcoming) {
                compareBy { LocalDate.parse("01 $it", DateTimeFormatter.ofPattern("dd MMMM yyyy")) }
            } else {
                compareByDescending {
                    LocalDate.parse(
                        "01 $it",
                        DateTimeFormatter.ofPattern("dd MMMM yyyy")
                    )
                }
            }
        )
    }

    fun updateError(message: String?) {
        error = message
    }


    fun toggleEditStatusDialog(show: Boolean) {
        showEditStatusDialog = show
    }

    fun toggleFilters(show: Boolean) {
        showFilters = show
    }

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
//                futureAppointments = futureAppointments.map {
//                    if (it.appointmentId == appointmentId) {
//                        it.copy(comments = newComment)
//                    } else {
//                        it
//                    }
//                }
            }
        }
    }

    fun loadDetailsForAppointment(appointmentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoadingDetails = true
            try {
                val result = appointmentRepo.getAppointmentById(appointmentId)
                if (result.isSuccess) {
                    selectedAppointment = result.getOrNull()
                } else {
                    error =
                        result.exceptionOrNull()?.message ?: "Failed to load appointment details"
                }
            } finally {
                isLoadingDetails = false
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

    val filteredFutureAppointments: List<AppointmentSummary>
        get() = if (role == "user") {
            futureAppointments.filter { appointment ->
                (selectedSpecializations.isEmpty() || selectedSpecializations.contains(appointment.enumType.speciality)) &&
                        (selectedDoctors.isEmpty() || selectedDoctors.contains(appointment.doctorName)) &&
                        (selectedDate == null || appointment.date == selectedDate)
            }
        } else {
            futureAppointments.filter { appointment ->
                (selectedTypes.isEmpty() || selectedTypes.contains(appointment.enumType)) &&
                        (selectedPatients.isEmpty() || selectedPatients.contains(appointment.patientName)) &&
                        (selectedDate == null || appointment.date == selectedDate)
            }
        }

    val filteredPastAppointments: List<AppointmentSummary>
        get() = if (role == "user") {
            pastAppointments.filter { appointment ->
                (selectedSpecializations.isEmpty() || selectedSpecializations.contains(appointment.enumType.speciality)) &&
                        (selectedDoctors.isEmpty() || selectedDoctors.contains(appointment.doctorName)) &&
                        (selectedDate == null || appointment.date == selectedDate)
            }
        } else {
            pastAppointments.filter { appointment ->
                (selectedTypes.isEmpty() || selectedTypes.contains(appointment.enumType)) &&
                        (selectedPatients.isEmpty() || selectedPatients.contains(appointment.patientName)) &&
                        (selectedDate == null || appointment.date == selectedDate)
            }
        }

    // Computed properties for filter options
    val allSpecializations: List<String>
        get() = (futureAppointments + pastAppointments).map { it.enumType.speciality }.distinct()

    val allDoctors: List<String>
        get() = (futureAppointments + pastAppointments).map { it.doctorName }.distinct()

    val allPatients: List<String>
        get() = (futureAppointments + pastAppointments).map { it.patientName }.distinct()

    val allTypes: List<AppointmentType>
        get() = (futureAppointments + pastAppointments).map { it.enumType }.distinct()
}
