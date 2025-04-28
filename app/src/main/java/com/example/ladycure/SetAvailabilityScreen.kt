package com.example.ladycure

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavHostController
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import com.example.ladycure.repository.AuthRepository
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import DefaultPrimary
import DefaultOnPrimary
import DefaultBackground
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TimePickerDialog

import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.ladycure.data.doctor.DoctorAvailability
import com.example.ladycure.utility.SnackbarController
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetAvailabilityScreen(
    navController: NavHostController,
    snackbarController: SnackbarController,
    authRepo: AuthRepository = AuthRepository()
) {
    // State management
    val selectedDates = remember { mutableStateOf<Set<LocalDate>>(emptySet()) }
    val showMonthPicker = remember { mutableStateOf(false) }
    val startTime = remember { mutableStateOf(LocalTime.of(9, 0)) }
    val endTime = remember { mutableStateOf(LocalTime.of(17, 0)) }
    val showTimePicker = remember { mutableStateOf(false) }
    val isStartTimePicker = remember { mutableStateOf(true) }
    val selectedDaysOfWeek = remember { mutableStateOf(setOf<DayOfWeek>()) }
    val showRecurringOptions = remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(false) }
    val isInitialLoad = remember { mutableStateOf(true) } // Track initial load

    val currentMonth = remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()
    val minMonth = YearMonth.from(today)
    val coroutineScope = rememberCoroutineScope()

    val existingAvailabilities = remember { mutableStateOf<List<DoctorAvailability>>(emptyList()) }

    // Load existing availabilities
    LaunchedEffect(Unit) {
        if (isInitialLoad.value) {
            isLoading.value = true
            try {
                val result = authRepo.getDoctorAvailability(authRepo.getCurrentUserId().toString())
                if (result.isSuccess) {
                    val existingAvailabilities = result.getOrThrow()
                } else {
                    snackbarController.showMessage("Failed to load availabilities")
                }

                if (existingAvailabilities.value.isNotEmpty()) {

                    val timeCounts = existingAvailabilities.value.groupBy {
                        it.startTime to it.endTime
                    }.mapValues { it.value.size }

                    val mostCommonTime = timeCounts.maxByOrNull { it.value }?.key
                    mostCommonTime?.let { (start, end) ->
                        startTime.value = start
                        endTime.value = end
                    }

                    selectedDates.value = existingAvailabilities.value.map { it.date }.toSet() as Set<LocalDate>

                    currentMonth.value = existingAvailabilities.value
                        .filter { it.date != null }
                        .minByOrNull { it.date!! }?.date
                        ?.let { YearMonth.from(it) } ?: YearMonth.now()
                }
            } catch (e: Exception) {
                snackbarController.showMessage("Error loading existing availabilities: ${e.message}")
            } finally {
                isLoading.value = false
                isInitialLoad.value = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header with navigation
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
            Text("Set Availability", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(Modifier.height(16.dp))

        // Month selection row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    val newMonth = currentMonth.value.minusMonths(1)
                    if (newMonth >= minMonth) {
                        currentMonth.value = newMonth
                    }
                },
                modifier = Modifier.size(48.dp),
                enabled = currentMonth.value > minMonth
            ) {
                Icon(Icons.Default.ChevronLeft, "Previous month")
            }

            TextButton(
                onClick = { showMonthPicker.value = true },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    currentMonth.value.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()) +
                            " " + currentMonth.value.year,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            IconButton(
                onClick = { currentMonth.value = currentMonth.value.plusMonths(1) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.ChevronRight, "Next month")
            }
        }


        // Month picker dialog
        if (showMonthPicker.value) {
            MonthYearPickerDialog(
                currentMonth = currentMonth.value,
                onDismiss = { showMonthPicker.value = false },
                onMonthSelected = {
                    currentMonth.value = it
                    showMonthPicker.value = false
                }
            )
        }

        // Weekday headers
        val weekDays = DayOfWeek.entries
        Row(Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Text(
                    day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(1),
                    modifier = Modifier.weight(1f).padding(4.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Calendar grid
        val daysInMonth = currentMonth.value.lengthOfMonth()
        val firstDay = currentMonth.value.atDay(1)
        val offset = (firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value).let {
            if (it < 0) 6 else it // Adjust for Sunday as first day
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(300.dp)
        ) {
            items(offset) { Spacer(Modifier) } // Empty cells for alignment

            // Calendar grid items - modify to disable past dates
            items(daysInMonth) { day ->
                val date = firstDay.plusDays(day.toLong())
                val isSelected = selectedDates.value.contains(date)
                val isPastDate = date.isBefore(today)
                val isCurrentMonth = date.month == currentMonth.value.month

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected -> DefaultPrimary
                                isPastDate -> Color.LightGray.copy(alpha = 0.3f)
                                else -> Color.Transparent
                            }
                        )
                        .border(
                            width = if (date == today) 1.dp else 0.dp,
                            color = DefaultPrimary,
                            shape = CircleShape
                        )
                        .clickable(enabled = !isPastDate && isCurrentMonth) {
                            selectedDates.value = if (isSelected) {
                                selectedDates.value - date
                            } else {
                                selectedDates.value + date
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (day + 1).toString(),
                        color = when {
                            isSelected -> Color.White
                            isPastDate -> Color.Gray
                            !isCurrentMonth -> Color.LightGray
                            else -> DefaultOnPrimary
                        }
                    )
                }
            }
        }

        // Time range selection
        TimeRangePicker(
            startTime = startTime.value,
            endTime = endTime.value,
            onStartTimeClick = {
                isStartTimePicker.value = true
                showTimePicker.value = true
            },
            onEndTimeClick = {
                isStartTimePicker.value = false
                showTimePicker.value = true
            },
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Quick selection buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Weekdays" to { selectWeekdays(currentMonth.value, selectedDates) },
                "This Week" to { selectThisWeek(selectedDates) },
                "Clear" to { selectedDates.value = emptySet() }
            ).forEach { (text, action) ->
                Button(
                    onClick = action,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = DefaultPrimary
                    ),
                    border = BorderStroke(1.dp, DefaultPrimary)
                ) {
                    Text(text)
                }
            }
        }

        // Recurring pattern button
        Button(
            onClick = { showRecurringOptions.value = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = DefaultPrimary
            ),
            border = BorderStroke(1.dp, DefaultPrimary)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Recurring")
            Spacer(Modifier.width(8.dp))
            Text("Set Recurring Pattern")
        }

        // Copy to other months button
        Button(
            onClick = {
                // Copy selected days/time to next 3 months
                val datesToAdd = mutableSetOf<LocalDate>()
                val timeRange = startTime.value..endTime.value

                selectedDates.value.forEach { date ->
                    for (i in 1..3) {
                        val newDate = date.plusMonths(i.toLong())
                        datesToAdd.add(newDate)
                    }
                }

                selectedDates.value = selectedDates.value + datesToAdd
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = DefaultPrimary
            ),
            border = BorderStroke(1.dp, DefaultPrimary)
        ) {
            Icon(Icons.Default.Schedule, contentDescription = "Copy")
            Spacer(Modifier.width(8.dp))
            Text("Copy to Next 3 Months")
        }

        // Save button
        Button(
            onClick = {
                if (selectedDates.value.isNotEmpty()) {
                    coroutineScope.launch {
                        isLoading.value = true
                        try {
                            authRepo.updateAvailabilities(
                                dates = selectedDates.value.toList(),
                                startTime = startTime.value,
                                endTime = endTime.value
                            )
                            snackbarController.showMessage(
                                "Availability updated successfully!"
                            )
                            navController.popBackStack()
                        } catch (e: Exception) {
                            snackbarController.showMessage(
                                "Error updating availability: ${e.message}"
                            )
                        } finally {
                            isLoading.value = false
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(50.dp),
            enabled = selectedDates.value.isNotEmpty() && !isLoading.value,
            colors = ButtonDefaults.buttonColors(
                containerColor = DefaultPrimary,
                contentColor = Color.White
            )
        ) {
            if (isLoading.value) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Updated Availability")
            }
        }
    }

    // Time Picker Dialog
    if (showTimePicker.value) {
        val timePickerState = rememberTimePickerState(
            initialHour = if (isStartTimePicker.value) startTime.value.hour else endTime.value.hour,
            initialMinute = if (isStartTimePicker.value) startTime.value.minute else endTime.value.minute,
            is24Hour = false
        )

        TimePickerDialog(
            onDismissRequest = { showTimePicker.value = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    if (isStartTimePicker.value) {
                        startTime.value = selectedTime
                    } else {
                        endTime.value = selectedTime
                    }
                    showTimePicker.value = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker.value = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Select Time") }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    var durationWeeks by remember { mutableStateOf(4) }

    // Recurring Options Dialog
    if (showRecurringOptions.value) {
        AlertDialog(
            onDismissRequest = { showRecurringOptions.value = false },
            title = { Text("Set Recurring Pattern") },
            text = {
                Column {
                    Text("Select days of the week:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))

                    // Day of week selection
                    val days = DayOfWeek.values()
                    LazyColumn {
                        items(days) { day ->
                            val isSelected = selectedDaysOfWeek.value.contains(day)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedDaysOfWeek.value = if (isSelected) {
                                            selectedDaysOfWeek.value - day
                                        } else {
                                            selectedDaysOfWeek.value + day
                                        }
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        selectedDaysOfWeek.value = if (checked) {
                                            selectedDaysOfWeek.value + day
                                        } else {
                                            selectedDaysOfWeek.value - day
                                        }
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    day.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Duration options
                    Text("Repeat for how many weeks?", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (durationWeeks > 1) durationWeeks-- }) {
                            Icon(Icons.Default.ChevronLeft, "Decrease")
                        }
                        Text("$durationWeeks weeks", modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        IconButton(onClick = { if (durationWeeks < 12) durationWeeks++ }) {
                            Icon(Icons.Default.ChevronRight, "Increase")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Apply the recurring pattern
                        applyRecurringPattern(
                            selectedDaysOfWeek.value,
                            durationWeeks,
                            startTime.value,
                            endTime.value,
                            selectedDates
                        )
                        showRecurringOptions.value = false
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecurringOptions.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun selectWeekdays(month: YearMonth, selectedDates: MutableState<Set<LocalDate>>) {
    val dates = (1..month.lengthOfMonth())
        .map { month.atDay(it) }
        .filter { it.dayOfWeek !in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) }
    selectedDates.value = selectedDates.value + dates
}

fun selectThisWeek(selectedDates: MutableState<Set<LocalDate>>) {
    val today = LocalDate.now()
    val dates = (0..6).map { today.plusDays(it.toLong()) }
    selectedDates.value = selectedDates.value + dates
}

@Composable
fun TimeRangePicker(
    startTime: LocalTime,
    endTime: LocalTime,
    onStartTimeClick: () -> Unit,
    onEndTimeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text("Available Hours", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            TimePickerChip(
                time = startTime,
                label = "From",
                onClick = onStartTimeClick,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(16.dp))

            TimePickerChip(
                time = endTime,
                label = "To",
                onClick = onEndTimeClick,
                modifier = Modifier.weight(1f)
            )
        }

        // Visual time bar
        if (startTime.isBefore(endTime)) {
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { calculateProgress(startTime, endTime) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = DefaultPrimary
            )
        } else if (startTime != endTime) {
            Spacer(Modifier.height(16.dp))
            Text(
                "End time must be after start time",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun TimePickerChip(
    time: LocalTime,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            border = BorderStroke(1.dp, DefaultPrimary.copy(alpha = 0.5f)),
            modifier = Modifier.clickable(onClick = onClick)
        ) {
            Text(
                time.format(DateTimeFormatter.ofPattern("h:mm a")),
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Progress calculation for visual time bar
private fun calculateProgress(start: LocalTime, end: LocalTime): Float {
    val totalMinutes = ChronoUnit.MINUTES.between(start, end)
    val currentMinutes = ChronoUnit.MINUTES.between(start, LocalTime.now())
    return (currentMinutes.toFloat() / totalMinutes.toFloat()).coerceIn(0f, 1f)
}

@Composable
fun MonthYearPickerDialog(
    currentMonth: YearMonth,
    onDismiss: () -> Unit,
    onMonthSelected: (YearMonth) -> Unit
) {
    val months = YearMonth.now().let { current ->
        (0..11).map { current.plusMonths(it.toLong()) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Select Month",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn {
                    items(months) { month ->
                        val isSelected = month == currentMonth
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onMonthSelected(month)
                                    onDismiss()
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = DefaultPrimary
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + month.year,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) DefaultPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

fun applyRecurringPattern(
    selectedDays: Set<DayOfWeek>,
    durationWeeks: Int,
    startTime: LocalTime,
    endTime: LocalTime,
    selectedDates: MutableState<Set<LocalDate>>
) {
    if (selectedDays.isEmpty()) return

    val today = LocalDate.now()
    val newDates = mutableSetOf<LocalDate>()

    for (week in 0 until durationWeeks) {
        selectedDays.forEach { dayOfWeek ->
            // Find next occurrence of this day of week
            var date = today.with(dayOfWeek)
            if (date.isBefore(today)) {
                date = date.plusWeeks(1)
            }
            date = date.plusWeeks(week.toLong())
            newDates.add(date)
        }
    }

    selectedDates.value = selectedDates.value + newDates
}