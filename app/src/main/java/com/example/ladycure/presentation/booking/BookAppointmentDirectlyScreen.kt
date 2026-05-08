package com.example.ladycure.presentation.booking

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.wear.compose.material3.Text
import coil.compose.AsyncImage
import com.example.ladycure.domain.model.AppointmentType
import com.example.ladycure.domain.model.Doctor
import com.example.ladycure.domain.model.Speciality
import com.example.ladycure.presentation.booking.components.DateAndTimeSelectionView
import com.example.ladycure.ui.theme.DefaultBackground
import com.example.ladycure.ui.theme.DefaultOnPrimary
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.YellowOrange
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import com.example.ladycure.utility.SnackbarController
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun BookAppointmentDirectlyScreen(
    navController: NavController,
    snackbarController: SnackbarController?,
    doctorId: String,
    selectedService: AppointmentType,
    referralId: String? = null,
    viewModel: BookingViewModel = viewModel()
) {
    val dimens = rememberResponsiveDimens()
    val selectedSpeciality = Speciality.fromDisplayName(selectedService.speciality)

    // Collect state from ViewModel
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val doctors = viewModel.doctors

    // Initialize data loading
    LaunchedEffect(doctorId) {
        viewModel.loadDoctorById(doctorId)
    }

    // Handle errors
    LaunchedEffect(errorMessage) {
        errorMessage?.let { err ->
            snackbarController?.showMessage(err)
            viewModel.errorMessage = null
        }
    }

    if (doctors.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = DefaultPrimary)
            Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))
            Text("Loading doctor data...", color = DefaultOnPrimary)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground)
        ) {
            AppointmentHeader(
                onBackClick = { navController.popBackStack() }
            )

            DoctorInfoHeader(
                doctor = doctors.first(),
                modifier = Modifier.padding(horizontal = dimens.w(16 / 411f)),
                onClick = {
                    navController.popBackStack(
                        "doctors/${selectedSpeciality.displayName}",
                        false
                    )
                }
            )

            when {
                isLoading -> LoadingView()
                !isLoading -> DateAndTimeSelectionView(
                    selectedService = selectedService,
                    availableDates = viewModel.availableDates,
                    selectedDate = viewModel.selectedDate,
                    onDateSelected = { date -> viewModel.selectDate(date) },
                    timeSlots = viewModel.getTimeSlotsForSelectedDate(selectedService.durationInMinutes),
                    selectedTimeSlot = viewModel.selectedTimeSlot,
                    onTimeSlotSelected = { time ->
                        var time = LocalTime.parse(
                            time,
                            DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
                        )
                        viewModel.selectTimeSlot(time)
                        val timestamp = viewModel.createTimestamp()
                        if (referralId == null) {
                            navController.navigate(
                                "confirmation/$doctorId/${timestamp.seconds}/${timestamp.nanoseconds}/${selectedService.displayName}"
                            )
                        } else {
                            navController.navigate(
                                "confirmation/$doctorId/${timestamp.seconds}/${timestamp.nanoseconds}/${selectedService.displayName}/$referralId"
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AppointmentHeader(
    onBackClick: () -> Unit
) {
    val dimens = rememberResponsiveDimens()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = dimens.h(20 / 914f),
                bottom = dimens.h(16 / 914f),
                start = dimens.w(16 / 411f),
                end = dimens.w(16 / 411f)
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(dimens.w(48 / 411f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Go back",
                tint = DefaultOnPrimary,
            )
        }

        Spacer(modifier = Modifier.width(dimens.w(16 / 411f)))

        Text(
            text = "Select Time",
            style = MaterialTheme.typography.titleLarge,
            color = DefaultOnPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(dimens.w(48 / 411f)))
    }
}


@Composable
private fun DoctorInfoHeader(
    doctor: Doctor,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val dimens = rememberResponsiveDimens()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = dimens.h(16 / 914f)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.5f),
            contentColor = DefaultOnPrimary
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, DefaultPrimary.copy(alpha = 0.3f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.w(16 / 411f), vertical = dimens.h(16 / 914f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Doctor Image
            if (doctor.profilePictureUrl.isEmpty()) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Doctor ${doctor.name}",
                    modifier = Modifier
                        .size(dimens.w(64 / 411f))
                        .clip(CircleShape),
                    tint = DefaultPrimary
                )
            } else {
                AsyncImage(
                    model = doctor.profilePictureUrl,
                    contentDescription = "Doctor ${doctor.name}",
                    modifier = Modifier
                        .size(dimens.w(64 / 411f))
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(dimens.w(16 / 411f)))

            // Doctor Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Dr. ${doctor.name} ${doctor.surname}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = DefaultOnPrimary
                )

                Text(
                    text = doctor.speciality.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DefaultPrimary,
                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                )

                // Rating and Experience
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RatingBar(
                        rating = doctor.rating,
                        modifier = Modifier.width(dimens.w(80 / 411f))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${"%.1f".format(doctor.rating)})",
                        style = MaterialTheme.typography.labelMedium,
                        color = YellowOrange
                    )
                    Spacer(modifier = Modifier.width(dimens.w(12 / 411f)))
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = "Experience",
                        tint = DefaultPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${doctor.experience} yrs",
                        style = MaterialTheme.typography.labelMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.7f)
                    )
                }
            }
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(dimens.w(32 / 411f))
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Change doctor",
                    tint = DefaultPrimary
                )
            }
        }
    }
}


@Composable
private fun LoadingView() {
    val dimens = rememberResponsiveDimens()
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = DefaultPrimary)
        Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))
        Text("Loading appointment data...", color = DefaultOnPrimary)
    }
}
