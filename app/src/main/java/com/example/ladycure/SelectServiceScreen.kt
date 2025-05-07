package com.example.ladycure

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.ladycure.data.AppointmentType
import com.example.ladycure.data.doctor.Doctor
import com.example.ladycure.data.doctor.Speciality
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.utility.SnackbarController

@Composable
fun SelectServiceScreen(
    navController: NavController,
    snackbarController: SnackbarController?,
    doctorId: String?,
    city: String?,
    speciality: Speciality?
) {
    var doctor by remember { mutableStateOf<Doctor?>(null) }
    var speciality by remember { mutableStateOf<Speciality?>(speciality) }
    val authRepo = AuthRepository()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedService by remember { mutableStateOf<AppointmentType?>(null) }

    if (doctorId != null) {
        LaunchedEffect(doctorId) {
            val result = authRepo.getDoctorById(doctorId)
            if (result.isSuccess) {
                doctor = result.getOrNull()
                speciality = doctor?.speciality
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }
        }
    }
    if (speciality == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = DefaultPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading services...", color = DefaultOnPrimary)
        }
    } else {
        // Filter services by specialization
        val services = remember(speciality) {
            AppointmentType.values().filter {
                it.speciality == speciality!!.displayName
            }
        }

        var showReferralDialog by remember { mutableStateOf(false) }

        if (errorMessage != null) {
            snackbarController?.showMessage(
                message = errorMessage ?: "An error occurred"
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground)
        ) {
            // Header with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back",
                        tint = DefaultOnPrimary,
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = speciality!!.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = DefaultOnPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            // Introduction text
            Text(
                text = "Available Services",
                style = MaterialTheme.typography.titleMedium,
                color = DefaultOnPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Text(
                text = "Select a service to book an appointment",
                style = MaterialTheme.typography.bodyMedium,
                color = DefaultOnPrimary.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Services list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(services) { service ->
                    ServiceCard(
                        service = service,
                        onClick = {
                            selectedService = service
                            if (service.needsReferral) {
                                showReferralDialog = true
                            } else if (city != null && doctorId == null) {
                                navController.navigate("book_appointment/$city/${service.displayName}")
                            } else {
                                navController.navigate("book_appointment_dir/${doctorId}/${service.displayName}")
                            }
                        }
                    )
                }
            }
        }

        if (showReferralDialog) {
            ReferralRequiredDialog(
                service = selectedService,
                onDismiss = { showReferralDialog = false },
                onUploadReferral = {
                    showReferralDialog = false
                    // Handle referral upload
//                            uploadReferral(
//                                navController = navController,
//                                doctorId = doctorId,
//                                service = selectedService
//                            )

                },
                onBringLater = {
                    showReferralDialog = false
                    if (city != null && doctorId == null) {
                        navController.navigate("book_appointment/$city/${selectedService!!.displayName}")
                    } else {
                        navController.navigate("book_appointment_dir/${doctorId}/${selectedService!!.displayName}")
                    }
                },
            )
        }
    }
}

@Composable
fun ServiceCard(
    service: AppointmentType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = service.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                // Price badge
                Text(
                    text = "$${service.price}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DefaultPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DefaultPrimary.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Duration and referral info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Duration",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${service.durationInMinutes} min",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )

                if (service.needsReferral) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Referral needed",
                        tint = Color(0xFFFFA000),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Referral needed",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFFA000)
                    )
                }
            }

            // Additional info
            Text(
                text = service.additionalInfo,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Preparation instructions (collapsible)
            var showPreparation by remember { mutableStateOf(false) }
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Preparation instructions",
                        style = MaterialTheme.typography.labelMedium,
                        color = DefaultPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { showPreparation = !showPreparation }) {
                        Icon(
                            imageVector = if (showPreparation) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (showPreparation) "Hide" else "Show",
                            tint = DefaultPrimary
                        )
                    }
                }

                if (showPreparation) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = service.preparationInstructions,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Book button
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultPrimary.copy(alpha = 0.9f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Book This Service")
            }
        }
    }
}

@Composable
fun ReferralRequiredDialog(
    service: AppointmentType?,
    onDismiss: () -> Unit,
    onUploadReferral: () -> Unit,
    onBringLater: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalInformation,
                        contentDescription = null,
                        tint = DefaultPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Referral Required",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DefaultPrimary
                    )
                }

                // Content
                Column(
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "The ${service?.displayName ?: "selected service"} requires a referral from your primary care physician.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Please upload your referral document to proceed with booking.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // Action buttons
                Column {
                    Button(
                        onClick = onUploadReferral,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DefaultPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Upload Referral Now")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onBringLater,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DefaultPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DefaultPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("I'll Bring It In Person")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Cancel", color = DefaultOnPrimary.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SpecializationServicesScreenPreview() {
    val navController = rememberNavController()
    SelectServiceScreen(
        navController = navController,
        null,
        speciality = Speciality.CARDIOLOGY,
        doctorId = "doctorId",
        city = "Wroclaw"
    )
}

@Preview
@Composable
fun ServiceCardPreview() {
    ServiceCard(
        service = AppointmentType.ECHOCARDIOGRAM,
        onClick = {}
    )
}

@Preview
@Composable
fun ReferralRequiredDialogPreview() {
    ReferralRequiredDialog(
        service = AppointmentType.ECHOCARDIOGRAM,
        onDismiss = {},
        onUploadReferral = {},
        onBringLater = {}
    )
}