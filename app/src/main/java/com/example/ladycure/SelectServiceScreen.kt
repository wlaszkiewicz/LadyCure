package com.example.ladycure

import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.example.ladycure.data.AppointmentType
import com.example.ladycure.data.doctor.Specialization
import com.example.ladycure.repository.AuthRepository

@Composable
fun SelectServiceScreen(
    navController: NavController,
    doctorId: String?,
    city: String?,
    specialization: Specialization?
) {
    var doctor by remember { mutableStateOf<Map<String, Any>?>(null) }
    var specialization by remember { mutableStateOf<Specialization?>(specialization) }
    val authRepo = AuthRepository()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (doctorId != null) {
        LaunchedEffect(doctorId) {
            val result = authRepo.getDoctorById(doctorId)
            if (result.isSuccess) {
                doctor = result.getOrNull()
                specialization =
                    Specialization.fromDisplayName(doctor?.get("specification") as String)
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }
        }
    }
    if (specialization == null){
        BaseScaffold { snackbarController ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = DefaultPrimary
                )
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.titleLarge,
                    color = DefaultOnPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        // Filter services by specialization
        val services = remember(specialization) {
            AppointmentType.values().filter {
                it.specialization == specialization!!.displayName
            }
        }

        var showReferralDialog by remember { mutableStateOf(false) }
        BaseScaffold { snackbarController ->
            if (errorMessage != null) {
                snackbarController.showSnackbar(
                    message = errorMessage ?: "An error occurred",
                    actionLabel = "OK"
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
                            text = specialization!!.displayName,
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
                        service = services.first { it.needsReferral },
                        onDismiss = { showReferralDialog = false },
                        onUploadReferral = {
                            showReferralDialog = false

                        }
                    )
                }
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
                    modifier = Modifier.weight(1f))

                // Price badge
                Text(
                    text = "$${service.price}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DefaultPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DefaultPrimary.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 4.dp))
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
                    modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${service.durationInMinutes} min",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray)

                if (service.needsReferral) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Referral needed",
                        tint = Color(0xFFFFA000),
                        modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Referral needed",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFFA000))
                }
            }

            // Additional info
            Text(
                text = service.additionalInfo,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp))

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
                        fontWeight = FontWeight.SemiBold)
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
                        color = Color.Gray)
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
    service: AppointmentType,
    onDismiss: () -> Unit,
    onUploadReferral: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Referral Required",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "The ${service.displayName} requires a referral from your primary care physician.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp))

                Text(
                    text = "Please upload your referral document to proceed with booking.",
                    style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = {
            Button(
                onClick = onUploadReferral,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultPrimary,
                    contentColor = Color.White
                )
            ) {
                Text("Upload Referral")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DefaultPrimary)
            }
        },
        modifier = modifier.padding(16.dp),
    )
}

@Preview
@Composable
fun SpecializationServicesScreenPreview() {
    val navController = rememberNavController()
    SelectServiceScreen(
        navController = navController,
        specialization = Specialization.CARDIOLOGY,
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
        onUploadReferral = {}
    )
}