package com.example.ladycure.presentation.booking

import BabyBlue
import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import Red
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.example.ladycure.domain.model.AppointmentType
import com.example.ladycure.domain.model.Referral
import com.example.ladycure.utility.SnackbarController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun ConfirmationScreen(
    navController: NavController,
    snackbarController: SnackbarController?,
    doctorId: String,
    timestamp: Timestamp,
    appointmentType: AppointmentType,
    referralId: String? = null,
    viewModel: ConfirmationViewModel = viewModel()
) {
    // Collect state from ViewModel
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val doctorInfo = viewModel.doctorInfo
    val referral = viewModel.referral
    val isUploading = viewModel.isUploading
    val showUploadSuccess = viewModel.showUploadSuccess
    val uploadProgress = viewModel.uploadProgress

    // Initialize data loading
    LaunchedEffect(Unit) {
        viewModel.loadInitialData(doctorId, timestamp, referralId)
    }

    // Handle errors
    LaunchedEffect(errorMessage) {
        errorMessage?.let { err ->
            snackbarController?.showMessage(err)
            viewModel.errorMessage = null
        }
    }

    // PDF upload launcher
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.uploadReferral(
                    uri = it,
                    referralId = referralId.toString(),
                    appointmentType = appointmentType,
                    onSuccess = { /* Success handled in ViewModel */ },
                    onError = { message ->
                        snackbarController?.showMessage(message)
                    }
                )
            }
        }
    )

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground),
            contentAlignment = Alignment.Center
        ) {
            LoadingView()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground)
                .padding(top = 20.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
        ) {
            // Header with back button
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    text = "Confirm Appointment",
                    style = MaterialTheme.typography.titleLarge,
                    color = DefaultOnPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            when {
                doctorInfo == null -> snackbarController?.showMessage("Doctor info is unavailable")
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Appointment confirmation card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White,
                                contentColor = DefaultPrimary
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Appointment Scheduled",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Date:", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = viewModel.formattedDate,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = DefaultOnPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Time:", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = viewModel.formattedTime,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = DefaultOnPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        AppointmentTypeCard(
                            appointmentType = appointmentType,
                            referralId = referralId,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (referralId != null) {
                            ReferralInfoCard(
                                referral = referral,
                                onUploadNew = {
                                    pdfLauncher.launch("application/pdf")
                                },
                                modifier = Modifier.padding(bottom = 16.dp),
                                isUploading = isUploading,
                                uploadProgress = uploadProgress,
                                showUploadSuccess = showUploadSuccess
                            )
                        }

                        // Doctor information card
                        DoctorConfirmationCard(
                            doctor = doctorInfo,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        LocationCard(
                            doctor = doctorInfo,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Payment information card
                        PaymentCard(
                            modifier = Modifier.padding(bottom = 24.dp),
                            appointmentType = appointmentType
                        )

                        // Action buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = DefaultOnPrimary,
                                ),
                                border = BorderStroke(1.dp, DefaultOnPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    viewModel.bookAppointment(
                                        doctorId = doctorId,
                                        timestamp = timestamp,
                                        appointmentType = appointmentType,
                                        onSuccess = { appointmentId ->
                                            snackbarController?.showMessage(
                                                "Appointment booked successfully"
                                            )
                                            if (referralId == null) {
                                                navController.navigate("booking_success/$appointmentId")
                                            } else {
                                                navController.navigate("booking_success/$appointmentId/$referralId")
                                            }
                                        },
                                        onError = { message ->
                                            snackbarController?.showMessage(message)
                                        }
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DefaultPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Confirm Booking")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = DefaultPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading appointment details...", color = DefaultOnPrimary)
        }
    }
}

@Composable
private fun AppointmentTypeCard(
    appointmentType: AppointmentType,
    referralId: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with service name and duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointmentType.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${appointmentType.durationInMinutes} min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DefaultPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DefaultPrimary.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Service description
            Text(
                text = "Service Description",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = appointmentType.additionalInfo,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Preparation instructions
            Text(
                text = "Preparation Instructions",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = appointmentType.preparationInstructions,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Requirements chip
            if (appointmentType.needsReferral && referralId == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Referral required",
                        tint = Color(0xFFFFA000),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Medical referral required",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFFA000)
                    )
                }
            }
        }
    }
}

// Update PaymentCard to use appointmentType.price
@Composable
private fun PaymentCard(
    appointmentType: AppointmentType,
    modifier: Modifier = Modifier
) {
    val taxRate = 0.09 // 9% tax
    val taxAmount = appointmentType.price * taxRate
    val totalAmount = appointmentType.price + taxAmount

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Payment Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Service Fee",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$${"%.2f".format(appointmentType.price)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tax (9%)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$${"%.2f".format(taxAmount)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color.LightGray,
                thickness = 1.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Amount",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$${"%.2f".format(totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DefaultPrimary
                )
            }
        }
    }
}

@Composable
private fun LocationCard(
    doctor: Map<String, Any>,
    modifier: Modifier = Modifier
) {
    val address = doctor["address"] as? String ?: "Address unavailable"
    val city = doctor["city"] as? String ?: ""
    val fullAddress = "$address, $city"

    val context = LocalContext.current
    val geocoder = Geocoder(context, Locale.getDefault())
    val location = remember(fullAddress) {
        geocoder.getFromLocationName(fullAddress, 1)?.firstOrNull()
    }
    val latLng = location?.let { LatLng(it.latitude, it.longitude) }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Location",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Google Map
            if (latLng != null) {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(latLng, 15f)
                    },
                    properties = MapProperties(isBuildingEnabled = true),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                ) {
                    Marker(
                        state = MarkerState(position = latLng),
                        title = "Clinic Location",
                        snippet = fullAddress
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading map...")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Contact details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Address",
                    tint = DefaultPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Clinic Address",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Phone",
                    tint = DefaultPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Contact Number",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = doctor["phone"] as? String ?: "Phone unavailable",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Directions button
            Button(
                onClick = {
                    val gmmIntentUri = if (latLng != null) {
                        "geo:${latLng.latitude},${latLng.longitude}?q=${fullAddress}".toUri()
                    } else {
                        "geo:0,0?q=${fullAddress}".toUri()
                    }
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    context.startActivity(mapIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultPrimary.copy(alpha = 0.1f),
                    contentColor = DefaultPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Directions,
                    contentDescription = "Directions",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Get Directions")
            }
        }
    }
}


@Composable
private fun DoctorConfirmationCard(
    doctor: Map<String, Any>,
    modifier: Modifier = Modifier
) {
    val name = doctor["name"] as? String ?: "Dr. Unknown"
    val surname = doctor["surname"] as? String ?: "Unknown"
    val specialization = doctor["speciality"] as? String ?: "Specialist"
    val imageUrl = doctor["profilePictureUrl"] as? String ?: ""
    val bio = doctor["bio"] as? String ?: "Experienced medical professional"

    val experience = when (val exp = doctor["experience"]) {
        is Int -> exp
        is Long -> exp.toInt()
        is Double -> exp.toInt()
        is String -> exp.toIntOrNull() ?: 5
        else -> 5
    }

    val rating = when (val rat = doctor["rating"]) {
        is Int -> rat.toDouble()
        is Long -> rat.toDouble()
        is Double -> rat
        is String -> rat.toDouble()
        else -> 4.5
    }


    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Your Doctor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "Doctor $name",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = DefaultPrimary
                            )
                        }
                    },
                    error = {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Doctor $name",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            tint = Color.Gray
                        )
                    }
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Dr. $name $surname",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = specialization,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⭐ $rating",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFFFA000)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "•",
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$experience yrs exp",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            Text(
                text = "About Dr. ${name.split(" ").last()}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = bio,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ReferralInfoCard(
    referral: Referral?,
    onUploadNew: () -> Unit,
    modifier: Modifier = Modifier,
    isUploading: Boolean = false,
    uploadProgress: Float = 0f,
    showUploadSuccess: Boolean = false
) {
    val referralUrl = referral?.url
    val serviceName = referral?.service
    val uploadDate = referral?.uploadedAt
    val context = LocalContext.current
    val pdfIconPainter = rememberVectorPainter(Icons.Default.PictureAsPdf)
    val fileSize = remember(referralUrl) { calculateFileSize(context, referralUrl) }

    // Animation for success message
    var showSuccessMessage by remember { mutableStateOf(false) }
    LaunchedEffect(showUploadSuccess) {
        if (showUploadSuccess) {
            showSuccessMessage = true
            delay(2000) // Show for 2 seconds
            showSuccessMessage = false
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Referral Document",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Upload progress section
            if (isUploading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { uploadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = BabyBlue,
                        trackColor = BabyBlue.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Uploading... ${(uploadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = BabyBlue,
                    )
                }
            } else if (showSuccessMessage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BabyBlue.copy(alpha = 0.1f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = BabyBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Upload successful!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BabyBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (!isUploading && referralUrl != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = pdfIconPainter,
                        contentDescription = "PDF",
                        tint = Red.copy(alpha = 0.8f),
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Referral.pdf",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = fileSize ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = { openPdf(context, referralUrl) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "View",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "View",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Metadata
                Column(
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    InfoRow(
                        icon = Icons.Default.Badge,
                        label = "For service:",
                        value = serviceName ?: "Not specified"
                    )

                    InfoRow(
                        icon = Icons.Default.DateRange,
                        label = "Uploaded:",
                        value = uploadDate?.let {
                            SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                                .format(Date(it))
                        } ?: "Unknown date"
                    )
                }
            } else if (!isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No referral uploaded",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Upload/Change Button (disabled during upload)
            Button(
                onClick = onUploadNew,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUploading) Color.LightGray else DefaultPrimary.copy(alpha = 0.1f),
                    contentColor = if (isUploading) Color.Gray else DefaultPrimary
                ),
                enabled = !isUploading,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Icon(
                    imageVector = if (referralUrl == null) Icons.Default.Upload else Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (referralUrl == null) "Upload Referral" else "Change Document",
                    color = if (isUploading) Color.Gray else DefaultPrimary
                )
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun calculateFileSize(context: Context, uriString: String?): String? {
    if (uriString == null) return null

    return try {
        val uri = uriString.toUri()
        val file = when (uri.scheme) {
            "content" -> {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                        it.getString(sizeIndex)
                    } else null
                }
            }

            "file" -> File(uri.path!!).length().toString()
            else -> null
        }

        file?.let {
            val sizeBytes = it.toLong()
            when {
                sizeBytes >= 1_000_000 -> "${sizeBytes / 1_000_000} MB"
                sizeBytes >= 1_000 -> "${sizeBytes / 1_000} KB"
                else -> "$sizeBytes bytes"
            }
        }
    } catch (e: Exception) {
        null
    }
}

private fun openPdf(context: Context, pdfUrl: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUrl.toUri(), "application/pdf")
            setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No PDF viewer installed", Toast.LENGTH_SHORT).show()
    }
}


// Helper functions for date/time formatting
fun formatConfirmationDate(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
    } catch (e: Exception) {
        dateString
    }
}

fun formatConfirmationTime(timeString: String): String {
    return try {
        val time = LocalTime.parse(timeString)
        time.format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
    } catch (e: Exception) {
        timeString
    }
}

