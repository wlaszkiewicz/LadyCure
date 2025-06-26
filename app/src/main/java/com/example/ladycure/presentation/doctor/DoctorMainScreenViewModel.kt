package com.example.ladycure.presentation.doctor

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.AppointmentRepository
import com.example.ladycure.data.repository.AuthRepository
import com.example.ladycure.data.repository.NotificationRepository
import com.example.ladycure.data.repository.UserRepository
import com.example.ladycure.domain.model.Appointment
import com.example.ladycure.domain.model.Appointment.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

/**
 * ViewModel for the Doctor Home screen, handling business logic and UI state for doctor-related functionalities.
 *
 * @param authRepo The repository for authentication-related operations.
 * @param userRepo The repository for user-related data operations.
 * @param appointmentsRepo The repository for appointment-related operations.
 * @param notificationRepo The repository for notification-related operations.
 */
class DoctorHomeViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val userRepo: UserRepository = UserRepository(),
    private val appointmentsRepo: AppointmentRepository = AppointmentRepository(),
    private val notificationRepo: NotificationRepository = NotificationRepository()
) : ViewModel() {

    /**
     * The mutable StateFlow that holds the current UI state of the Doctor Home screen.
     */
    private val _uiState = MutableStateFlow(DoctorHomeUiState())

    /**
     * The public immutable StateFlow exposing the UI state.
     */
    val uiState: StateFlow<DoctorHomeUiState> = _uiState.asStateFlow()

    /**
     * The mutable state for the currently selected appointment, used for dialogs.
     */
    private val _selectedAppointment = mutableStateOf<Appointment?>(null)

    /**
     * The public immutable state for the currently selected appointment.
     */
    val selectedAppointment: State<Appointment?> = _selectedAppointment

    /**
     * The mutable state to control the visibility of the edit status dialog.
     */
    private val _showEditStatusDialog = mutableStateOf(false)

    /**
     * The public immutable state for the visibility of the edit status dialog.
     */
    val showEditStatusDialog: State<Boolean> = _showEditStatusDialog

    /**
     * The mutable state to control the visibility of the details dialog.
     */
    private val _showDetailsDialog = mutableStateOf(false)

    /**
     * The public immutable state for the visibility of the details dialog.
     */
    val showDetailsDialog: State<Boolean> = _showDetailsDialog

    /**
     * StateFlow representing the nearest upcoming appointment.
     */
    val nearestAppointment: StateFlow<Appointment?> = uiState.map { state ->
        findNearestAppointment(state.upcomingAppointments)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    init {
        loadData()
        startTimeUpdates()
    }

    /**
     * Loads initial data for the Doctor Home screen, including doctor data, appointments, and unread notifications count.
     */
    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val doctorData = userRepo.getCurrentUserData().getOrNull()
                val allAppointments =
                    (appointmentsRepo.getAppointments("doctor").getOrNull() ?: emptyList()).filter {
                        it.status != Status.CANCELLED
                    }
                val upcomingAppointments = allAppointments.filter {
                    it.date.isAfter(LocalDate.now()) ||
                            (it.date == LocalDate.now() && it.time >= LocalTime.now())
                }.sortedWith(compareBy({ it.date }, { it.time }))

                notificationRepo.getUnreadNotificationsCount(
                    onResult = { count ->
                        _uiState.update { it.copy(unreadNotificationsCount = count) }
                    },
                    onError = { error ->
                        _uiState.update { it.copy(errorMessage = error) }
                    }
                )

                _uiState.update {
                    it.copy(
                        doctorData = doctorData,
                        allAppointments = allAppointments,
                        upcomingAppointments = upcomingAppointments,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    /**
     * Finds the nearest upcoming appointment from a list of appointments.
     * @param appointments The list of appointments to search through.
     * @return The nearest Appointment, or null if no upcoming appointments are found.
     */
    private fun findNearestAppointment(appointments: List<Appointment>): Appointment? {
        if (appointments.isEmpty()) return null
        val now = LocalDate.now()
        val currentTime = LocalTime.now()
        val todaysNext = appointments.firstOrNull {
            it.date == now && it.time.isAfter(currentTime) && it.status != Status.CANCELLED
        }
        if (todaysNext != null) return todaysNext
        return appointments.firstOrNull { it.date.isAfter(now) && it.status != Status.CANCELLED }
            ?: appointments.firstOrNull()
    }

    /**
     * Starts time updates to keep the current time in the UI state synchronized.
     */
    private fun startTimeUpdates() {
        viewModelScope.launch {
            val now = LocalTime.now()
            val initialDelay = (60_000 - (now.second * 1000 + now.nano / 1_000_000)).toLong()
            delay(initialDelay)
            while (true) {
                _uiState.update { it.copy(currentTime = LocalTime.now()) }
                delay(60_000)
            }
        }
    }

    /**
     * Confirms the currently selected appointment, updating its status in the repository and local state.
     */
    fun confirmAppointment() {
        selectedAppointment.value?.let { appointment ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    appointmentsRepo.updateAppointmentStatus(
                        appointmentId = appointment.appointmentId,
                        status = Status.CONFIRMED.displayName
                    )
                    _uiState.update { current ->
                        current.copy(
                            allAppointments = current.allAppointments.map {
                                if (it.appointmentId == appointment.appointmentId) {
                                    it.copy(status = Status.CONFIRMED)
                                } else it
                            },
                            upcomingAppointments = current.upcomingAppointments.map {
                                if (it.appointmentId == appointment.appointmentId) {
                                    it.copy(status = Status.CONFIRMED)
                                } else it
                            }
                        )
                    }
                    _selectedAppointment.value =
                        _selectedAppointment.value?.copy(status = Status.CONFIRMED)
                } catch (e: Exception) {
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
            }
        }
        _showEditStatusDialog.value = false
    }

    /**
     * Updates the comment for the currently selected appointment.
     * @param newComment The new comment string.
     */
    fun updateAppointmentComment(newComment: String) {
        selectedAppointment.value?.let { appointment ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    appointmentsRepo.updateAppointmentComment(
                        appointmentId = appointment.appointmentId,
                        newComment
                    )
                    _uiState.update { current ->
                        current.copy(
                            allAppointments = current.allAppointments.map {
                                if (it.appointmentId == appointment.appointmentId) {
                                    it.copy(comments = newComment)
                                } else it
                            },
                            upcomingAppointments = current.upcomingAppointments.map {
                                if (it.appointmentId == appointment.appointmentId) {
                                    it.copy(comments = newComment)
                                } else it
                            },
                        )
                    }
                    _selectedAppointment.value =
                        _selectedAppointment.value?.copy(comments = newComment)
                } catch (e: Exception) {
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
            }
        }
    }

    /**
     * Sets the currently selected appointment.
     * @param appointment The appointment to select, or null to clear selection.
     */
    fun selectAppointment(appointment: Appointment?) {
        _selectedAppointment.value = appointment
    }

    /**
     * Sets the visibility of the edit status dialog.
     * @param show Boolean indicating whether to show or hide the dialog.
     */
    fun setShowEditStatusDialog(show: Boolean) {
        _showEditStatusDialog.value = show
    }

    /**
     * Sets the visibility of the appointment details dialog.
     * @param show Boolean indicating whether to show or hide the dialog.
     */
    fun setShowDetailsDialog(show: Boolean) {
        _showDetailsDialog.value = show
    }

    /**
     * Clears any current error message in the UI state.
     */
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

/**
 * Data class representing the UI state for the Doctor Home screen.
 *
 * @property doctorData Map of doctor's personal data.
 * @property allAppointments List of all appointments, regardless of status.
 * @property upcomingAppointments List of upcoming appointments.
 * @property unreadNotificationsCount Number of unread notifications.
 * @property currentTime The current local time, updated periodically.
 * @property isLoading Boolean indicating if data is currently being loaded.
 * @property errorMessage A string containing an error message if an error occurred, otherwise null.
 */
data class DoctorHomeUiState(
    val doctorData: Map<String, Any>? = null,
    val allAppointments: List<Appointment> = emptyList(),
    val upcomingAppointments: List<Appointment> = emptyList(),
    val unreadNotificationsCount: Int = 0,
    val currentTime: LocalTime = LocalTime.now(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)