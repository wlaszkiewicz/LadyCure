package com.example.ladycure.presentation.booking

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import Green
import Yellow
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileCopy
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.ladycure.R
import com.example.ladycure.data.repository.DoctorRepository
import com.example.ladycure.data.repository.StorageRepository
import com.example.ladycure.domain.model.AppointmentType
import com.example.ladycure.domain.model.Doctor
import com.example.ladycure.domain.model.Speciality
import com.example.ladycure.utility.PdfUploader
import com.example.ladycure.utility.SnackbarController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val referralRepo = StorageRepository()
    val doctorRepo = DoctorRepository()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedService by remember { mutableStateOf<AppointmentType?>(null) }

    var isUploading by remember { mutableStateOf(false) }
    var showUploadSuccessDialog by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var referralId by remember { mutableStateOf<String?>(null) }
    var tooLarge by remember { mutableStateOf(false) }
    var context = LocalContext.current

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    if (PdfUploader.isFileTooLarge(context, uri)) {
                        tooLarge = true
                        return@launch
                    }

                    try {
                        withContext(Dispatchers.Main) {
                            isUploading = true
                            uploadProgress = 0f
                        }

                        val result = referralRepo.uploadReferralToFirestore(
                            uri,
                            selectedService
                        ) { progress ->
                            uploadProgress = progress.progress
                        }

                        withContext(Dispatchers.Main) {
                            isUploading = false
                            if (result.isSuccess) {
                                referralId = result.getOrNull()
                                showUploadSuccessDialog = true
                            } else {
                                snackbarController?.showMessage(
                                    message = result.exceptionOrNull()?.message
                                        ?: "Could not upload PDF"
                                )
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            isUploading = false
                            snackbarController?.showMessage(
                                message = e.message ?: "Upload failed"
                            )
                        }
                    }
                }
            }
        }
    )


    if (doctorId != null) {
        LaunchedEffect(doctorId) {
            val result = doctorRepo.getDoctorById(doctorId)
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
        val services = remember(speciality) {
            AppointmentType.entries.filter {
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

        if (tooLarge) {
            FileTooLargeDialog(
                onDismiss = { tooLarge = false },
            )
        }

        if (showReferralDialog) {
            ReferralRequiredDialog(
                service = selectedService,
                onDismiss = { showReferralDialog = false },
                onUploadReferral = {
                    showReferralDialog = false
                    pdfLauncher.launch("application/pdf")
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

        if (isUploading) {
            Dialog(onDismissRequest = {}) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Uploading your file...",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        LinearProgressIndicator(
                            progress = { uploadProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = DefaultPrimary,
                            trackColor = DefaultPrimary.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${(uploadProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DefaultOnPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = {
                                //implement task cancellation.
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = DefaultPrimary
                            )
                        ) {
                            Text("Cancel Upload")
                        }
                    }
                }
            }
        }

        if (showUploadSuccessDialog) {
            Dialog(onDismissRequest = {/* do nothing */ }) { // we dont want them to go back
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Green.copy(alpha = 0.7f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Upload Successful!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Your referral document has been uploaded successfully.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                showUploadSuccessDialog = false
                                if (city != null && doctorId == null) {
                                    navController.navigate("book_appointment/$city/${selectedService!!.displayName}/${referralId}")
                                } else {
                                    navController.navigate("book_appointment_dir/${doctorId}/${selectedService!!.displayName}/${referralId}")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DefaultPrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Continue to Booking")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FileTooLargeDialog(
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = DefaultBackground
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.file_kapi),
                        contentDescription = "File Too Large",
                        modifier = Modifier.size(150.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "File Too Large",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = DefaultPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "The selected file exceeds the maximum size limit of 5MB. Please choose a smaller file and try again.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Black
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DefaultPrimary,
                        containerColor = DefaultPrimary
                    ),
                    border = BorderStroke(1.dp, DefaultPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK", color = Color.White)
                }
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
                        tint = Yellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Referral needed",
                        style = MaterialTheme.typography.labelMedium,
                        color = Yellow
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