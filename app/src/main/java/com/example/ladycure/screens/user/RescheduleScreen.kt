package com.example.ladycure.screens.user

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material3.Text
import com.example.ladycure.data.Appointment
import com.example.ladycure.data.AppointmentType
import com.example.ladycure.data.doctor.Doctor
import com.example.ladycure.data.doctor.DoctorAvailability
import com.example.ladycure.data.doctor.Speciality
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.utility.SnackbarController
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
 fun RescheduleScreen(
    appointmentId: String,
    navController: NavController,
    snackbarController: SnackbarController
) { var appointment by remember { mutableStateOf<Appointment?>(null) }
    val selectedDate = remember { mutableStateOf<LocalDate?>(appointment?.date) }
    val selectedTimeSlot = remember { mutableStateOf<LocalTime?>(appointment?.time) }
    val authRepo = AuthRepository()
    val doctorAvailability = remember { mutableStateOf<List<DoctorAvailability>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val doctor = remember { mutableStateOf<Doctor?>(null) }
    var error by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(error) {
        if (error !="") {
            snackbarController.showMessage(error)
            error = ""
        }
    }

    LaunchedEffect(appointment) {
        val result = authRepo.getAppointmentById(appointmentId)
        if (result.isSuccess) {
            isLoading.value = false
            appointment = result.getOrNull()
            val result = authRepo.getDoctorById(appointment!!.doctorId)
            if (result.isSuccess) {
                doctor.value = result.getOrNull()
                val result = authRepo.getDoctorAvailability(appointment!!.doctorId)
                if (result.isSuccess) {
                    doctorAvailability.value = result.getOrNull()!!
                    isLoading.value = false
                } else {
                    error = result.exceptionOrNull()?.message!!
                }
            } else {
                error = result.exceptionOrNull()?.message!!
            }
        } else {
            error = result.exceptionOrNull()?.message!!
            isLoading.value = false
        }
    }

    if (isLoading.value || appointment == null) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = DefaultPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading appointment data...", color = DefaultOnPrimary)
        }
    } else if (appointment != null &&  doctor.value != null) {
        val availableDates = doctorAvailability.value
            .map { it.date }
            .filter { it != null && (it.isAfter(LocalDate.now()) || it.isEqual(LocalDate.now())) }
            .distinct()
            .sortedBy { it }

        val timeSlotsForSelectedDate = remember(selectedDate.value, doctorAvailability.value) {
            if (selectedDate.value == null) emptyList() else {
                filerTimeSlotsForDate(selectedDate.value!!, doctorAvailability.value)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Reschedule Appointment",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DefaultPrimary
                    )
                )

                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DefaultOnPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

//            // Doctor info
//            AppointmentInfoHeader(
//                appointment = appointment!!,
//                modifier = Modifier.padding(horizontal = 0.dp)
//            )

            Spacer(modifier = Modifier.height(16.dp))

            // Service info chip
            ServiceInfoChip(appointment!!.type, modifier = Modifier.padding(bottom = 16.dp))

            // Date selection
            Text(
                text = "Select New Date",
                style = MaterialTheme.typography.titleMedium,
                color = DefaultOnPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
            )

            // Date selector
            DateSelector(
                availableDates = availableDates.map { it!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) },
                selectedDate = selectedDate.value?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                onDateSelected = {
                    selectedDate.value =
                        LocalDate.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    selectedTimeSlot.value = null
                },
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Time slots
            if (selectedDate.value != null) {
                Text(
                    text = "Available Time Slots",
                    style = MaterialTheme.typography.titleMedium,
                    color = DefaultOnPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (timeSlotsForSelectedDate.isEmpty()) {
                    Text(
                        text = "No available time slots for selected date",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.7f)
                    )
                } else {
                    TimeSlotGrid(
                        timeSlots = timeSlotsForSelectedDate,
                        selectedTimeSlot = selectedTimeSlot.value?.format(
                            DateTimeFormatter.ofPattern("h:mm a", Locale.US)
                        ),
                        onTimeSlotSelected = {
                            selectedTimeSlot.value = LocalTime.parse(
                                it,
                                DateTimeFormatter.ofPattern("h:mm a", Locale.US)
                            )

                            if (selectedDate.value != null && selectedTimeSlot.value != null) {
                                coroutineScope.launch{
                                    val result = authRepo.rescheduleAppointment(appointmentId,
                                        selectedTimeSlot.value!!, selectedDate.value!!)

                                    if (result.isSuccess) {
                                        snackbarController.showMessage("Appointment was rescheduled successfully")
                                        navController.popBackStack()
                                    } else {
                                        error = result.exceptionOrNull()?.message!!
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}



@Composable
private fun DateSelector(
    availableDates: List<String>,
    selectedDate: String?,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier) {
        if (availableDates.isEmpty()) {
            Text(
                text = "We are sorry, there's no other available dates for this appointment",
                style = MaterialTheme.typography.bodyMedium,
                color = DefaultOnPrimary.copy(alpha = 0.9f),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableDates.forEach { date ->
                    DateCard(
                        date = date,
                        isSelected = date == selectedDate,
                        onSelect = { onDateSelected(date) },
                        modifier = Modifier.width(80.dp)
                    )
                }
            }
        }
    }
}


@Composable
private fun AppointmentInfoHeader(
    appointment: Appointment,
    modifier: Modifier = Modifier
) {
    val speciality = Speciality.fromDisplayName(appointment.type.speciality)

    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.5f),
            contentColor = DefaultOnPrimary
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, DefaultPrimary.copy(alpha = 0.3f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Current appointment info
            Text(
                text = "Current Appointment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DefaultPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Service type and price
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DefaultPrimary.copy(alpha = 0.1f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(speciality.icon),
                        contentDescription = appointment.type.displayName,
                        tint = DefaultPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = appointment.type.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DefaultOnPrimary
                    )
                    Text(
                        text = "$${"%.2f".format(appointment.price)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Date and time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Current Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        appointment.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary
                    )
                }

                Column {
                    Text(
                        "Current Time",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        appointment.time.format(DateTimeFormatter.ofPattern("h:mm a", Locale.US)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Doctor info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DefaultPrimary.copy(alpha = 0.1f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Doctor",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Dr. ${appointment.doctorName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DefaultOnPrimary
                    )
                    Text(
                        text = speciality.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = DefaultPrimary
                    )
                }
            }

            // Status chip
            Spacer(modifier = Modifier.height(12.dp))

            val statusColor = when (appointment.status) {
                Appointment.Status.CONFIRMED -> Color(0xFF4CAF50) // Green
                Appointment.Status.PENDING -> Color(0xFFFFC107) // Amber
                else -> Color(0xFFF44336) // Red
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Start)
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = appointment.status.displayName,
                    color = statusColor,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Preview
@Composable
fun AppointmentInfoHeaderPreview(){
    AppointmentInfoHeader(
        appointment = Appointment(
            appointmentId = "124",
            doctorId = "RE2CoEAtEmXbYdhQ7PotN1rFqMk1",
            patientId = "P002",
            date = LocalDate.now(),
            time = LocalTime.now(),
            status = Appointment.Status.PENDING,
            type = AppointmentType.DENTAL_IMPLANT,
            price = 30.0,
            doctorName = "Artur Kot",
            patientName = "John Doe",
            comments = "Make sure to arrive 15 minutes early. Bring your ID.",),
        modifier = Modifier
    )
}