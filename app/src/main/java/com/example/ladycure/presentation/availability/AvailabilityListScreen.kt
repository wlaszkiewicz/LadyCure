package com.example.ladycure.presentation.availability

import DefaultBackground
import DefaultPrimary
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ladycure.data.repository.AuthRepository
import com.example.ladycure.data.repository.DoctorRepository
import com.example.ladycure.domain.model.DoctorAvailability
import com.example.ladycure.utility.SnackbarController
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Composable function for displaying the availability list screen.
 *
 * @param navController The NavController for navigation.
 * @param snackbarController The SnackbarController for displaying messages.
 * @param isAdminView Boolean indicating if the screen is viewed by an admin.
 * @param doctorId The ID of the doctor whose availability is to be displayed (only if [isAdminView] is true).
 */
@Composable
fun AvailabilityListScreen(
    navController: NavController,
    snackbarController: SnackbarController,
    isAdminView: Boolean = false,
    doctorId: String? = null
) {
    val existingAvailabilities = remember { mutableStateOf<List<DoctorAvailability>>(emptyList()) }
    val doctorRepo = DoctorRepository()
    val authRepo = AuthRepository()
    val isLoading = remember { mutableStateOf(false) }
    val currentMonth = remember { LocalDate.now().withDayOfMonth(1) }
    var chosenMonth by remember { mutableStateOf(currentMonth) }
    var availabilitiesInMonth by remember {
        mutableStateOf<Map<LocalDate?, List<DoctorAvailability>>>(
            emptyMap()
        )
    }

    val effectiveDoctorId = if (isAdminView) {
        doctorId ?: run {
            LaunchedEffect(Unit) {
                snackbarController.showMessage("Doctor ID is missing")
                navController.popBackStack()
            }
            return
        }
    } else {
        authRepo.getCurrentUserId() ?: run {
            LaunchedEffect(Unit) {
                snackbarController.showMessage("User not logged in")
                navController.popBackStack()
            }
            return
        }
    }

    availabilitiesInMonth = existingAvailabilities.value.filter { availability ->
        val date = availability.date
        val today = LocalDate.now()
        date != null && date.isAfter(today.minusDays(1)) &&
                (date != today || availability.availableSlots.any { it.isAfter(LocalTime.now()) }) &&
                (date.month == chosenMonth.month && date.year == chosenMonth.year)
    }.groupBy { it.date }

    LaunchedEffect(effectiveDoctorId) {
        isLoading.value = true
        try {
            val result = doctorRepo.getDoctorAvailability(effectiveDoctorId)
            if (result.isSuccess) {
                existingAvailabilities.value = result.getOrThrow()
            } else {
                snackbarController.showMessage("Error loading availability data")
            }
        } catch (e: Exception) {
            snackbarController.showMessage("Error: ${e.message}")
        } finally {
            isLoading.value = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultBackground)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.ArrowBack, "Back", tint = DefaultPrimary)
            }
            Spacer(Modifier.width(16.dp))
            Text(
                if (isAdminView) "Doctor Availability" else "My Availability",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = DefaultPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        if (isLoading.value) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DefaultPrimary)
            }
        } else {

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { chosenMonth = chosenMonth.minus(1, ChronoUnit.MONTHS) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Previous Month",
                        tint = DefaultPrimary
                    )
                }
                Text(
                    chosenMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                            " " + chosenMonth.year,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DefaultPrimary
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                IconButton(
                    onClick = {
                        chosenMonth = chosenMonth.plus(1, ChronoUnit.MONTHS)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "Next Month",
                        tint = DefaultPrimary
                    )
                }
            }

            if (availabilitiesInMonth.isEmpty()) {
                EmptyAvailabilityState()
            } else {
                AvailabilityContent(availabilitiesInMonth)
            }
        }
    }
}

/**
 * Composable function to display an empty availability state.
 */
@Composable
private fun EmptyAvailabilityState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        )
        {
            Icon(
                Icons.Default.Schedule,
                contentDescription = "No availability",
                tint = DefaultPrimary.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "No availability scheduled",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = DefaultPrimary.copy(alpha = 0.8f)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Add availability to start accepting appointments",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray
                )
            )
        }
    }
}

/**
 * Composable function to display the availability content.
 *
 * @param availabilitiesInMonth A map where the key is LocalDate and the value is a list of DoctorAvailability for that date.
 */
@Composable
private fun AvailabilityContent(
    availabilitiesInMonth: Map<LocalDate?, List<DoctorAvailability>>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        availabilitiesInMonth.forEach { (date, availabilities) ->
            item {
                ExistingAvailabilityDayItem(
                    date = date,
                    availabilities = availabilities,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .animateContentSize()
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Composable function to display a single day's existing availability.
 *
 * @param date The LocalDate for which the availability is displayed.
 * @param availabilities A list of DoctorAvailability for the given [date].
 * @param modifier The Modifier to be applied to the layout.
 */
@Composable
private fun ExistingAvailabilityDayItem(
    date: LocalDate?,
    availabilities: List<DoctorAvailability>,
    modifier: Modifier = Modifier
) {
    val elevation by animateDpAsState(
        targetValue = 2.dp,
        animationSpec = tween(durationMillis = 100)
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = elevation,
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = { /* Handle click if needed */ }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    buildString {
                        append(date!!.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()))
                        append(", ")
                        append(date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
                        append(" ")
                        append(date.dayOfMonth)
                    },
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.8f)
                    )
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = DefaultPrimary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = "Availability",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            availabilities.forEachIndexed { index, availability ->
                val startTime = availability.startTime
                val endTime = availability.endTime
                val availableSlots = availability.availableSlots

                if (startTime != null && endTime != null) {
                    if (index > 0) {
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.LightGray.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = DefaultPrimary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${availableSlots.size}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DefaultPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                "${startTime.format(DateTimeFormatter.ofPattern("h:mm a"))} - " +
                                        endTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.Black.copy(alpha = 0.8f)
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            TimeSlotsVisualization(
                                startTime = startTime,
                                endTime = endTime,
                                availableSlots = availableSlots,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable function to visualize time slots.
 *
 * @param startTime The start time of the availability block.
 * @param endTime The end time of the availability block.
 * @param availableSlots A list of LocalTime representing the available slots within the block.
 * @param modifier The Modifier to be applied to the layout.
 */
@Composable
private fun TimeSlotsVisualization(
    startTime: LocalTime,
    endTime: LocalTime,
    availableSlots: List<LocalTime>,
    modifier: Modifier = Modifier
) {
    val slotDuration = 15
    val allPossibleSlots = remember(startTime, endTime) {
        generateSequence(startTime) { it.plusMinutes(slotDuration.toLong()) }
            .takeWhile { it.isBefore(endTime) }
            .toList()
    }

    val validAvailableSlots = remember(availableSlots, allPossibleSlots) {
        availableSlots.filter { it in allPossibleSlots }.toSet()
    }

    val expanded = remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${validAvailableSlots.size} of ${allPossibleSlots.size} slots available",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.Gray
                )
            )

            IconButton(
                onClick = { expanded.value = !expanded.value },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    modifier = Modifier.fillMaxSize(),
                    imageVector = if (expanded.value) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded.value) "Collapse" else "Expand",
                    tint = DefaultPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.LightGray.copy(alpha = 0.1f))
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                allPossibleSlots.forEach { slotTime ->
                    val isAvailable = slotTime in validAvailableSlots

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                color = if (isAvailable) DefaultPrimary.copy(
                                    alpha = if (slotTime.minute % 30 == 0) 0.8f else 0.5f
                                ) else Color.Gray
                            )
                            .border(0.5.dp, Color.White.copy(alpha = 0.7f))
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                startTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
            )
            Text(
                endTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
            )
        }

        AnimatedVisibility(
            visible = expanded.value,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                val slotsByHour = allPossibleSlots.groupBy { it.hour }

                slotsByHour.forEach { (hour, hourSlots) ->
                    val hourText = LocalTime.of(hour, 0).format(DateTimeFormatter.ofPattern("h a"))

                    Text(
                        text = hourText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DefaultPrimary.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        hourSlots.forEach { slotTime ->
                            val isAvailable = slotTime in validAvailableSlots

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isAvailable) DefaultPrimary.copy(alpha = 0.1f)
                                        else Color.Red.copy(alpha = 0.05f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isAvailable) DefaultPrimary.copy(alpha = 0.5f)
                                        else Color.Gray,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        slotTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isAvailable) DefaultPrimary
                                            else Color.Gray
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (isAvailable) Icons.Default.CheckCircle
                                        else Icons.Default.HighlightOff,
                                        contentDescription = if (isAvailable) "Available" else "Booked",
                                        tint = if (isAvailable) DefaultPrimary
                                        else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}