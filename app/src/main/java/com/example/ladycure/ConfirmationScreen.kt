package com.example.ladycure

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import com.example.ladycure.data.Appointment
import com.example.ladycure.data.AppointmentType
import com.example.ladycure.data.doctor.Doctor
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.utility.SnackbarController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import androidx.compose.ui.platform.LocalContext
import android.location.Geocoder
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun ConfirmationScreen(
    navController: NavController,
    snackbarController: SnackbarController?,
    doctorId: String,
    date: String,
    time: String,
    appointmentType: AppointmentType,
    authRepo: AuthRepository = AuthRepository()
) {

    val doctorInfo = remember { mutableStateOf<Map<String, Any>?>(null) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    var userName by remember { mutableStateOf("Patient unavaiable") }

    LaunchedEffect(Unit) {
        userName = authRepo.getUserField("name").getOrNull() + " " +
                authRepo.getUserField("surname").getOrNull()
    }
    // Create an appointment object
    val appointment = Appointment(
        appointmentId = "",
        doctorId = doctorId,
        date = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        time = LocalTime.parse(time, DateTimeFormatter.ofPattern("h:mm a", Locale.US)),
        patientId = authRepo.getCurrentUserId().toString(),
        status = Appointment.Status.PENDING,
        type = appointmentType,
        price = appointmentType.price,
        address = doctorInfo.value?.get("address") as? String ?: "Address unavaiable",
        doctorName = (doctorInfo.value?.get("name") as? String + " " +
                doctorInfo.value?.get("surname") as? String),
        patientName = userName,
        comments = "",
    )

    // Fetch doctor details
    LaunchedEffect(doctorId) {
        try {
            val result = authRepo.getDoctorById(doctorId)
            if (result.isSuccess) {
                val doctor = result.getOrNull()
                doctorInfo.value = Doctor.toMap(doctor!!)
            } else {
                errorMessage.value = "Failed to load doctor details"
            }
            isLoading.value = false
        } catch (e: Exception) {
            errorMessage.value = "Error: ${e.message}"
            isLoading.value = false
        }
    }

    if (isLoading.value) {
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
                errorMessage.value != null -> snackbarController?.showMessage(
                    message = errorMessage.value ?: "Unknown error"
                )

                doctorInfo.value == null -> snackbarController?.showMessage(
                    message = "Doctor info is unaviable"
                )

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
                                        text = formatConfirmationDate(date),
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
                                        text = formatConfirmationTime(time),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = DefaultOnPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        AppointmentTypeCard(
                            appointmentType = appointmentType,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )


                        // Doctor information card
                        DoctorConfirmationCard(
                            doctor = doctorInfo.value!!,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        LocationCard(
                            doctor = doctorInfo.value!!,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Payment information card (optional)
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
                            Button(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.LightGray,
                                    contentColor = DefaultOnPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        val result = authRepo.bookAppointment(appointment)
                                        if (result.isSuccess) {
                                            snackbarController?.showMessage(
                                                message = "Appointment booked successfully"
                                            )
                                            navController.navigate("booking_success/${result.getOrNull()}")
                                        } else {
                                            snackbarController?.showMessage(
                                                message = "Failed to book appointment: ${result.exceptionOrNull()?.message}"
                                            )
                                        }
                                    }
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
private fun AppointmentTypeCard(
    appointmentType: AppointmentType,
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
            if (appointmentType.needsReferral) {
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
                        android.net.Uri.parse("geo:${latLng.latitude},${latLng.longitude}?q=${fullAddress}")
                    } else {
                        android.net.Uri.parse("geo:0,0?q=${fullAddress}")
                    }
                    val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
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
    val specialization = doctor["specification"] as? String ?: "Specialist"
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

@Composable
private fun LoadingView() {
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


@Preview
@Composable
fun ConfirmationScreenPreview() {
    val navController = rememberNavController()
    ConfirmationScreen(
        navController = navController,
        snackbarController = null,
        doctorId = "123",
        date = "2023-12-25",
        time = "4:30 PM",
        appointmentType = AppointmentType.ECHOCARDIOGRAM
    )
}