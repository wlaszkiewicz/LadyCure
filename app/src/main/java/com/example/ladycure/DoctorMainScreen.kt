package com.example.ladycure

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.TextField
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import com.example.ladycure.data.Appointment
import com.example.ladycure.data.AppointmentType
import com.example.ladycure.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun DoctorHomeScreen(
    navController: NavHostController,
    authRepo: AuthRepository = AuthRepository()
) {
    val doctorData = remember { mutableStateOf<Map<String, Any>?>(null) }
    var upcomingAppointments = remember { mutableStateOf<List<Appointment>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val showAvailabilityDialog = remember { mutableStateOf(false) }
    var showEditStatusDialog by remember { mutableStateOf(false) }
    val selectedAppointment = remember { mutableStateOf<Appointment?>(null) }

    LaunchedEffect(Unit) {
        val result = authRepo.getCurrentUserData()
        if (result.isSuccess) {
            doctorData.value = result.getOrNull()

            val appointmentsResult = authRepo.getAppointments("doctor")
            if (appointmentsResult.isSuccess) {
                upcomingAppointments.value = appointmentsResult.getOrNull() ?: emptyList()

                upcomingAppointments.value = upcomingAppointments.value.filter {
                    it.date.isAfter(LocalDate.now()) || (it.date == LocalDate.now() && it.time >= LocalTime.now())
                }

                upcomingAppointments.value = upcomingAppointments.value.sortedWith(
                    compareBy({ it.date }, { it.time })
                )

            }
            isLoading.value = false
        } else {
            errorMessage.value = result.exceptionOrNull()?.message
            isLoading.value = false
        }
    }


    if (isLoading.value) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = DefaultPrimary)
        }
    } else {


        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header with greeting
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Welcome Dr. ${doctorData.value?.get("name") ?: ""}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = DefaultPrimary
                    )
                    Text(
                        text = doctorData.value?.get("speciality") as? String ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f)
                    )
                }

                // Doctor avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(DefaultPrimary.copy(alpha = 0.2f))
                        .clickable { navController.navigate("profile") },
                    contentAlignment = Alignment.Center
                ) {
                    val profileUrl = doctorData.value?.get("profilePictureUrl") as? String
                    if (profileUrl != null) {
                        SubcomposeAsyncImage(
                            model = profileUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            loading = {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = DefaultPrimary
                                )
                            },
                            error = {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Profile",
                                    tint = DefaultPrimary
                                )
                            }
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = DefaultPrimary,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Quick Stats Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Today's Appointments
                StatCard(
                    icon = Icons.Default.CalendarToday,
                    value = upcomingAppointments.value.count { it.date == LocalDate.now() && it.time >= LocalTime.now() },
                    label = "Today",
                    modifier = Modifier.weight(1f)
                )

                // Total Patients
                StatCard(
                    icon = Icons.Default.People,
                    value = doctorData.value?.get("patientCount") as? Int ?: 0,
                    label = "Patients",
                    modifier = Modifier.weight(1f)
                )

                val rating = when (val rat = doctorData.value?.get("rating")) {
                    is Int -> rat.toDouble()
                    is Long -> rat.toDouble()
                    is Double -> rat
                    is String -> rat.toDouble()
                    else -> 4.5
                }

                // Rating
                StatCard(
                    icon = Icons.Default.Star,
                    value = rating,
                    label = "Rating",
                    isDecimal = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Upcoming Appointments Section
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Upcoming Appointments",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = DefaultPrimary
                        )
                    )
                    TextButton(onClick = { /* View all */ }) {
                        Text("View All", color = DefaultPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (upcomingAppointments.value.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No upcoming appointments",
                            color = DefaultOnPrimary.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    val appointments = upcomingAppointments.value.take(3)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(appointments) { appointment ->
                            // Card width can be adjusted as needed
                            Box(modifier = Modifier.width(300.dp)) {
                                DoctorAppointmentCard(
                                    appointment = appointment,
                                    onPatientClick = {
                                        //    navController.navigate("patient/${appointment.patientId}")
                                    },
                                    onCommentUpdated = { newComment ->
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val result = authRepo.updateAppointmentComment(
                                                appointment.appointmentId,
                                                newComment
                                            )
                                            if (result.isFailure) {
                                                errorMessage.value =
                                                    result.exceptionOrNull()?.message
                                            }
                                        }
                                    },
                                    onClickStatus = {
                                        selectedAppointment.value = appointment
                                        if (appointment.status == Appointment.Status.PENDING) {
                                            showEditStatusDialog = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DefaultPrimary
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActionButton(
                        icon = Icons.Default.Schedule,
                        label = "Set Availability",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("set_availability") }
                    )

                    ActionButton(
                        icon = Icons.Default.PersonAdd,
                        label = "Add Patient",
                        modifier = Modifier.weight(1f),
                        onClick = { /* Handle add patient */ }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActionButton(
                        icon = Icons.Default.NoteAdd,
                        label = "Add Prescription",
                        modifier = Modifier.weight(1f),
                        onClick = { /* Handle add prescription */ }
                    )

                    ActionButton(
                        icon = Icons.Default.MonetizationOn,
                        label = "View Earnings",
                        modifier = Modifier.weight(1f),
                        onClick = { /* Handle view earnings */ }
                    )
                }
            }

            // Recent Activity/Notifications
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = DefaultPrimary
                        )
                    )
                    TextButton(onClick = { /* View all */ }) {
                        Text("See All", color = DefaultPrimary)
                    }
                }

                // Sample activity items
                ActivityItem(
                    icon = Icons.Default.Notifications,
                    title = "New appointment booked",
                    description = "Patient: Sarah Johnson, Tomorrow at 10:00 AM",
                    time = "2 hours ago"
                )
                ActivityItem(
                    icon = Icons.Default.Payment,
                    title = "Payment received",
                    description = "$120 for consultation with Michael Brown",
                    time = "Yesterday"
                )
            }
        }

        // Availability Dialog
        if (showAvailabilityDialog.value) {
            SetAvailabilityDialog(
                onDismiss = { showAvailabilityDialog.value = false },
                onSave = { dates, times ->
                    // Save availability logic
                    showAvailabilityDialog.value = false
                }
            )
        }

        if (showEditStatusDialog) {
            AlertDialog(
                onDismissRequest = { showEditStatusDialog = false },
                title = { Text("Confirm") },
                text = { Text("Do you want to confirm the appointment?") },
                confirmButton = {
                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                val result = authRepo.updateAppointmentStatus(
                                    appointmentId = selectedAppointment.value!!.appointmentId,
                                    status = Appointment.Status.CONFIRMED.displayName
                                )
                                if (result.isFailure) {
                                    errorMessage.value = result.exceptionOrNull()?.message
                                } else {
                                    // Change the status of the appointment in the list
                                    upcomingAppointments.value = upcomingAppointments.value.map {
                                        if (it.appointmentId == selectedAppointment.value!!.appointmentId) {
                                            it.copy(status = Appointment.Status.CONFIRMED)
                                        } else {
                                            it
                                        }
                                    }
                                }
                            }
                            showEditStatusDialog = false
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditStatusDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: Number,
    label: String,
    isDecimal: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = DefaultPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isDecimal) "%.1f".format(value) else value.toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = DefaultPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = DefaultOnPrimary.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun DoctorAppointmentCard(
    appointment: Appointment,
    onPatientClick: () -> Unit,
    onCommentUpdated: (String) -> Unit = {},
    onClickStatus: () -> Unit = {}
) {
    var showEditComment by remember { mutableStateOf(false) }
    var editedComment by remember { mutableStateOf(appointment.comments) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with appointment info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.type.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = DefaultOnPrimary
                        )
                    )
                    Text(
                        text = "${appointment.date} • ${appointment.time}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.7f)
                    )
                }

                // Status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (appointment.status) {
                                Appointment.Status.CONFIRMED -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                Appointment.Status.PENDING -> Color(0xFFFFC107).copy(alpha = 0.1f)
                                else -> Color(0xFFF44336).copy(alpha = 0.1f)
                            }
                        ).clickable(onClick = onClickStatus)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = appointment.status.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        color = when (appointment.status) {
                            Appointment.Status.CONFIRMED -> Color(0xFF4CAF50)
                            Appointment.Status.PENDING -> Color(0xFFFFC107)
                            else -> Color(0xFFF44336)
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Patient info row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onPatientClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Patient avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DefaultPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Patient",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.patientName ?: "Patient",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$${"%.2f".format(appointment.price)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DefaultPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Comment section
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = DefaultOnPrimary.copy(alpha = 0.8f)
                        )
                    )
                    IconButton(
                        onClick = { showEditComment = !showEditComment },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (showEditComment) Icons.Default.Close else Icons.Default.Edit,
                            contentDescription = if (showEditComment) "Close" else "Edit",
                            tint = DefaultPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (showEditComment) {
                    TextField(
                        value = editedComment,
                        onValueChange = { editedComment = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = DefaultPrimary,
                            unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f)
                        ), textStyle = MaterialTheme.typography.bodyMedium,
                        placeholder = {
                            Text("Add notes about this appointment",
                                color = DefaultOnPrimary.copy(alpha = 0.4f), style = MaterialTheme.typography.bodyMedium)
                        },
                        maxLines = 3,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                showEditComment = false
                                editedComment = appointment.comments
                            }
                        ) {
                            Text("Cancel", color = DefaultPrimary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onCommentUpdated(editedComment)
                                showEditComment = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DefaultPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Save")
                        }
                    }
                } else {
                    Text(
                        text = editedComment.ifEmpty { "No notes added" },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (editedComment.isEmpty())
                            DefaultOnPrimary.copy(alpha = 0.4f)
                        else
                            DefaultOnPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { /* Start consultation */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DefaultPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Message")
                }
                Button(
                    onClick = { /* Reschedule */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = DefaultPrimary
                    ),
                    border = BorderStroke(1.dp, DefaultPrimary)
                ) {
                    Text("Reschedule")
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = DefaultPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = DefaultPrimary
            )
        }
    }
}

@Composable
private fun ActivityItem(
    icon: ImageVector,
    title: String,
    description: String,
    time: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(DefaultPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = DefaultPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = DefaultOnPrimary.copy(alpha = 0.6f)
            )
        }
        Text(
            text = time,
            style = MaterialTheme.typography.labelSmall,
            color = DefaultOnPrimary.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun SetAvailabilityDialog(
    onDismiss: () -> Unit,
    onSave: (List<String>, List<String>) -> Unit
) {
    val selectedDates = remember { mutableStateOf<List<String>>(emptyList()) }
    val selectedTimes = remember { mutableStateOf<List<String>>(emptyList()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Set Your Availability",
                style = MaterialTheme.typography.titleLarge,
                color = DefaultPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Select available dates:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // Date selector would go here

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select available time slots:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // Time slot selector would go here
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedDates.value, selectedTimes.value) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultPrimary,
                    contentColor = Color.White
                )
            ) {
                Text("Save Availability")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DefaultPrimary)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Preview
@Composable
fun DoctorDashboardPreview() {
    DoctorHomeScreen(navController = rememberNavController())
}

@Preview
@Composable
fun DoctorAppointmentPreview(){

    DoctorAppointmentCard(   appointment = Appointment(
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
        comments = "Make sure to arrive 15 minutes early. Bring your ID.",)  ,
        onPatientClick = {})

}