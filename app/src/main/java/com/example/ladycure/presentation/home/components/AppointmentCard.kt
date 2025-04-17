package com.example.ladycure.presentation.home.components

import androidx.compose.runtime.Composable
import com.example.ladycure.data.Appointment
import com.example.ladycure.data.Status
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.ladycure.data.AppointmentType


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
    val statusColor = when (appointment.status) {
        Status.CONFIRMED -> Color(0xFF4CAF50) // Green
        Status.PENDING -> Color(0xFFFFC107) // Amber
        else -> Color(0xFFF44336) // Red
    }

    Card(
        modifier = Modifier.width(280.dp), // Set a fixed width
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
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
                            "Cardiology" -> painterResource(id = com.example.ladycure.R.drawable.ic_cardiology)
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
                        text = "Dr. ${appointment.doctorId}",
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
}

@Preview
@Composable
fun PreviewAppointmentCard() {
     ModernAppointmentCard(
            appointment = Appointment(
                appointmentId = "1",
                doctorId = "Dr. Smith",
                patientId = "Patient123",
                date = "2023-10-01",
                time = "10:00 AM",
                status = Status.CONFIRMED,
                type = AppointmentType.ANXIETY_DEPRESSION_SCREENING,
                price = 150.0
            )
        )
}