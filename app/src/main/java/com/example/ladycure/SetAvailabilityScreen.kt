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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TimePickerDialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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
    val existingAvailabilities = remember { mutableStateOf<List<DoctorAvailability>>(emptyList()) }

    val currentMonth = remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()
    val minMonth = YearMonth.from(today)

    val coroutineScope = rememberCoroutineScope()
    val isLoading = remember { mutableStateOf(false) }

    // Load existing availabilities when screen appears
    LaunchedEffect(Unit) {
        isLoading.value = true
        try {
            val result = authRepo.getDoctorAvailability(authRepo.getCurrentUserId().toString())
            if (result.isSuccess) {
                val availabilities = result.getOrThrow()
                existingAvailabilities.value = availabilities
            }
            else {
                snackbarController.showMessage("Error loading existing availabilities")
            }
        } catch (e: Exception) {
            snackbarController.showMessage("Error loading existing availabilities")
        } finally {
            isLoading.value = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultBackground),
    ) {
        // Header with navigation
        // Replace the current header Row with this:
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = DefaultPrimary)
                }
                Text(
                    "Set Availability",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = DefaultPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // New button style
            OutlinedButton(
                onClick = { navController.navigate("availabilityList") },
                border = BorderStroke(1.dp, DefaultPrimary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DefaultPrimary
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "View",
                    tint = DefaultPrimary
                )
                Spacer(Modifier.width(4.dp))
                Text("View", style = MaterialTheme.typography.labelMedium)
            }
        }

        AvailabilityLegend(
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Main content with scroll
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .weight(1f)
        ) {

            // Month selection row
            item{
                CalendarHeader(
                    currentMonth = currentMonth.value,
                    minMonth = minMonth,
                    onMonthChange = { currentMonth.value = it },
                    onShowMonthPicker = { showMonthPicker.value = true },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Calendar grid
            item{
                CalendarView(
                    currentMonth = currentMonth.value,
                    selectedDates = selectedDates.value,
                    existingAvailabilities = existingAvailabilities.value,
                    today = today,
                    onDateSelected = { date ->
                        selectedDates.value = if (selectedDates.value.contains(date)) {
                            selectedDates.value - date
                        } else {
                            selectedDates.value + date
                        }
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Time range selection
            item{
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
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Quick selection buttons
            item{
                QuickSelectionButtons(
                    currentMonth = currentMonth.value,
                    selectedDates = selectedDates,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Recurring pattern button
            item{
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
            }

            // Copy to other months button
            item{
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
            }

        }
        // Save button at bottom
        SaveButton(
            isLoading = isLoading.value,
            enabled = selectedDates.value.isNotEmpty(),
            onClick = {
                coroutineScope.launch {
                    isLoading.value = true
                    try {
                        authRepo.updateAvailabilities(
                            dates = selectedDates.value.toList(),
                            startTime = startTime.value,
                            endTime = endTime.value
                        )
                        snackbarController.showMessage("Availability saved successfully!")
                        // refresh the screen
                        coroutineScope.launch {
                            existingAvailabilities.value = authRepo.getDoctorAvailability(authRepo.getCurrentUserId().toString()).getOrThrow()
                        }
                    } catch (e: Exception) {
                        snackbarController.showMessage("Error saving availability: ${e.message}")
                    } finally {
                        isLoading.value = false
                    }
                }
            },
            modifier = Modifier.padding(16.dp)
        )
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

    // Time Picker Dialog
    if (showTimePicker.value) {
        TimePickerDialog(
            isStartTimePicker = isStartTimePicker.value,
            initialTime = if (isStartTimePicker.value) startTime.value else endTime.value,
            onTimeSelected = { time ->
                if (isStartTimePicker.value) {
                    startTime.value = time
                } else {
                    endTime.value = time
                }
            },
            onDismiss = { showTimePicker.value = false }
        )
    }

    // Recurring Options Dialog
    if (showRecurringOptions.value) {
        RecurringPatternDialog(
            selectedDaysOfWeek = selectedDaysOfWeek.value,
            onApply = { days, weeks ->
                applyRecurringPattern(
                    days,
                    weeks,
                    startTime.value,
                    endTime.value,
                    selectedDates
                )
                showRecurringOptions.value = false
            },
            onDismiss = { showRecurringOptions.value = false }
        )
    }
}

@Composable
private fun AvailabilityLegend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(
            color = DefaultPrimary.copy(alpha = 0.3f),
            text = "Existing",
            modifier = Modifier.weight(1f)
        )
        LegendItem(
            color = DefaultPrimary,
            text = "Selected",
            modifier = Modifier.weight(1f)
        )
        LegendItem(
            color = Color.LightGray.copy(alpha = 0.3f),
            text = "Unavailable",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LegendItem(color: Color, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, DefaultPrimary.copy(alpha = 0.5f), CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun CalendarHeader(
    currentMonth: YearMonth,
    minMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    onShowMonthPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        IconButton(
            onClick = {
                val newMonth = currentMonth.minusMonths(1)
                if (newMonth >= minMonth) onMonthChange(newMonth)
            },
            modifier = Modifier.size(48.dp),
            enabled = currentMonth > minMonth
        ) {
            Icon(Icons.Default.ChevronLeft, "Previous month", tint = DefaultPrimary)
        }

        TextButton(
            onClick = onShowMonthPicker,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                currentMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()) +
                        " " + currentMonth.year,
                style = MaterialTheme.typography.titleMedium.copy(color = DefaultPrimary)
            )
        }

        IconButton(
            onClick = { onMonthChange(currentMonth.plusMonths(1)) },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.ChevronRight, "Next month", tint = DefaultPrimary)
        }
    }

    // Weekday headers
    val weekDays = DayOfWeek.entries
    Row(Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp)) {
        weekDays.forEach { day ->
            Text(
                day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(1),
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall.copy(color = DefaultPrimary)
            )
        }
    }
}

@Composable
private fun CalendarView(
    currentMonth: YearMonth,
    selectedDates: Set<LocalDate>,
    existingAvailabilities: List<DoctorAvailability>,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDay = currentMonth.atDay(1)
    val offset = (firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value).let {
        if (it < 0) 6 else it // Adjust for Sunday as first day
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = modifier.height(280.dp)
    ) {
        items(offset) { Spacer(Modifier) } // Empty cells for alignment

        items(daysInMonth) { day ->
            val date = firstDay.plusDays(day.toLong())
            val isSelected = selectedDates.contains(date)
            val hasExistingAvailability = existingAvailabilities.any { availability ->
                availability.date == date
            }
            val isPastDate = date.isBefore(today)
            val isCurrentMonth = date.month == currentMonth.month
            val isToday = date == today

            val backgroundColor = when {
                isSelected -> DefaultPrimary
                hasExistingAvailability -> DefaultPrimary.copy(alpha = 0.3f)
                isPastDate -> Color.LightGray.copy(alpha = 0.3f)
                else -> Color.Transparent
            }

            val borderColor = when {
                isToday -> DefaultPrimary
                hasExistingAvailability -> DefaultPrimary.copy(alpha = 0.5f)
                else -> Color.Transparent
            }

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .border(
                        width = if (borderColor != Color.Transparent) 1.dp else 0.dp,
                        color = borderColor,
                        shape = CircleShape
                    )
                    .clickable(enabled = !isPastDate && isCurrentMonth) {
                        onDateSelected(date)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = (day + 1).toString(),
                        color = when {
                            isSelected -> Color.White
                            isPastDate -> Color.Gray
                            !isCurrentMonth -> Color.LightGray
                            else -> DefaultOnPrimary
                        }
                    )

                    // Show small indicator if there's existing availability
                    if (hasExistingAvailability && !isSelected) {
                        Spacer(Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(DefaultPrimary)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun QuickSelectionButtons(
    currentMonth: YearMonth,
    selectedDates: MutableState<Set<LocalDate>>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            "Weekdays" to { selectWeekdays(currentMonth, selectedDates) },
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
}

@Composable
private fun SaveButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = DefaultPrimary,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text("Save Availability", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    isStartTimePicker: Boolean,
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = false
    )

    TimePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                onTimeSelected(selectedTime)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text(if (isStartTimePicker) "Select Start Time" else "Select End Time") }
    ) {
        TimePicker(state = timePickerState)
    }
}

@Composable
private fun RecurringPatternDialog(
    selectedDaysOfWeek: Set<DayOfWeek>,
    onApply: (Set<DayOfWeek>, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var durationWeeks by remember { mutableStateOf(4) }
    val tempSelectedDays = remember { mutableStateOf(selectedDaysOfWeek.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Set Recurring Pattern",
                style = MaterialTheme.typography.titleMedium.copy(color = DefaultPrimary)
            )
        },
        text = {
            Column {
                Text(
                    "Select days of the week:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))

                // Day of week selection
                val days = DayOfWeek.values()
                LazyColumn {
                    items(days) { day ->
                        val isSelected = tempSelectedDays.value.contains(day)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    tempSelectedDays.value = if (isSelected) {
                                        tempSelectedDays.value - day
                                    } else {
                                        tempSelectedDays.value + day
                                    }
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    tempSelectedDays.value = if (checked) {
                                        tempSelectedDays.value + day
                                    } else {
                                        tempSelectedDays.value - day
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = DefaultPrimary,
                                    uncheckedColor = DefaultPrimary.copy(alpha = 0.6f)
                                )
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
                    IconButton(
                        onClick = { if (durationWeeks > 1) durationWeeks-- },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = DefaultPrimary
                        )
                    ) {
                        Icon(Icons.Default.ChevronLeft, "Decrease")
                    }
                    Text(
                        "$durationWeeks weeks",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    IconButton(
                        onClick = { if (durationWeeks < 12) durationWeeks++ },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = DefaultPrimary
                        )
                    ) {
                        Icon(Icons.Default.ChevronRight, "Increase")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(tempSelectedDays.value, durationWeeks) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultPrimary,
                    contentColor = Color.White
                )
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = DefaultPrimary
                )
            ) {
                Text("Cancel")
            }
        }
    )
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
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Available Hours", style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TimePickerChip(
                time = startTime,
                label = "From",
                onClick = onStartTimeClick,
                modifier = Modifier
            )

            TimePickerChip(
                time = endTime,
                label = "To",
                onClick = onEndTimeClick,
                modifier = Modifier
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
    Row(modifier, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.width(10.dp))
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
