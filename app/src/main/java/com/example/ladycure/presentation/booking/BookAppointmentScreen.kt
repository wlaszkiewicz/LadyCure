package com.example.ladycure.presentation.booking

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.wear.compose.material3.Text
import coil.compose.SubcomposeAsyncImage
import com.example.ladycure.domain.model.AppointmentType
import com.example.ladycure.domain.model.Doctor
import com.example.ladycure.domain.model.Speciality
import com.example.ladycure.presentation.booking.components.DateAndTimeSelectionView
import com.example.ladycure.presentation.booking.components.formatDateForDisplay
import com.example.ladycure.utility.SnackbarController
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.floor

@Composable
fun BookAppointmentScreen(
    navController: NavController,
    snackbarController: SnackbarController?,
    city: String,
    selectedService: AppointmentType,
    referralId: String? = null,
    viewModel: BookingViewModel = viewModel()
) {
    val selectedSpeciality = Speciality.fromDisplayName(selectedService.speciality)

    // Collect state from ViewModel
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val showDoctorsForSlot = viewModel.showDoctorsForSlot

    // Initialize data loading
    LaunchedEffect(selectedSpeciality) {
        viewModel.loadDoctorsBySpeciality(selectedSpeciality, city)
    }

    // Handle errors
    LaunchedEffect(errorMessage) {
        errorMessage?.let { err ->
            snackbarController?.showMessage(err)
            viewModel.errorMessage = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultBackground)
    ) {
        // Header
        AppointmentHeader(
            showDoctorsForSlot = showDoctorsForSlot,
            onBackClick = {
                if (showDoctorsForSlot) {
                    viewModel.toggleShowDoctorsForSlot(false)
                } else {
                    navController.popBackStack()
                }
            }
        )

        when {
            isLoading -> LoadingView()
            !showDoctorsForSlot -> DateAndTimeSelectionView(
                city = city,
                selectedSpeciality = selectedSpeciality,
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
                }
            )

            else -> DoctorSelectionView(
                selectedDate = viewModel.selectedDate!!,
                selectedTimeSlot = viewModel.selectedTimeSlot!!,
                doctors = viewModel.getAvailableDoctorsForSlot(selectedService.durationInMinutes),
                onBackClick = { viewModel.toggleShowDoctorsForSlot(false) },
                onDoctorSelected = { doctorId ->
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

@Composable
private fun AppointmentHeader(
    showDoctorsForSlot: Boolean,
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
            text = if (showDoctorsForSlot) "Available Doctors" else "Select Time",
            style = MaterialTheme.typography.titleLarge,
            color = DefaultOnPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(48.dp))
    }
}


@Composable
fun ServiceInfoChip(
    service: AppointmentType,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, DefaultPrimary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                painter = painterResource(Speciality.fromDisplayName(service.speciality).icon),
                contentDescription = "Service type",
                tint = DefaultPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = service.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = DefaultPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "• ${service.durationInMinutes} min",
                style = MaterialTheme.typography.labelMedium,
                color = DefaultPrimary.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
internal fun LocationSpecialtyRow(
    city: String,
    speciality: Speciality
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location",
            tint = DefaultOnPrimary.copy(alpha = 0.8f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = city,
            style = MaterialTheme.typography.bodyMedium,
            color = DefaultOnPrimary.copy(alpha = 0.8f),
            modifier = Modifier.padding(end = 12.dp)
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DefaultPrimary.copy(alpha = 0.1f)
        ) {
            Text(
                text = speciality.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = DefaultPrimary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}


@Composable
private fun DoctorSelectionView(
    selectedDate: LocalDate,
    selectedTimeSlot: LocalTime,
    doctors: List<Doctor>,
    onBackClick: () -> Unit,
    onDoctorSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        SelectedTimeInfo(
            date = selectedDate,
            time = selectedTimeSlot,
            onBackClick = onBackClick
        )

        if (doctors.isEmpty()) {
            EmptyDoctorsView()
        } else {
            Text(
                text = "Available Doctors",
                style = MaterialTheme.typography.titleMedium,
                color = DefaultOnPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                items(doctors) { doctor ->
                    DoctorCard(
                        doctor = doctor,
                        onSelect = { onDoctorSelected(doctor.id.toString()) },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedTimeInfo(
    date: LocalDate,
    time: LocalTime,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val borderColor: Color by animateColorAsState(
        targetValue = if (interactionSource.collectIsPressedAsState().value) {
            DefaultPrimary.copy(alpha = 0.4f)
        } else {
            DefaultPrimary.copy(alpha = 0.2f)
        },
        animationSpec = tween(durationMillis = 200),
        label = "borderColor"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRippleIndication(),
                onClick = onBackClick
            ),
        color = DefaultPrimary.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatDateForDisplay(date),
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Time",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = time.format(
                            DateTimeFormatter.ofPattern(
                                "h:mm a",
                                Locale.getDefault()
                            )
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DefaultOnPrimary
                    )
                }
            }

            // Change button with icon
            TextButton(
                onClick = onBackClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = DefaultPrimary
                ),
                modifier = Modifier.padding(start = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Change",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Change",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = DefaultPrimary
                )
            }
        }
    }
}

@Composable
private fun rememberRippleIndication(): Indication {
    return LocalIndication.current
}

@Composable
private fun EmptyDoctorsView() {
    // Technically, this should never happen if the filtering logic is correct but you never know so its here just in case
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No doctors available",
            style = MaterialTheme.typography.titleMedium,
            color = DefaultOnPrimary.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
        Text(
            text = "No doctors available for the selected time slot",
            style = MaterialTheme.typography.bodyMedium,
            color = DefaultOnPrimary.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}


@Composable
fun DoctorCard(
    doctor: Doctor,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onSelect,
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row with image and basic info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Doctor image
                if (doctor.profilePictureUrl.isEmpty()) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Doctor ${doctor.name}",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        tint = Color.Gray
                    )
                } else {

                    SubcomposeAsyncImage(
                        model = doctor.profilePictureUrl,
                        contentDescription = "Doctor ${doctor.name}",
                        loading = {
                            Box(modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.Center),
                                    color = DefaultPrimary,
                                    strokeWidth = 3.dp,
                                )
                            }
                        },
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Doctor basic info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Dr. ${doctor.name} ${doctor.surname}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = DefaultOnPrimary
                    )

                    // Rating and experience in one line
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = " ${"%.1f".format(doctor.rating)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFFFA000),
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = "Experience",
                            tint = DefaultPrimary.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = " ${doctor.experience} yrs",
                            style = MaterialTheme.typography.labelMedium,
                            color = DefaultOnPrimary.copy(alpha = 0.7f)
                        )
                    }

                    // Languages in one line
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Languages",
                            tint = DefaultPrimary.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = " ${
                                doctor.languages.joinToString()
                            }",
                            style = MaterialTheme.typography.labelSmall,
                            color = DefaultOnPrimary.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Address",
                    tint = DefaultPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = doctor.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = DefaultOnPrimary.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                )
            }


            Spacer(modifier = Modifier.height(8.dp))

            // Bio (collapsible)
            var expanded by remember { mutableStateOf(false) }
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Bio",
                        tint = DefaultPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "About Dr. ${doctor.surname}",
                        style = MaterialTheme.typography.labelLarge,
                        color = DefaultOnPrimary.copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (!expanded) {
                    Text(
                        text = doctor.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = doctor.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = if (expanded) "Show less" else "Show more",
                        color = DefaultPrimary
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

        }
    }
}

@Composable
fun RatingBar(
    rating: Double,
    modifier: Modifier = Modifier
) {
    val filledStars = floor(rating).toInt()
    val halfStar = rating - filledStars >= 0.5
    val emptyStars = 5 - filledStars - (if (halfStar) 1 else 0)

    Row(modifier = modifier) {
        repeat(filledStars) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Filled star",
                tint = Color(0xFFFFA000),
                modifier = Modifier.size(16.dp)
            )
        }
        if (halfStar) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.StarHalf,
                contentDescription = "Half star",
                tint = Color(0xFFFFA000),
                modifier = Modifier.size(16.dp)
            )
        }
        repeat(emptyStars) {
            Icon(
                imageVector = Icons.Default.StarOutline,
                contentDescription = "Empty star",
                tint = Color(0xFFFFA000),
                modifier = Modifier.size(16.dp)
            )
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

