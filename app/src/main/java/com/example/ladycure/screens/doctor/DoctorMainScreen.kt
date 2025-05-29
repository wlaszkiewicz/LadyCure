package com.example.ladycure.screens.doctor

import BabyBlue
import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import Green
import Red
import Yellow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NoteAdd
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.example.ladycure.data.Appointment
import com.example.ladycure.data.Appointment.Status
import com.example.ladycure.presentation.home.components.AppointmentDetailItem
import com.example.ladycure.presentation.home.components.InfoChip
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.repository.AppointmentRepository
import com.example.ladycure.repository.UserRepository
import com.example.ladycure.utility.SnackbarController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DoctorHomeScreen(
    navController: NavHostController,
    snackbarController: SnackbarController,
    authRepo: AuthRepository = AuthRepository(),
    userRepo: UserRepository = UserRepository(),
    appointmentsRepo: AppointmentRepository = AppointmentRepository()
) {
    val doctorData = remember { mutableStateOf<Map<String, Any>?>(null) }
    var upcomingAppointments = remember { mutableStateOf<List<Appointment>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val allAppointments = remember { mutableStateOf<List<Appointment>>(emptyList()) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    remember { mutableStateOf(false) }
    var showEditStatusDialog by remember { mutableStateOf(false) }
    val selectedAppointment = remember { mutableStateOf<Appointment?>(null) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val result = userRepo.getCurrentUserData()
        if (result.isSuccess) {
            doctorData.value = result.getOrNull()

            val appointmentsResult = appointmentsRepo.getAppointments("doctor")
            if (appointmentsResult.isSuccess) {
                allAppointments.value = appointmentsResult.getOrNull() ?: emptyList()

                upcomingAppointments.value = allAppointments.value.filter {
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
                    color = Color(0xFFFFF0F5),
                    value = upcomingAppointments.value.count { it.date == LocalDate.now() && it.time >= LocalTime.now() },
                    label = "Today",
                    modifier = Modifier.weight(1f)
                )

                //val specializationColors = listOf(
//    Color(0xFFFFF0F5), // Lavender Blush (very light pink)
//    Color(0xFFF0F8FF),
//    Color.White
//)
                listOf(
                    Color(0xFFFFF0F5), // light pink
                    Color(0xFFF0F8FF), // light blue
                    Color(0xFFFAFAD2), // light yellow
                    Color(0xFFE9FFEB), // light green
                    Color(0xFFE2DCFA) // light purple
                )


                // Total Patients
                StatCard(
                    icon = Icons.Default.People,
                    color = Color(0xFFF0F8FF),
                    value = allAppointments.value.distinctBy { it.patientId }.size,
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
                    color = Color(0xFFFAFAD2),
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
                    TextButton(onClick = {
                        navController.navigate("appointments")
                    }) {
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
                                    onClickStatus = {
                                        selectedAppointment.value = appointment
                                        if (appointment.status == Status.PENDING) {
                                            showEditStatusDialog = true
                                        }
                                    },
                                    onCommentUpdated = { newComment ->
                                        coroutineScope.launch {
                                            val result = appointmentsRepo.updateAppointmentComment(
                                                appointment.appointmentId,
                                                newComment
                                            )
                                            if (result.isFailure) {
                                                snackbarController.showMessage(
                                                    result.exceptionOrNull()?.message
                                                        ?: "Failed to update comment"
                                                )
                                            }
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
        }

        if (showEditStatusDialog) {
            ConfirmAppointmentDialog(
                onDismiss = { showEditStatusDialog = false },
                onConfirm = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = appointmentsRepo.updateAppointmentStatus(
                            appointmentId = selectedAppointment.value!!.appointmentId,
                            status = Status.CONFIRMED.displayName
                        )
                        if (result.isFailure) {
                            errorMessage.value = result.exceptionOrNull()?.message
                        } else {
                            // Change the status of the appointment in the list
                            upcomingAppointments.value = upcomingAppointments.value.map {
                                if (it.appointmentId == selectedAppointment.value!!.appointmentId) {
                                    it.copy(status = Status.CONFIRMED)
                                } else {
                                    it
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ConfirmAppointmentDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm") },
        text = { Text("Do you want to confirm the appointment?") },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun StatCard(
    icon: ImageVector,
    color: Color,
    value: Number,
    label: String,
    isDecimal: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
    onClickStatus: () -> Unit = {},
    onCommentUpdated: (String) -> Unit = {}
) {
    var showDetailsDialog by remember { mutableStateOf(false) }

    val statusColor = when (appointment.status) {
        Status.CONFIRMED -> Color(0xFF4CAF50)
        Status.PENDING -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier
            .width(280.dp)
            .padding(vertical = 4.dp)
            .clickable { showDetailsDialog = true },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header with doctor info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DefaultPrimary.copy(alpha = 0.1f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Patient",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column {
                    Text(
                        text = appointment.patientName ?: "Patient",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = appointment.type.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date and time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        appointment.date.format(DateTimeFormatter.ofPattern("MMM dd yyyy")),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Time",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        appointment.time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US)),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer with status and price
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.1f))
                        .clickable(onClick = onClickStatus)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = appointment.status.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Text(
                    text = "$${"%.2f".format(appointment.price)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DefaultPrimary
                    )
                )
            }
        }
    }

    // Details Dialog
    if (showDetailsDialog) {
        DetailsDialog(
            appointment = appointment,
            onDismiss = { showDetailsDialog = false },
            onClickStatus = {
                onClickStatus()
            },
            onMessage = {},
            onCommentUpdated = {
                onCommentUpdated(it)
                appointment.comments = it
            }
        )
    }
}

@Composable
fun DetailsDialog(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onClickStatus: () -> Unit,
    onMessage: () -> Unit,
    onCommentUpdated: (String) -> Unit
) {
    var editedComment by remember { mutableStateOf(appointment.comments) }
    var showEditComment by remember { mutableStateOf(false) }


    remember { mutableStateOf(false) }
    val statusColor = when (appointment.status) {
        Status.CONFIRMED -> Green
        Status.PENDING -> Yellow
        else -> Red
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
            ) {
                // Header with patient info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    DefaultPrimary.copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = 100f
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Doctor avatar
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(DefaultPrimary.copy(alpha = 0.1f))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Patient",
                                    tint = DefaultPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = appointment.patientName,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                )
                                Text(
                                    text = appointment.type.displayName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = DefaultOnPrimary.copy(alpha = 0.7f)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Info chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Status chip
                            InfoChip(
                                text = appointment.status.displayName,
                                color = statusColor,
                                onClick = if (appointment.status == Status.PENDING) {
                                    onClickStatus
                                } else {
                                    {}
                                }
                            )

                            // Duration chip
                            InfoChip(
                                text = "${appointment.type.durationInMinutes} min",
                                color = DefaultPrimary
                            )

                            // Price chip
                            InfoChip(
                                text = "$%.2f".format(appointment.price),
                                color = BabyBlue
                            )
                        }
                    }
                }

                // Appointment details
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    // Date and time section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Date",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DefaultOnPrimary.copy(alpha = 0.6f)
                                )
                            )
                            Text(
                                text = appointment.date.format(DateTimeFormatter.ofPattern("EEE, MMM dd")),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Time",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DefaultOnPrimary.copy(alpha = 0.6f)
                                )
                            )
                            Text(
                                text = appointment.time.format(
                                    DateTimeFormatter.ofPattern(
                                        "h:mm a",
                                        Locale.US
                                    )
                                ),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Details section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Location
                        AppointmentDetailItem(
                            icon = Icons.Default.LocationOn,
                            title = "Location",
                            value = appointment.address.ifEmpty { "Not specified" }
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Comment,
                                        contentDescription = "Notes",
                                        tint = DefaultPrimary.copy(alpha = 0.8f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Notes",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = DefaultOnPrimary.copy(alpha = 0.6f)
                                        )
                                    )
                                }

                                IconButton(
                                    onClick = { showEditComment = !showEditComment },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (showEditComment) Icons.Default.Close else Icons.Default.Edit,
                                        contentDescription = if (showEditComment) "Close" else "Edit",
                                        tint = DefaultPrimary,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = !showEditComment,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Text(
                                    text = appointment.comments.ifEmpty { "No notes added" },
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = DefaultOnPrimary
                                    ),
                                    modifier = Modifier.padding(start = 32.dp, top = 8.dp)
                                )
                            }

                            AnimatedVisibility(
                                visible = showEditComment,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier.padding(start = 32.dp, top = 8.dp)
                                ) {
                                    TextField(
                                        value = editedComment,
                                        onValueChange = { editedComment = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedIndicatorColor = DefaultPrimary,
                                            unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f),
                                            focusedTextColor = DefaultOnPrimary,
                                            unfocusedTextColor = DefaultOnPrimary
                                        ),
                                        textStyle = MaterialTheme.typography.bodyLarge,
                                        placeholder = {
                                            Text(
                                                "Add notes about this appointment",
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    color = DefaultOnPrimary.copy(alpha = 0.5f)
                                                )
                                            )
                                        },
                                        maxLines = 3,
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
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

                                        OutlinedButton(
                                            onClick = {
                                                onCommentUpdated(editedComment)
                                                showEditComment = false
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = Color.White,
                                                contentColor = DefaultPrimary
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Save")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons
                    if (appointment.status != Status.CANCELLED) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White,
                                    contentColor = DefaultPrimary
                                ),
                            ) {
                                Text(
                                    text = "Go back",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = DefaultPrimary
                                )
                            }
                            // Reschedule button
                            Button(
                                onClick = onMessage,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DefaultPrimary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.padding(6.dp)
                            ) {
                                Text(
                                    "Message",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
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

