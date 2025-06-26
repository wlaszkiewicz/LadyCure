package com.example.ladycure.presentation.availability

import DefaultBackground
import DefaultPrimary
import DefaultOnPrimary
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.ladycure.data.repository.AuthRepository
import com.example.ladycure.data.repository.DoctorRepository
import com.example.ladycure.domain.model.DoctorAvailability
import com.example.ladycure.presentation.availability.components.AvailabilityLegend
import com.example.ladycure.presentation.availability.components.CalendarHeader
import com.example.ladycure.presentation.availability.components.CalendarView
import com.example.ladycure.presentation.availability.components.QuickSelectionButtons
import com.example.ladycure.presentation.availability.components.RecurringPatternDialog
import com.example.ladycure.presentation.availability.components.SaveButton
import com.example.ladycure.presentation.availability.components.TimeRangePicker
import com.example.ladycure.utility.SnackbarController
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

// Data class to hold all the state for the availability screens
data class AvailabilityScreenState(
    val selectedDates: Set<LocalDate> = emptySet(),
    val showMonthPicker: Boolean = false,
    val startTime: LocalTime = LocalTime.of(9, 0),
    val endTime: LocalTime = LocalTime.of(17, 0),
    val showTimePicker: Boolean = false,
    val isStartTimePicker: Boolean = true,
    val selectedDaysOfWeek: Set<DayOfWeek> = emptySet(),
    val showRecurringOptions: Boolean = false,
    val existingAvailabilities: List<DoctorAvailability> = emptyList(),
    val currentMonth: YearMonth = YearMonth.now(),
    val isLoading: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseAvailabilityScreen(
    navController: NavHostController,
    snackbarController: SnackbarController,
    state: AvailabilityScreenState,
    onStateChange: (AvailabilityScreenState) -> Unit,
    onSave: (dates: Set<LocalDate>, startTime: LocalTime, endTime: LocalTime) -> Unit,
    headerTitle: String,
    onViewAvailabilityClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val today = LocalDate.now()
    val minMonth = YearMonth.from(today)

    // A mutable state for selectedDates that can be passed to children components.
    // This state will be kept in sync with the parent's `state.selectedDates`.
    val internalSelectedDates = remember { mutableStateOf(state.selectedDates) }

    // Use LaunchedEffect to update internalSelectedDates when the parent's state.selectedDates changes
    LaunchedEffect(state.selectedDates) {
        internalSelectedDates.value = state.selectedDates
    }

    // Use LaunchedEffect to update the parent's state when internalSelectedDates changes
    LaunchedEffect(internalSelectedDates.value) {
        if (internalSelectedDates.value != state.selectedDates) {
            onStateChange(state.copy(selectedDates = internalSelectedDates.value))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultBackground),
    ) {
        // Header with navigation
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DefaultBackground,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DefaultPrimary)
                    }
                    Text(
                        headerTitle,
                        style = MaterialTheme.typography.headlineMedium.copy( // Changed from headlineLarge to headlineMedium
                            color = DefaultPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // New button style
                OutlinedButton(
                    onClick = onViewAvailabilityClick,
                    border = BorderStroke(1.dp, DefaultPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DefaultPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "View",
                        tint = DefaultPrimary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("View", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        AvailabilityLegend(
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        )

        // Main content with scroll
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Consolidated Calendar card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White,
                        contentColor = DefaultOnPrimary
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) { // Added Column inside Card
                        CalendarHeader(
                            currentMonth = state.currentMonth,
                            minMonth = minMonth,
                            onMonthChange = { newMonth -> onStateChange(state.copy(currentMonth = newMonth)) },
                            onShowMonthPicker = { onStateChange(state.copy(showMonthPicker = true)) },
                            modifier = Modifier.fillMaxWidth() // Modifier applied to Header
                        )
                        Spacer(Modifier.height(8.dp)) // Spacer between header and view
                        CalendarView(
                            currentMonth = state.currentMonth,
                            selectedDates = state.selectedDates, // Pass immutable set
                            existingAvailabilities = state.existingAvailabilities,
                            today = today,
                            onDateSelected = { date ->
                                val newSelectedDates = if (state.selectedDates.contains(date)) {
                                    state.selectedDates - date
                                } else {
                                    state.selectedDates + date
                                }
                                onStateChange(state.copy(selectedDates = newSelectedDates))
                            },
                            modifier = Modifier.fillMaxWidth() // Modifier applied to View
                        )
                    }
                }
            }

            // Time range selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White,
                        contentColor = DefaultOnPrimary
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    TimeRangePicker(
                        startTime = state.startTime,
                        endTime = state.endTime,
                        onStartTimeClick = {
                            onStateChange(state.copy(isStartTimePicker = true, showTimePicker = true))
                        },
                        onEndTimeClick = {
                            onStateChange(state.copy(isStartTimePicker = false, showTimePicker = true))
                        },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Quick selection buttons
            item {
                QuickSelectionButtons(
                    currentMonth = state.currentMonth,
                    selectedDates = internalSelectedDates, // Pass the mutable state directly
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Recurring pattern button
            item {
                Button(
                    onClick = { onStateChange(state.copy(showRecurringOptions = true)) },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DefaultPrimary,
                        contentColor = DefaultOnPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Recurring")
                    Spacer(Modifier.width(8.dp))
                    Text("Set Recurring Pattern", style = MaterialTheme.typography.labelLarge)
                }
            }

            // Copy to other months button
            item {
                Button(
                    onClick = {
                        // Copy selected days/time to next 3 months
                        val datesToAdd = mutableSetOf<LocalDate>()

                        state.selectedDates.forEach { date ->
                            for (i in 1..3) {
                                val newDate = date.plusMonths(i.toLong())
                                datesToAdd.add(newDate)
                            }
                        }

                        onStateChange(state.copy(selectedDates = state.selectedDates + datesToAdd))
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DefaultPrimary.copy(alpha = 0.8f),
                        contentColor = DefaultOnPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = "Copy")
                    Spacer(Modifier.width(8.dp))
                    Text("Copy to Next 3 Months", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        // Save button at bottom
        SaveButton(
            isLoading = state.isLoading,
            enabled = state.selectedDates.isNotEmpty(),
            onClick = {
                coroutineScope.launch {
                    onStateChange(state.copy(isLoading = true))
                    try {
                        onSave(state.selectedDates, state.startTime, state.endTime)
                        snackbarController.showMessage("Availability saved successfully!")
                        // Refresh the screen after saving will be handled by the specific screen (admin or doctor)
                    } catch (e: Exception) {
                        snackbarController.showMessage("Error saving availability: ${e.message}")
                    } finally {
                        onStateChange(state.copy(isLoading = false))
                    }
                }
            },
            modifier = Modifier.padding(16.dp)
        )
    }

    // Month picker dialog
    if (state.showMonthPicker) {
        MonthYearPickerDialog(
            currentMonth = state.currentMonth,
            onDismiss = { onStateChange(state.copy(showMonthPicker = false)) },
            onMonthSelected = {
                onStateChange(state.copy(currentMonth = it, showMonthPicker = false))
            }
        )
    }

    // Time Picker Dialog
    if (state.showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = if (state.isStartTimePicker) state.startTime.hour else state.endTime.hour,
            initialMinute = if (state.isStartTimePicker) state.startTime.minute else state.endTime.minute,
            is24Hour = false
        )

        androidx.compose.material3.TimePickerDialog(
            onDismissRequest = { onStateChange(state.copy(showTimePicker = false)) },
            confirmButton = {
                TextButton(onClick = {
                    val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    if (state.isStartTimePicker) {
                        onStateChange(state.copy(startTime = selectedTime, showTimePicker = false))
                    } else {
                        onStateChange(state.copy(endTime = selectedTime, showTimePicker = false))
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { onStateChange(state.copy(showTimePicker = false)) }) {
                    Text("Cancel")
                }
            },
            title = { Text(if (state.isStartTimePicker) "Select Start Time" else "Select End Time") }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    // Recurring Options Dialog
    if (state.showRecurringOptions) {
        RecurringPatternDialog(
            selectedDaysOfWeek = state.selectedDaysOfWeek,
            onApply = { days, weeks ->
                applyRecurringPattern(
                    days,
                    weeks,
                    internalSelectedDates // Pass the mutable state directly
                )
                onStateChange(state.copy(selectedDaysOfWeek = days, showRecurringOptions = false))
            },
            onDismiss = { onStateChange(state.copy(showRecurringOptions = false)) }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetAvailabilityScreen(
    navController: NavHostController,
    snackbarController: SnackbarController,
    authRepo: AuthRepository = AuthRepository(),
    doctorRepo: DoctorRepository = DoctorRepository(),
    adminEditingDoctorId: String? = null // Add this new optional parameter
) {
    val state = remember { mutableStateOf(AvailabilityScreenState()) }
    val coroutineScope = rememberCoroutineScope()

    // Determine the effective doctor ID for loading/saving
    val effectiveDoctorId = adminEditingDoctorId ?: authRepo.getCurrentUserId().toString()

    // Load existing availabilities when screen appears
    LaunchedEffect(effectiveDoctorId) { // Relaunch if effectiveDoctorId changes
        if (effectiveDoctorId == "null" || effectiveDoctorId.isBlank()) { // Handle null and blank string case
            snackbarController.showMessage("Doctor ID is missing for availability management.")
            return@LaunchedEffect
        }
        state.value = state.value.copy(isLoading = true)
        try {
            val result = doctorRepo.getDoctorAvailability(effectiveDoctorId) // Use effectiveDoctorId
            if (result.isSuccess) {
                val availabilities = result.getOrThrow()
                val today = LocalDate.now()

                // Filter out past availabilities
                val pastAvailabilities = availabilities.filter { it.date?.isBefore(today) == true }
                val futureAvailabilities = availabilities.filter { it.date?.isBefore(today) == false }

                // Delete past availabilities from Firestore
                pastAvailabilities.forEach { pastAvailability ->
                    doctorRepo.deleteDoctorAvailability(
                        pastAvailability.doctorId,
                        pastAvailability.date!!
                    )
                }

                state.value = state.value.copy(existingAvailabilities = futureAvailabilities)
            } else {
                snackbarController.showMessage("Error loading existing availabilities")
            }
        } catch (e: Exception) {
            snackbarController.showMessage("Error loading existing availabilities: ${e.message}")
        } finally {
            state.value = state.value.copy(isLoading = false)
        }
    }

    BaseAvailabilityScreen(
        navController = navController,
        snackbarController = snackbarController,
        state = state.value,
        onStateChange = { newState -> state.value = newState },
        onSave = { dates, startTime, endTime ->
            coroutineScope.launch {
                state.value = state.value.copy(isLoading = true) // Set loading state for save operation
                try {
                    doctorRepo.updateAvailabilities(
                        dates = dates.toList(),
                        startTime = startTime,
                        endTime = endTime,
                        doctorId = effectiveDoctorId // Use effectiveDoctorId for saving
                    )
                    snackbarController.showMessage("Availability saved successfully!")
                    // Refresh the screen after saving for the current user or admin-edited doctor
                    val newAvailabilities = doctorRepo.getDoctorAvailability(effectiveDoctorId).getOrThrow()
                    state.value = state.value.copy(existingAvailabilities = newAvailabilities)
                } catch (e: Exception) {
                    snackbarController.showMessage("Error saving availability: ${e.message}")
                } finally {
                    state.value = state.value.copy(isLoading = false) // Reset loading state
                }
            }
        },
        headerTitle = if (adminEditingDoctorId != null) "Edit Doctor Availability" else "Set Availability", // Dynamic header
        onViewAvailabilityClick = {
            if (adminEditingDoctorId != null) {
                navController.navigate("adminAvailabilityList/$effectiveDoctorId")
            } else {
                navController.navigate("availabilityList")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun SetAvailabilityScreenAdmin(
    navController: NavHostController,
    snackbarController: SnackbarController,
    doctorId: String,
    doctorRepo: DoctorRepository = DoctorRepository()
) {
    val state = remember { mutableStateOf(AvailabilityScreenState()) }
    val coroutineScope = rememberCoroutineScope()

    // Use the passed doctorId directly
    val effectiveDoctorId = doctorId

    // Load existing availabilities when screen appears
    LaunchedEffect(effectiveDoctorId) {
        if (effectiveDoctorId.isBlank()) {
            snackbarController.showMessage("Doctor ID is missing for availability management.")
            return@LaunchedEffect
        }

        state.value = state.value.copy(isLoading = true)
        try {
            val result = doctorRepo.getDoctorAvailability(effectiveDoctorId)
            if (result.isSuccess) {
                val availabilities = result.getOrThrow()
                val today = LocalDate.now()

                // Filter out past availabilities
                val pastAvailabilities = availabilities.filter { it.date?.isBefore(today) == true }
                val futureAvailabilities = availabilities.filter { it.date?.isBefore(today) == false }

                // Delete past availabilities from Firestore
                pastAvailabilities.forEach { pastAvailability ->
                    doctorRepo.deleteDoctorAvailability(
                        pastAvailability.doctorId,
                        pastAvailability.date!!
                    )
                }

                state.value = state.value.copy(existingAvailabilities = futureAvailabilities)
            } else {
                snackbarController.showMessage("Error loading existing availabilities")
            }
        } catch (e: Exception) {
            snackbarController.showMessage("Error loading existing availabilities: ${e.message}")
        } finally {
            state.value = state.value.copy(isLoading = false)
        }
    }

    BaseAvailabilityScreen(
        navController = navController,
        snackbarController = snackbarController,
        state = state.value,
        onStateChange = { newState -> state.value = newState },
        onSave = { dates, startTime, endTime ->
            coroutineScope.launch {
                state.value = state.value.copy(isLoading = true)
                try {
                    doctorRepo.updateAvailabilities(
                        dates = dates.toList(),
                        startTime = startTime,
                        endTime = endTime,
                        doctorId = effectiveDoctorId // Use the passed doctorId
                    )
                    snackbarController.showMessage("Availability saved successfully!")
                    // Refresh the screen after saving
                    val newAvailabilities = doctorRepo.getDoctorAvailability(effectiveDoctorId).getOrThrow()
                    state.value = state.value.copy(existingAvailabilities = newAvailabilities)
                } catch (e: Exception) {
                    snackbarController.showMessage("Error saving availability: ${e.message}")
                } finally {
                    state.value = state.value.copy(isLoading = false)
                }
            }
        },
        headerTitle = "Edit Doctor Avail.",
        onViewAvailabilityClick = {
            navController.navigate("adminAvailabilityList/$effectiveDoctorId")
        }
    )
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

    androidx.compose.material3.TimePickerDialog(
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
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            color = DefaultBackground
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Select Month",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = DefaultPrimary,
                        fontWeight = FontWeight.Bold
                    ),
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
                                month.month.getDisplayName(
                                    TextStyle.FULL,
                                    Locale.getDefault()
                                ) + " " + month.year,
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
    selectedDates: MutableState<Set<LocalDate>>
) {
    if (selectedDays.isEmpty()) return

    val today = LocalDate.now()
    val newDates = mutableSetOf<LocalDate>()

    for (week in 0 until durationWeeks) {
        selectedDays.forEach { dayOfWeek ->
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