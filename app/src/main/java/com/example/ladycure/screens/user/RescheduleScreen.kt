package com.example.ladycure.screens.user

import BabyBlue
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.wear.compose.material3.Text
import coil.compose.SubcomposeAsyncImage
import com.example.ladycure.data.Appointment
import com.example.ladycure.data.doctor.Doctor
import com.example.ladycure.data.doctor.DoctorAvailability
import com.example.ladycure.data.doctor.Speciality
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.utility.SnackbarController
import kotlinx.coroutines.async
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
) {
    var appointment by remember { mutableStateOf<Appointment?>(null) }
    val selectedDate = remember { mutableStateOf<LocalDate?>(null) }
    val selectedTimeSlot = remember { mutableStateOf<LocalTime?>(null) }
    val authRepo = AuthRepository()
    val doctorAvailability = remember { mutableStateOf<List<DoctorAvailability>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val doctor = remember { mutableStateOf<Doctor?>(null) }
    var error by remember { mutableStateOf("") }
    var showRescheduleSuccessDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var showRescheduleDialog by remember { mutableStateOf(false) }

    LaunchedEffect(error) {
        if (error.isNotEmpty()) {
            snackbarController.showMessage(error)
            error = ""
        }
    }

    LaunchedEffect(appointmentId) {
        isLoading.value = true
        try {
            // Load appointment
            val appointmentResult = authRepo.getAppointmentById(appointmentId)
            if (appointmentResult.isSuccess) {
                appointment = appointmentResult.getOrNull()

                // Load doctor and availability in parallel
                val doctorDeferred = coroutineScope.async {
                    authRepo.getDoctorById(appointment!!.doctorId).getOrNull()
                }
                val availabilityDeferred = coroutineScope.async {
                    authRepo.getDoctorAvailability(appointment!!.doctorId).getOrNull()
                }

                doctor.value = doctorDeferred.await()
                doctorAvailability.value = availabilityDeferred.await() ?: emptyList()
            } else {
                error = appointmentResult.exceptionOrNull()?.message ?: "Failed to load appointment"
            }
        } catch (e: Exception) {
            error = e.message ?: "An error occurred"
        } finally {
            isLoading.value = false
        }
    }

    if (isLoading.value) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = DefaultPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading appointment data...", color = DefaultOnPrimary)
        }
    } else if (appointment == null || doctor.value == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Failed to load appointment data", color = DefaultOnPrimary)
        }
    } else {
        val availableDates = doctorAvailability.value
            .mapNotNull { it.date }
            .filter { it.isAfter(LocalDate.now()) || it.isEqual(LocalDate.now()) }
            .distinct()
            .sorted()

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

            // Doctor info
            AppointmentInfoHeader(
                doctor = doctor.value!!,
                appointment = appointment!!,
                modifier = Modifier.padding(horizontal = 0.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date selection
            if (availableDates.isNotEmpty()) {
                Text(
                    text = "Select New Date",
                    style = MaterialTheme.typography.titleMedium,
                    color = DefaultOnPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
                )

                DateSelector(
                    availableDates = availableDates.map { it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) },
                    selectedDate = selectedDate.value?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    onDateSelected = {
                        selectedDate.value =
                            LocalDate.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        selectedTimeSlot.value = null
                    },
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Time slots - only show if we have a selected date
                selectedDate.value?.let {
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
                            timeSlots = timeSlotsForSelectedDate.map {
                                it.format(
                                    DateTimeFormatter.ofPattern("h:mm a", Locale.US)
                                )
                            },
                            selectedTimeSlot = selectedTimeSlot.value?.format(
                                DateTimeFormatter.ofPattern("h:mm a", Locale.US)
                            ),
                            onTimeSlotSelected = { timeStr ->
                                selectedTimeSlot.value = LocalTime.parse(
                                    timeStr,
                                    DateTimeFormatter.ofPattern("h:mm a", Locale.US)
                                )
                                showRescheduleDialog = true
                            }
                        )
                    }
                }
            } else {
                Text(
                    text = "There are no available dates for rescheduling this appointment",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DefaultOnPrimary.copy(alpha = 0.9f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }

    if (showRescheduleDialog) {
        RescheduleConfirmationDialog(
            oldAppointment = appointment!!,
            newDate = selectedDate.value!!,
            newTime = selectedTimeSlot.value!!,
            onConfirm = {
                if (selectedDate.value != null && selectedTimeSlot.value != null) {
                    coroutineScope.launch {
                        val result = authRepo.rescheduleAppointment(
                            appointmentId,
                            selectedTimeSlot.value!!, selectedDate.value!!
                        )
                        if (result.isSuccess) {
                            showRescheduleDialog = false
                            showRescheduleSuccessDialog = true
                        } else {
                            error = result.exceptionOrNull()?.message!!
                        }
                    }
                }
            },
            onDismiss = { showRescheduleDialog = false }
        )
    }

    if (showRescheduleSuccessDialog) {
        RescheduleSuccessDialog(
            onDismiss = {
                showRescheduleSuccessDialog = false
                navController.popBackStack()
            },
            newDate = selectedDate.value!!.format(DateTimeFormatter.ofPattern("MMM dd")),
            newTime = selectedTimeSlot.value!!.format(
                DateTimeFormatter.ofPattern(
                    "h:mm a",
                    Locale.US
                )
            )
        )
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
    doctor: Doctor,
    appointment: Appointment,
    modifier: Modifier = Modifier
) {
    val speciality = Speciality.fromDisplayName(appointment.type.speciality)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.2f),
            contentColor = DefaultOnPrimary
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DefaultPrimary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // First row - Doctor and service
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Doctor avatar
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(DefaultPrimary.copy(alpha = 0.1f))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (doctor.profilePictureUrl.isNotEmpty()) {
                        SubcomposeAsyncImage(
                            model = doctor.profilePictureUrl,
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            loading = {
                                CircularProgressIndicator(
                                    color = DefaultPrimary,
                                    modifier = Modifier.fillMaxSize(0.5f)
                                )
                            },
                            error = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Doctor",
                                    tint = DefaultPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Doctor",
                            tint = DefaultPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Doctor name and speciality
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Dr. ${appointment.doctorName}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = DefaultOnPrimary
                    )
                    Text(
                        text = speciality.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = DefaultPrimary
                    )
                }

                // Service icon
                Icon(
                    painter = painterResource(speciality.icon),
                    contentDescription = "Service",
                    tint = DefaultPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Second row - Service details and time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Service name and duration
                Text(
                    text = "${appointment.type.displayName} • ${appointment.type.durationInMinutes}min",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    color = DefaultOnPrimary
                )

                // Price
                Text(
                    text = "$${"%.2f".format(appointment.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = DefaultPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Third row - Date and time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Date",
                    tint = DefaultPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = appointment.date.format(DateTimeFormatter.ofPattern("MMM dd")),
                    style = MaterialTheme.typography.labelMedium,
                    color = DefaultOnPrimary
                )

                Spacer(modifier = Modifier.width(12.dp))

                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Time",
                    tint = DefaultPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = appointment.time.format(
                        DateTimeFormatter.ofPattern(
                            "h:mm a",
                            Locale.US
                        )
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = DefaultOnPrimary
                )
            }
        }
    }
}


@Composable
fun RescheduleSuccessDialog(
    onDismiss: () -> Unit,
    newDate: String,
    newTime: String
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // Celebration icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(DefaultPrimary.copy(alpha = 0.1f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = "Rescheduled",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title with emoji
                Text(
                    text = "Rescheduled Successfully!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DefaultPrimary,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // New appointment details card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BabyBlue.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, BabyBlue.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "New Appointment",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = BabyBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = newDate,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = DefaultOnPrimary
                        )
                        Text(
                            text = newTime,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = DefaultOnPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Additional message
                Text(
                    text = "We've sent a confirmation to your email. You can view all your appointments in the 'My Appointments' section.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DefaultOnPrimary.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action button
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DefaultPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        "View Appointments",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun RescheduleConfirmationDialog(
    oldAppointment: Appointment,
    newDate: LocalDate,
    newTime: LocalTime,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Confirm Reschedule",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DefaultPrimary
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Current appointment
                Text(
                    text = "Current Appointment",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = DefaultOnPrimary.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.LightGray.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Date",
                                tint = DefaultPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = oldAppointment.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                                style = MaterialTheme.typography.bodyMedium,
                                color = DefaultOnPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Time",
                                tint = DefaultPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = oldAppointment.time.format(
                                    DateTimeFormatter.ofPattern(
                                        "h:mm a",
                                        Locale.US
                                    )
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = DefaultOnPrimary
                            )
                        }
                    }
                }

                // Arrow icon
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Reschedule to",
                    tint = DefaultPrimary,
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 8.dp)
                )

                // New appointment
                Text(
                    text = "New Appointment",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = DefaultOnPrimary.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BabyBlue.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, BabyBlue.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Date",
                                tint = BabyBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = newDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                                style = MaterialTheme.typography.bodyMedium,
                                color = DefaultOnPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Time",
                                tint = BabyBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = newTime.format(
                                    DateTimeFormatter.ofPattern(
                                        "h:mm a",
                                        Locale.US
                                    )
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = DefaultOnPrimary
                            )
                        }
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = DefaultPrimary
                        ),
                        border = BorderStroke(1.dp, DefaultPrimary.copy(alpha = 0.5f))
                    ) {
                        Text("Cancel", color = DefaultPrimary)
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DefaultPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}