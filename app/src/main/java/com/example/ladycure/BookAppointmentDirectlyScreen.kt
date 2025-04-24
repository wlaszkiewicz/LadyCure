package com.example.ladycure

import DefaultOnPrimary
import DefaultPrimary
import DefaultBackground
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import com.example.ladycure.data.doctor.Specialization
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material3.Text
import com.example.ladycure.data.AppointmentType
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.ladycure.data.doctor.DoctorAvailability
import com.example.ladycure.repository.AuthRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.floor

@Composable
fun BookAppointmentDirectlyScreen(
    navController: NavController,
    doctorId: String,
    selectedService: AppointmentType,
    authRepo: AuthRepository = AuthRepository()
) {
    val selectedSpecialization = Specialization.fromDisplayName(selectedService.specialization)
    // State variables
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    var doctor by remember { mutableStateOf<Map<String, Any>?>(null) }
    val doctorAvailability = remember { mutableStateOf<List<DoctorAvailability>>(emptyList())}

    // UI state
    val selectedDate = remember { mutableStateOf<String?>(null) }
    val selectedTimeSlot = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(doctorId) {
        val result = authRepo.getDoctorById(doctorId)
        if (result.isSuccess) {
            doctor = result.getOrNull()
            val aviabilityResult = authRepo.getDoctorAvailability(doctorId)
            if (aviabilityResult.isSuccess) {
                doctorAvailability.value = aviabilityResult.getOrNull()!!
                isLoading.value = false
            } else {
                errorMessage.value = aviabilityResult.exceptionOrNull()?.message
            }
        } else {
            errorMessage.value = result.exceptionOrNull()?.message
        }
    }

    // Get unique available dates from filtered availabilities
    val availableDates = doctorAvailability.value
        .map { it.date }
        .distinct()
        .sortedBy { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }

    // Generate time slots for selected date
    val timeSlotsForSelectedDate = remember(selectedDate.value, doctorAvailability.value) {
        if (selectedDate.value == null) emptyList() else {
            generateTimeSlotsForDate(selectedDate.value!!, doctorAvailability.value)
        }
    }

    BaseScaffold { snackbarController ->
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
                doctor = doctor,
                modifier = Modifier.padding(horizontal = 16.dp),
                onClick = {
                    navController.popBackStack("doctors/${selectedSpecialization.displayName}",false) // go back to doctor list
                }
            )

            LaunchedEffect(errorMessage.value) {
                errorMessage.value?.let {
                    snackbarController.showSnackbar(it)
                }
            }

            when {
                isLoading.value -> LoadingView()
                !isLoading.value -> DateAndTimeSelectionView(
                    selectedService = selectedService,
                    availableDates = availableDates,
                    selectedDate = selectedDate.value,
                    onDateSelected = { selectedDate.value = it },
                    timeSlots = timeSlotsForSelectedDate,
                    selectedTimeSlot = selectedTimeSlot.value,
                    onTimeSlotSelected = {
                        selectedTimeSlot.value = it
                        navController.navigate("confirmation/$doctorId/${selectedDate.value}/${selectedTimeSlot.value}/${selectedService.displayName}")
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
            modifier = Modifier.weight(1f))

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
            modifier = Modifier.padding(bottom = 12.dp, top = 8.dp))

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
                modifier = Modifier.padding(bottom = 12.dp))

            if (timeSlots.isEmpty()) {
                EmptyTimeSlotsView()
            } else {
                TimeSlotGrid(
                    timeSlots = timeSlots,
                    selectedTimeSlot = selectedTimeSlot,
                    onTimeSlotSelected = onTimeSlotSelected)
            }
        } else if (availableDates.isNotEmpty()) {
            PromptToSelectDate()
        }
    }
}

@Composable
private fun DoctorInfoHeader(
    doctor: Map<String, Any>?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val name = doctor?.get("name") as? String ?: "Dr. Unknown"
    val surname = doctor?.get("surname") as? String ?: ""
    val specialization = doctor?.get("specification") as? String ?: "Specialist"
    val imageUrl = doctor?.get("profilePictureUrl") as? String ?: ""
    val experience = when (val exp = doctor?.get("experience")) {
        is Int -> exp
        is Long -> exp.toInt()
        is Double -> exp.toInt()
        is String -> exp.toIntOrNull() ?: 5
        else -> 5
    }

    val rating = when (val rat = doctor?.get("rating")) {
        is Int -> rat.toDouble()
        is Long -> rat.toDouble()
        is Double -> rat
        is String -> rat.toDouble()
        else -> 4.5
    }

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
            if (imageUrl.isEmpty()) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Doctor $name",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    tint = DefaultPrimary
                )
            } else {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Doctor $name",
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
                    text = "Dr. $name $surname",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = DefaultOnPrimary
                )

                Text(
                    text = specialization,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DefaultPrimary,
                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                )

                // Rating and Experience
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RatingBar(
                        rating = rating,
                        modifier = Modifier.width(80.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${"%.1f".format(rating)})",
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
                        text = "$experience yrs",
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
                text = "We are sorry, there's no available dates for this specialization",
                style = MaterialTheme.typography.bodyMedium,
                color = DefaultOnPrimary.copy(alpha = 0.9f),
                modifier = Modifier.padding(vertical = 16.dp))
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
        selectedService = AppointmentType.CANCER_SCREENING
    )
}

