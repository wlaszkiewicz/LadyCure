package com.example.ladycure.presentation.home.components


import BabyBlue
import DefaultOnPrimary
import DefaultPrimary
import Green
import Red
import Yellow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.ladycure.data.Appointment
import com.example.ladycure.data.Appointment.Status
import com.example.ladycure.data.doctor.Speciality
import com.example.ladycure.repository.AppointmentRepository
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.utility.SnackbarController
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
fun AppointmentsSection(
    appointments: List<Appointment>?,
    onAppointmentChanged: (Appointment) -> Unit,
    snackbarController: SnackbarController, navController: NavController
) {
    val futureAppointments = appointments?.filter {
        it.date.isAfter(LocalDate.now()) ||
                (it.date == LocalDate.now() && it.time >= LocalTime.now())
    }?.sortedWith(compareBy({ it.date }, { it.time })) ?: emptyList()

    val pastAppointments =
        appointments?.sortedWith(compareBy({ it.date }, { it.time }))?.reversed() ?: emptyList()
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        )
        {
            Text(
                text = if (futureAppointments.isNotEmpty()) {
                    "Upcoming Appointments"
                } else if (
                    appointments?.isNotEmpty() == true) {
                    "Past Appointments"
                } else {
                    "Appointments"
                },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = DefaultPrimary
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            TextButton(onClick = {
                navController.navigate("appointments")
            }) {
                Text("View All", color = DefaultPrimary)
            }
        }

        if (appointments == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (appointments.isEmpty()) {
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
        } else if (futureAppointments.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                futureAppointments.forEach { appointment ->
                    PatientAppointmentCard(
                        appointment = appointment,
                        onAppointmentChanged = { updatedAppointment ->
                            onAppointmentChanged(updatedAppointment)
                        },
                        snackbarController = snackbarController,
                        navController = navController
                    )
                }
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                pastAppointments.take(5).forEach { appointment ->
                    PatientAppointmentCard(
                        appointment = appointment,
                        onAppointmentChanged = { updatedAppointment ->
                            onAppointmentChanged(updatedAppointment)
                        },
                        snackbarController = snackbarController,
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun PatientAppointmentCard(
    appointment: Appointment,
    onAppointmentChanged: (Appointment) -> Unit,
    snackbarController: SnackbarController,
    navController: NavController
) {
    val statusColor by remember(appointment.status) {
        derivedStateOf {
            when (appointment.status) {
                Status.CONFIRMED -> Green
                Status.PENDING -> Yellow
                else -> Red
            }
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val appointmentRepo = AppointmentRepository()

    val showDetailsDialog = remember { mutableStateOf(false) }
    var showCancelSuccessDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
    ) {
        Card(
            modifier = Modifier.width(280.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            ),
            onClick = {
                showDetailsDialog.value = true
            }
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
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
                            painter = painterResource(Speciality.fromDisplayName(appointment.type.speciality).icon),
                            contentDescription = appointment.type.speciality,
                            tint = DefaultPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Text(
                            text = appointment.type.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "Dr. ${appointment.doctorName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
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

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "Time",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            appointment.time.format(
                                DateTimeFormatter.ofPattern(
                                    "hh:mm a",
                                    Locale.US
                                )
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
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

                    Text(
                        text = "$${appointment.price}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DefaultPrimary
                        )
                    )
                }
            }
        }
    }

    if (showDetailsDialog.value) {
        ShowDetailsDialog(
            appointment = appointment,
            onDismiss = { showDetailsDialog.value = false },
            onReschedule = {
                showDetailsDialog.value = false
                navController.navigate("reschedule/${appointment.appointmentId}")
            },
            onCancel = {
                showDetailsDialog.value = false
                coroutineScope.launch {
                    try {
                        val result = appointmentRepo.cancelAppointment(appointment.appointmentId)
                        if (result.isSuccess) {
                            appointment.status = Status.CANCELLED
                            onAppointmentChanged(appointment)
                            showCancelSuccessDialog = true

                        } else {
                            snackbarController.showMessage("Failed to cancel appointment: ${result.exceptionOrNull()?.message}")
                        }
                    } catch (e: Exception) {
                        snackbarController.showMessage("Error: ${e.message}")
                    }
                }
            }
        )
    }


    if (showCancelSuccessDialog) {
        CancelSuccessDialog(
            onDismiss = { showCancelSuccessDialog = false },
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ShowDetailsDialog(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onReschedule: () -> Unit,
    onCancel: () -> Unit
) {
    val isPreparationExpanded = remember { mutableStateOf(false) }
    val showCancelConfirmation = remember { mutableStateOf(false) }
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
                // Header with doctor info
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
                                    painter = painterResource(Speciality.fromDisplayName(appointment.type.speciality).icon),
                                    contentDescription = "Appointment Type",
                                    tint = DefaultPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = appointment.type.displayName,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                )
                                Text(
                                    text = "Dr. ${appointment.doctorName}",
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
                                color = statusColor
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

                        // Preparation instructions
                        if (appointment.type.preparationInstructions.isNotEmpty()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            isPreparationExpanded.value =
                                                !isPreparationExpanded.value
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Preparation",
                                        tint = DefaultPrimary.copy(alpha = 0.8f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Preparation",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = DefaultOnPrimary.copy(alpha = 0.6f)
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (!isPreparationExpanded.value) {
                                            Text(
                                                text = "Tap to view preparation instructions",
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    color = DefaultPrimary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = if (isPreparationExpanded.value)
                                            Icons.Default.KeyboardArrowUp
                                        else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isPreparationExpanded.value)
                                            "Collapse"
                                        else "Expand",
                                        tint = DefaultPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                AnimatedVisibility(
                                    visible = isPreparationExpanded.value,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    Text(
                                        text = appointment.type.preparationInstructions,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = DefaultOnPrimary
                                        ),
                                        modifier = Modifier.padding(start = 32.dp, top = 8.dp)
                                    )
                                }
                            }
                        }

                        // Comments
                        if (appointment.comments.isNotEmpty()) {
                            AppointmentDetailItem(
                                icon = Icons.AutoMirrored.Filled.Comment,
                                title = "Notes",
                                value = appointment.comments
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons
                    if (appointment.status != Status.CANCELLED) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Cancel button
                            OutlinedButton(
                                onClick = { showCancelConfirmation.value = true },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Red
                                ),
                                modifier = Modifier.padding(6.dp),
                                border = BorderStroke(1.dp, Red.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Cancel",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }

                            // Reschedule button
                            Button(
                                onClick = onReschedule,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DefaultPrimary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.padding(6.dp),
                            ) {
                                Text(
                                    "Reschedule",
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
    if (showCancelConfirmation.value) {
        CancelConfirmationDialog(
            onDismiss = { showCancelConfirmation.value = false },
            onConfirm = {
                onCancel()
                showCancelConfirmation.value = false
            },
            appointment = appointment
        )
    }
}

@Composable
fun CancelConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    appointment: Appointment
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
                modifier = Modifier.padding(24.dp)
            ) {
                // Warning icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Red.copy(alpha = 0.1f))
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Red,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Cancel Appointment?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Red,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Appointment details
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFAFAFA),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Dr. ${appointment.doctorName}",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = appointment.type.displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = appointment.date.format(DateTimeFormatter.ofPattern("EEE, MMM dd")),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = appointment.time.format(DateTimeFormatter.ofPattern("h:mm a")),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Confirmation text
                Text(
                    text = "This action cannot be undone. Are you sure you want to cancel this appointment?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DefaultOnPrimary.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DefaultPrimary
                        ),
                        border = BorderStroke(1.dp, DefaultPrimary)
                    ) {
                        Text(
                            "Go Back",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Button(
                        onClick = {
                            onConfirm()
                        },
                        modifier = Modifier.padding(16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Red,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            "Confirm",
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

@Composable
fun InfoChip(
    text: String,
    color: Color,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = text,
                color = color,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
        }
    }
}

@Composable
fun AppointmentDetailItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = DefaultPrimary.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = DefaultOnPrimary.copy(alpha = 0.6f)
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = DefaultOnPrimary
                )
            )
        }
    }
}


@Composable
fun CancelSuccessDialog(
    onDismiss: () -> Unit
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
                // Animated checkmark icon with circle background
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(DefaultPrimary.copy(alpha = 0.1f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EventBusy,
                        contentDescription = "Success",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Appointment Cancelled!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DefaultPrimary,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Message
                Text(
                    text = "Your appointment has been successfully cancelled. Any payments will be refunded within 7 business days.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DefaultOnPrimary.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

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
                        .padding(horizontal = 24.dp)
                        .height(40.dp)
                ) {
                    Text(
                        "Got it!",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}
