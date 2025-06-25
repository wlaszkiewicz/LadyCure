package com.example.ladycure.presentation.booking


import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.AppointmentRepository
import com.example.ladycure.domain.model.Appointment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class BookingSuccessViewModel(
    private val appointmentRepo: AppointmentRepository = AppointmentRepository()
) : ViewModel() {
    // State variables
    var isLoading by mutableStateOf(true)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        internal set
    var appointment by mutableStateOf<Appointment?>(null)
        private set

    fun loadAppointment(appointmentId: String) {
        viewModelScope.launch {
            try {
                val result = appointmentRepo.getAppointmentById(appointmentId)
                if (result.isSuccess) {
                    appointment = result.getOrNull()
                } else {
                    errorMessage =
                        "Failed to load appointment: ${result.exceptionOrNull()?.message}"
                }
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Error loading appointment: ${e.message}"
                isLoading = false
            }
        }
    }

    fun addToCalendar(context: Context) {
        val appointment = appointment ?: return

        try {
            val intent = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(
                    CalendarContract.Events.TITLE,
                    "Appointment with Dr. ${appointment.doctorName}"
                )
                .putExtra(
                    CalendarContract.Events.DESCRIPTION,
                    "Appointment for ${appointment.type.displayName} at LadyCure Clinic"
                )
                .putExtra(
                    CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                    appointment.dateTime.toDate().time
                )
                .putExtra(
                    CalendarContract.EXTRA_EVENT_END_TIME,
                    appointment.dateTime.toDate().time + appointment.type.durationInMinutes * 60 * 1000
                )
                .putExtra(CalendarContract.Events.EVENT_LOCATION, appointment.address)
                .putExtra(
                    CalendarContract.Events.AVAILABILITY,
                    CalendarContract.Events.AVAILABILITY_BUSY
                )

            context.startActivity(intent)
        } catch (e: Exception) {
            errorMessage = "Failed to open calendar: ${e.message}"
        }
    }

    // Helper properties for formatted date/time
    val formattedDate: String
        get() = appointment?.let {
            SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
                .format(it.dateTime.toDate())
        } ?: "Date unavailable"

    val formattedTime: String
        get() = appointment?.let {
            SimpleDateFormat("h:mm a", Locale.getDefault())
                .format(it.dateTime.toDate())
        } ?: "Time unavailable"
}