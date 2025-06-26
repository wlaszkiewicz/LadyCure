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

/**
 * ViewModel for managing appointments, including fetching, filtering, and updating appointment status and comments.
 *
 * @param userRepo The repository for user-related operations.
 * @param appointmentRepo The repository for appointment-related operations.
 */
class AppointmentViewModel(
    private val userRepo: UserRepository = UserRepository(),
    private val appointmentRepo: AppointmentRepository = AppointmentRepository()
) : ViewModel() {

    /**
     * List of future appointments.
     */
    var futureAppointments by mutableStateOf<List<Appointment>>(emptyList())
        private set

    /**
     * List of past appointments.
     */
    var pastAppointments by mutableStateOf<List<Appointment>>(emptyList())
        private set

    /**
     * Indicates if appointments are currently being loaded.
     */
    var isLoading by mutableStateOf(true)
        private set

    /**
     * Stores any error message that occurs during appointment operations.
     */
    var error by mutableStateOf<String?>(null)
        private set

    /**
     * The currently selected appointment.
     */
    var selectedAppointment by mutableStateOf<Appointment?>(null)
        private set

    /**
     * Controls the visibility of the edit status dialog.
     */
    var showEditStatusDialog by mutableStateOf(false)
        private set

    /**
     * Controls the visibility of the filters.
     */
    var showFilters by mutableStateOf(false)
        private set

    /**
     * List of selected specializations for filtering.
     */
    var selectedSpecializations by mutableStateOf<List<String>>(emptyList())
        private set

    /**
     * List of selected doctors for filtering.
     */
    var selectedDoctors by mutableStateOf<List<String>>(emptyList())
        private set

    /**
     * The selected date for filtering.
     */
    var selectedDate by mutableStateOf<LocalDate?>(null)
        private set

    /**
     * List of selected appointment types for filtering.
     */
    var selectedTypes by mutableStateOf<List<AppointmentType>>(emptyList())
        private set

    /**
     * List of selected patients for filtering.
     */
    var selectedPatients by mutableStateOf<List<String>>(emptyList())
        private set

    /**
     * The role of the current user.
     */
    var role by mutableStateOf<String?>(null)
        private set

    init {
        loadAppointments()
    }

    /**
     * Loads appointments based on the user's role.
     */
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

    /**
     * Updates the future and past appointment lists from a given list of all appointments.
     *
     * @param allAppointments The list of all appointments.
     */
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

    /**
     * Groups appointments by month.
     *
     * @param appointments The list of appointments to group.
     * @return A map where keys are month strings and values are lists of appointments for that month.
     */
    internal fun groupAppointmentsByMonth(appointments: List<Appointment>): Map<String, List<Appointment>> {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        return appointments.groupBy {
            it.date.format(formatter)
        }.toSortedMap(compareByDescending {
            LocalDate.parse("01 $it", DateTimeFormatter.ofPattern("dd MMMM yyyy"))
        })
    }

    /**
     * Updates the current error message.
     *
     * @param message The error message to set.
     */
    fun updateError(message: String?) {
        error = message
    }

    /**
     * Sets the currently selected appointment.
     *
     * @param appointment The appointment to select.
     */
    fun selectAppointment(appointment: Appointment) {
        selectedAppointment = appointment
    }

    /**
     * Toggles the visibility of the edit status dialog.
     *
     * @param show True to show the dialog, false to hide it.
     */
    fun toggleEditStatusDialog(show: Boolean) {
        showEditStatusDialog = show
    }

    /**
     * Toggles the visibility of the filters.
     *
     * @param show True to show the filters, false to hide them.
     */
    fun toggleFilters(show: Boolean) {
        showFilters = show
    }

    /**
     * Toggles a specialization in the filter selection.
     *
     * @param specialization The specialization to toggle.
     */
    fun toggleSpecializationFilter(specialization: String) {
        selectedSpecializations = if (selectedSpecializations.contains(specialization)) {
            selectedSpecializations - specialization
        } else {
            selectedSpecializations + specialization
        }
    }

    /**
     * Toggles a doctor in the filter selection.
     *
     * @param doctor The doctor to toggle.
     */
    fun toggleDoctorFilter(doctor: String) {
        selectedDoctors = if (selectedDoctors.contains(doctor)) {
            selectedDoctors - doctor
        } else {
            selectedDoctors + doctor
        }
    }

    /**
     * Sets the selected date for filtering.
     *
     * @param date The date to set.
     */
    fun setDateFilter(date: LocalDate?) {
        selectedDate = date
    }

    /**
     * Toggles an appointment type in the filter selection.
     *
     * @param type The appointment type to toggle.
     */
    fun toggleTypeFilter(type: AppointmentType) {
        selectedTypes = if (selectedTypes.contains(type)) {
            selectedTypes - type
        } else {
            selectedTypes + type
        }
    }

    /**
     * Toggles a patient in the filter selection.
     *
     * @param patient The patient to toggle.
     */
    fun togglePatientFilter(patient: String) {
        selectedPatients = if (selectedPatients.contains(patient)) {
            selectedPatients - patient
        } else {
            selectedPatients + patient
        }
    }

    /**
     * Clears all active filters.
     */
    fun clearAllFilters() {
        selectedSpecializations = emptyList()
        selectedDoctors = emptyList()
        selectedDate = null
        selectedTypes = emptyList()
        selectedPatients = emptyList()
    }

    /**
     * Updates the status of the currently selected appointment.
     *
     * @param status The new status to set.
     */
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

    /**
     * Updates the comment for a specific appointment.
     *
     * @param appointmentId The ID of the appointment to update.
     * @param newComment The new comment to set.
     */
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

    /**
     * Cancels a specific appointment.
     *
     * @param appointmentId The ID of the appointment to cancel.
     */
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

    /**
     * Returns a filtered list of future appointments based on selected filters and user role.
     */
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

    /**
     * Returns a filtered list of past appointments based on selected filters and user role.
     */
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

    /**
     * Returns a distinct list of all specializations from both future and past appointments.
     */
    val allSpecializations: List<String>
        get() = (futureAppointments + pastAppointments).map { it.type.speciality }.distinct()

    /**
     * Returns a distinct list of all doctors from both future and past appointments.
     */
    val allDoctors: List<String>
        get() = (futureAppointments + pastAppointments).map { it.doctorName }.distinct()

    /**
     * Returns a distinct list of all patients from both future and past appointments.
     */
    val allPatients: List<String>
        get() = (futureAppointments + pastAppointments).map { it.patientName }.distinct()

    /**
     * Returns a distinct list of all appointment types from both future and past appointments.
     */
    val allTypes: List<AppointmentType>
        get() = (futureAppointments + pastAppointments).map { it.type }.distinct()
}