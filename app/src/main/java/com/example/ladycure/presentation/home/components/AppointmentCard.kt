package com.example.ladycure.presentation.home.components


import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import com.example.ladycure.data.Appointment
import com.example.ladycure.data.Status
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import DefaultOnPrimary
import DefaultPrimary
import android.app.Dialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.ladycure.R
import com.example.ladycure.data.AppointmentType
import com.example.ladycure.repository.AuthRepository


@Composable
fun AppointmentsSection(appointments: List<Appointment>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        Text(
            text = "Upcoming Appointments",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DefaultPrimary
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (appointments.isEmpty()) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                appointments.forEach { appointment ->
                    ModernAppointmentCard(appointment)
                }
            }
        }
    }
}

@Composable
fun ModernAppointmentCard(appointment: Appointment) {

    val showDetailsDialog = remember { mutableStateOf(false) }
    val statusColor = when (appointment.status) {
        Status.CONFIRMED -> Color(0xFF4CAF50) // Green
        Status.PENDING -> Color(0xFFFFC107) // Amber
        else -> Color(0xFFF44336) // Red
    }

    Card(
        modifier = Modifier.width(280.dp), // Set a fixed width
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
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
                        painter = when (appointment.type.specialization) {
                            "Cardiology" -> painterResource(id = R.drawable.ic_cardiology)
                            "Dentistry" -> painterResource(id = R.drawable.ic_dentistry)
                            "Dermatology" -> painterResource(id = R.drawable.ic_dermatology)
                            "Gynecology" -> painterResource(id = R.drawable.ic_gynecology)
                            "Endocrinology" -> painterResource(id = R.drawable.ic_endocrinology)
                            "Gastroenterology" -> painterResource(id = R.drawable.ic_gastroenterology)
                            "Neurology" -> painterResource(id = R.drawable.ic_neurology)
                            "Oncology" -> painterResource(id = R.drawable.ic_oncology)
                            "Ophthalmology" -> painterResource(id = R.drawable.ic_ophthalmology)
                            "Orthopedics" -> painterResource(id = R.drawable.ic_orthopedics)
                            "Pediatrics" -> painterResource(id = R.drawable.ic_pediatrics)
                            "Physiotherapy" -> painterResource(id = R.drawable.ic_physiotherapy)
                            "Psychiatry" -> painterResource(id = R.drawable.ic_psychology)
                            "Radiology" -> painterResource(id = R.drawable.ic_radiology)
                            else -> painterResource(id = R.drawable.ic_medical_services)
                        },
                        contentDescription = appointment.type.specialization
                            ?: "Medical Appointment",
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
                    Text("Date", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(appointment.date, style = MaterialTheme.typography.bodyMedium)
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text("Time", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(appointment.time, style = MaterialTheme.typography.bodyMedium)
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
                        text = appointment.status.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
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

    if (showDetailsDialog.value) {
        ShowDetailsDialog(appointment = appointment,
            onDismiss = { showDetailsDialog.value = false }
        )
    }
}

    @Composable
    private fun DetailRow(
        icon: ImageVector,
        title: String,
        value: String,
        valueColor: Color = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = DefaultPrimary.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = valueColor
                )
            }
        }
    }

@Preview
@Composable
fun PreviewAppointmentCard() {
     ModernAppointmentCard(
            appointment = Appointment(
                appointmentId = "1",
                doctorId = "Smith",
                patientId = "Patient123",
                date = "2023-10-01",
                time = "10:00 AM",
                status = Status.CONFIRMED,
                type = AppointmentType.ANXIETY_DEPRESSION_SCREENING,
                price = 150.0,
                address = "123 Main St, City",
                doctorName = "John Doe",
                comments = "Follow-up appointment for anxiety and depression screening."
            )
        )
}

@Preview
@Composable
fun PreviewAppointmentDetailsDialog() {
    ShowDetailsDialog(
        appointment = Appointment(
            appointmentId = "1",
            doctorId = "Smith",
            patientId = "Patient123",
            date = "2023-10-01",
            time = "10:00 AM",
            status = Status.CONFIRMED,
            type = AppointmentType.ANXIETY_DEPRESSION_SCREENING,
            price = 150.0,
            address = "123 Main St, City",
            doctorName = "John Doe",
            comments = "Follow-up appointment for anxiety and depression screening."
        ),
        onDismiss = {}
    )
}


@Composable
fun ShowDetailsDialog(appointment: Appointment, onDismiss: () -> Unit) {
    val showCancelConfirmation = remember { mutableStateOf(false) }
    val statusColor = when (appointment.status) {
        Status.CONFIRMED -> Color(0xFF4CAF50)
        Status.PENDING -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(DefaultPrimary.copy(alpha = 0.1f))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = when (appointment.type.specialization) {
                                "Cardiology" -> painterResource(id = R.drawable.ic_cardiology)
                                "Dentistry" -> painterResource(id = com.example.ladycure.R.drawable.ic_dentistry)
                                "Dermatology" -> painterResource(id = com.example.ladycure.R.drawable.ic_dermatology)
                                "Gynecology" -> painterResource(id = com.example.ladycure.R.drawable.ic_gynecology)
                                "Endocrinology" -> painterResource(id = com.example.ladycure.R.drawable.ic_endocrinology)
                                "Gastroenterology" -> painterResource(id = com.example.ladycure.R.drawable.ic_gastroenterology)
                                "Neurology" -> painterResource(id = com.example.ladycure.R.drawable.ic_neurology)
                                "Oncology" -> painterResource(id = com.example.ladycure.R.drawable.ic_oncology)
                                "Ophthalmology" -> painterResource(id = com.example.ladycure.R.drawable.ic_ophthalmology)
                                "Orthopedics" -> painterResource(id = com.example.ladycure.R.drawable.ic_orthopedics)
                                "Pediatrics" -> painterResource(id = com.example.ladycure.R.drawable.ic_pediatrics)
                                "Physiotherapy" -> painterResource(id = com.example.ladycure.R.drawable.ic_physiotherapy)
                                "Psychiatry" -> painterResource(id = com.example.ladycure.R.drawable.ic_psychology)
                                "Radiology" -> painterResource(id = com.example.ladycure.R.drawable.ic_radiology)
                                else -> painterResource(id = com.example.ladycure.R.drawable.ic_medical_services)
                            },
                            contentDescription = "Appointment Type",
                            tint = DefaultPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Text(
                            text = appointment.type.displayName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            )
                        )
                        Text(
                            text = "Dr. ${appointment.doctorName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Date and Time in one row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date
                    DetailRow(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CalendarToday,
                        title = "Date",
                        value = appointment.date
                    )

                    // Time
                    DetailRow(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Schedule,
                        title = "Time",
                        value = appointment.time
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status and Price in one row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status
                    DetailRow(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Info,
                        title = "Status",
                        value = appointment.status.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        valueColor = statusColor
                    )

                    // Price
                    DetailRow(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.MonetizationOn,
                        title = "Price",
                        value = "$${"%.2f".format(appointment.price)}"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Address (full width)
                DetailRow(
                    icon = Icons.Default.LocationOn,
                    title = "Address",
                    value = appointment.address.ifEmpty { "N/A" }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Comments (full width)
                DetailRow(
                    icon = Icons.Default.Comment,
                    title = "Comments",
                    value = appointment.comments.ifEmpty { "No comments" }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showCancelConfirmation.value = true }, // Changed from onDismiss
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.Red),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Red.copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Text("Cancel", color = Color.Red.copy(alpha = 0.8f))
                    }

                    Button(
                        onClick = { /* Handle reschedule */ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DefaultPrimary.copy(alpha = 0.8f),
                        )
                    ) {
                        Text("Reschedule", color = Color.White)
                    }
                }
            }
        }

        // Confirmation Dialog
        if (showCancelConfirmation.value) {
            AlertDialog(
                onDismissRequest = { showCancelConfirmation.value = false },
                title = {
                    Text(
                        text = "Cancel Appointment",
                        style = MaterialTheme.typography.titleLarge,
                        color = DefaultPrimary
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to cancel this appointment with Dr. ${appointment.doctorName} on ${appointment.date}?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showCancelConfirmation.value = false
                            onDismiss()
                            //cancellation logic here
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red.copy(alpha = 0.5f),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Yes, Cancel")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showCancelConfirmation.value = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DefaultPrimary.copy(alpha = 0.1f),
                            contentColor = DefaultPrimary
                        )
                    ) {
                        Text("No, Keep It")
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White
            )
        }
    }


}

@Composable
private fun DetailRow(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = DefaultPrimary.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = valueColor
            )
        }
    }
}