package com.example.ladycure.screens.doctor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavHostController
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import com.example.ladycure.repository.AuthRepository
import java.time.YearMonth
import java.util.Locale
import DefaultPrimary
import DefaultBackground
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TimePickerDialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.font.FontWeight
import com.example.ladycure.data.doctor.DoctorAvailability
import com.example.ladycure.utility.SnackbarController
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle

import com.example.ladycure.presentation.availability.CalendarView
import com.example.ladycure.presentation.availability.CalendarHeader
import com.example.ladycure.presentation.availability.AvailabilityLegend
import com.example.ladycure.presentation.availability.RecurringPatternDialog
import com.example.ladycure.presentation.availability.TimeRangePicker
import com.example.ladycure.presentation.availability.QuickSelectionButtons
import com.example.ladycure.presentation.availability.SaveButton


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
                showTimePicker.value = false
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
