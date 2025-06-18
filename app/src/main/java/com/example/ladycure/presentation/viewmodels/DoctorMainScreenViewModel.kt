package com.example.ladycure.presentation.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.Appointment
import com.example.ladycure.data.Appointment.Status
import com.example.ladycure.repository.AppointmentRepository
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class DoctorHomeViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val userRepo: UserRepository = UserRepository(),
    private val appointmentsRepo: AppointmentRepository = AppointmentRepository()
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(DoctorHomeUiState())
    val uiState: StateFlow<DoctorHomeUiState> = _uiState.asStateFlow()

    // Dialog states
    private val _selectedAppointment = mutableStateOf<Appointment?>(null)
    val selectedAppointment: State<Appointment?> = _selectedAppointment

    private val _showEditStatusDialog = mutableStateOf(false)
    val showEditStatusDialog: State<Boolean> = _showEditStatusDialog

    private val _showDetailsDialog = mutableStateOf(false)
    val showDetailsDialog: State<Boolean> = _showDetailsDialog

    private val _nearestAppointment = mutableStateOf<Appointment?>(null)
    val nearestAppointment: State<Appointment?> = _nearestAppointment

    init {
        loadData()
        startTimeUpdates()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val doctorData = userRepo.getCurrentUserData().getOrNull()
                val allAppointments = appointmentsRepo.getAppointments("doctor").getOrNull() ?: emptyList()

                val upcomingAppointments = allAppointments.filter {
                    it.date.isAfter(LocalDate.now()) ||
                            (it.date == LocalDate.now() && it.time >= LocalTime.now())
                }.sortedWith(compareBy({ it.date }, { it.time }))

                _nearestAppointment.value = findNearestAppointment(upcomingAppointments)

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

    private fun findNearestAppointment(appointments: List<Appointment>): Appointment? {
        if (appointments.isEmpty()) return null

        val now = LocalDate.now()
        val currentTime = LocalTime.now()

        // First try to find today's next appointment
        val todaysNext = appointments.firstOrNull {
            it.date == now && it.time.isAfter(currentTime)
        }
        if (todaysNext != null) return todaysNext

        // If none today, find the earliest future appointment
        return appointments.firstOrNull { it.date.isAfter(now) } ?: appointments.firstOrNull()
    }

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

    fun confirmAppointment() {
        selectedAppointment.value?.let { appointment ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    appointmentsRepo.updateAppointmentStatus(
                        appointmentId = appointment.appointmentId,
                        status = Status.CONFIRMED.displayName
                    )

                    // Update local state
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

                    // Update selected appointment
                    _selectedAppointment.value = _selectedAppointment.value?.copy(status = Status.CONFIRMED)
                } catch (e: Exception) {
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
            }
        }
        _showEditStatusDialog.value = false
    }

    fun updateAppointmentComment(newComment: String) {
        selectedAppointment.value?.let { appointment ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    appointmentsRepo.updateAppointmentComment(
                        appointmentId = appointment.appointmentId,
                        newComment
                    )

                    // Update local state
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
                            }
                        )
                    }

                    // Update selected appointment
                    _selectedAppointment.value = _selectedAppointment.value?.copy(comments = newComment)
                } catch (e: Exception) {
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
            }
        }
    }

    fun selectAppointment(appointment: Appointment?) {
        _selectedAppointment.value = appointment
    }

    fun setShowEditStatusDialog(show: Boolean) {
        _showEditStatusDialog.value = show
    }

    fun setShowDetailsDialog(show: Boolean) {
        _showDetailsDialog.value = show
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

data class DoctorHomeUiState(
    val doctorData: Map<String, Any>? = null,
    val allAppointments: List<Appointment> = emptyList(),
    val upcomingAppointments: List<Appointment> = emptyList(),
    val currentTime: LocalTime = LocalTime.now(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)