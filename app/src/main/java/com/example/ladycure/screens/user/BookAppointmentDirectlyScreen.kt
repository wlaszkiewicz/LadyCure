package com.example.ladycure.screens.user

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material3.Text
import coil.compose.AsyncImage
import com.example.ladycure.data.AppointmentType
import com.example.ladycure.data.doctor.Doctor
import com.example.ladycure.data.doctor.DoctorAvailability
import com.example.ladycure.data.doctor.Speciality
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.utility.SnackbarController
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun BookAppointmentDirectlyScreen(
    navController: NavController,
    snackbarController: SnackbarController?,
    doctorId: String,
    selectedService: AppointmentType,
    authRepo: AuthRepository = AuthRepository()
) {
    val selectedSpeciality = Speciality.fromDisplayName(selectedService.speciality)
    // State variables
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    var doctor = remember { mutableStateOf<Doctor?>(null) }
    val doctorAvailability = remember { mutableStateOf<List<DoctorAvailability>>(emptyList()) }

    // UI state
    val selectedDate = remember { mutableStateOf<LocalDate?>(null) }
    val selectedTimeSlot = remember { mutableStateOf<LocalTime?>(null) }

    LaunchedEffect(doctorId) {
        val result = authRepo.getDoctorById(doctorId)
        if (result.isSuccess) {
            doctor.value = result.getOrNull()
            val availabilityResult = authRepo.getDoctorAvailability(doctorId)
            if (availabilityResult.isSuccess) {
                doctorAvailability.value = availabilityResult.getOrNull()!!
                isLoading.value = false
            } else {
                errorMessage.value = availabilityResult.exceptionOrNull()?.message
            }
        } else {
            errorMessage.value = result.exceptionOrNull()?.message
        }
    }

    // Get unique available dates from filtered availabilities
    val availableDates = doctorAvailability.value
        .map { it.date }
        .distinct()
        .sortedBy { it }


    // Generate time slots for selected date
    val timeSlotsForSelectedDate = remember(selectedDate.value, doctorAvailability.value) {
        if (selectedDate.value == null) emptyList() else {
            filerTimeSlotsForDate(selectedDate.value!!, doctorAvailability.value)
        }
    }


    if (doctor.value == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = DefaultPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading doctor data...", color = DefaultOnPrimary)
        }
    } else {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground)
        ) {

            AppointmentHeader(
                onBackClick = {
                    navController.popBackStack()
                }
            )

            DoctorInfoHeader(
                doctor = doctor.value!!,
                modifier = Modifier.padding(horizontal = 16.dp),
                onClick = {
                    navController.popBackStack(
                        "doctors/${selectedSpeciality.displayName}",
                        false
                    ) // go back to doctor list
                }
            )

            LaunchedEffect(errorMessage.value) {
                errorMessage.value?.let {
                    snackbarController?.showMessage(it)
                }
            }

            when {
                isLoading.value -> LoadingView()
                !isLoading.value -> DateAndTimeSelectionView(
                    selectedService = selectedService,
                    availableDates = availableDates.map { it!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) },
                    selectedDate = selectedDate.value?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    onDateSelected = {
                        selectedDate.value =
                            LocalDate.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    },
                    timeSlots = timeSlotsForSelectedDate,
                    selectedTimeSlot = selectedTimeSlot.value?.format(
                        DateTimeFormatter.ofPattern(
                            "h:mm a",
                            Locale.US
                        )
                    ),
                    onTimeSlotSelected = {
                        selectedTimeSlot.value =
                            LocalTime.parse(it, DateTimeFormatter.ofPattern("h:mm a", Locale.US))
                        navController.navigate(
                            "confirmation/$doctorId/${selectedDate.value}/${
                                selectedTimeSlot.value!!.format(
                                    DateTimeFormatter.ofPattern("h:mm a", Locale.US)
                                )
                            }/${selectedService.displayName}"
                        )
                    })

            }
        }
    }
}

@Composable
private fun AppointmentHeader(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        IconButton(
            onClick = onBackClick,
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
            text = "Select Time",
            style = MaterialTheme.typography.titleLarge,
            color = DefaultOnPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun DateAndTimeSelectionView(
    selectedService: AppointmentType,
    availableDates: List<String>,
    selectedDate: String?,
    onDateSelected: (String) -> Unit,
    timeSlots: List<String>,
    selectedTimeSlot: String?,
    onTimeSlotSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Service info chip
        ServiceInfoChip(selectedService, modifier = Modifier.padding(bottom = 16.dp))

        // Date selection
        Text(
            text = "Select Date",
            style = MaterialTheme.typography.titleMedium,
            color = DefaultOnPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
        )

        // Enhanced date selector
        DateSelector(
            availableDates = availableDates,
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Time slots
        if (selectedDate != null) {
            Text(
                text = "Available Time Slots",
                style = MaterialTheme.typography.titleMedium,
                color = DefaultOnPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (timeSlots.isEmpty()) {
                PromptToSelectDate()
                //  EmptyTimeSlotsView()
            } else {
                TimeSlotGrid(
                    timeSlots = timeSlots,
                    selectedTimeSlot = selectedTimeSlot,
                    onTimeSlotSelected = onTimeSlotSelected
                )
            }
        } else if (availableDates.isNotEmpty()) {
            PromptToSelectDate()
        }
    }
}

@Composable
private fun DoctorInfoHeader(
    doctor: Doctor,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Doctor Image
            if (doctor.profilePictureUrl.isEmpty()) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Doctor ${doctor.name}",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    tint = DefaultPrimary
                )
            } else {
                AsyncImage(
                    model = doctor.profilePictureUrl,
                    contentDescription = "Doctor ${doctor.name}",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

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
                        modifier = Modifier.width(80.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${"%.1f".format(doctor.rating)})",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFFA000)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
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
                modifier = Modifier.size(32.dp)
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
private fun DateSelector(
    availableDates: List<String>,
    selectedDate: String?,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier) {
        if (availableDates.isEmpty()) {
            Text(
                text = "We are sorry, there's no available dates for this doctor",
                style = MaterialTheme.typography.bodyMedium,
                color = DefaultOnPrimary.copy(alpha = 0.9f),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableDates.forEach { date ->
                    DateCard(
                        date = date,
                        isSelected = date == selectedDate,
                        onSelect = { onDateSelected(date) },
                        modifier = Modifier.width(80.dp)
                    )
                }
            }
        }
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
        Text("Loading appointment data...", color = DefaultOnPrimary)
    }
}

@Preview
@Composable
fun BookAppointmentDirectlyScreenPreview() {
    val navController = rememberNavController()
    BookAppointmentDirectlyScreen(
        navController = navController,
        doctorId = "12345",
        selectedService = AppointmentType.CANCER_SCREENING,
        snackbarController = null
    )
}

