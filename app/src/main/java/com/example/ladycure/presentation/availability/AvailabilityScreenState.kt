package com.example.ladycure.presentation.availability

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.ladycure.domain.model.DoctorAvailability
import com.example.ladycure.presentation.availability.components.AvailabilityLegend
import com.example.ladycure.presentation.availability.components.CalendarHeader
import com.example.ladycure.presentation.availability.components.CalendarView
import com.example.ladycure.presentation.availability.components.QuickSelectionButtons
import com.example.ladycure.presentation.availability.components.RecurringPatternDialog
import com.example.ladycure.presentation.availability.components.SaveButton
import com.example.ladycure.presentation.availability.components.TimeRangePicker
import com.example.ladycure.ui.theme.DefaultBackground
import com.example.ladycure.ui.theme.DefaultOnPrimary
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.utility.SnackbarController
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale


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

    val internalSelectedDates = remember { mutableStateOf(state.selectedDates) }

    LaunchedEffect(state.selectedDates) {
        internalSelectedDates.value = state.selectedDates
    }

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

        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

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
                            onStateChange(
                                state.copy(
                                    isStartTimePicker = true,
                                    showTimePicker = true
                                )
                            )
                        },
                        onEndTimeClick = {
                            onStateChange(
                                state.copy(
                                    isStartTimePicker = false,
                                    showTimePicker = true
                                )
                            )
                        },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            item {
                QuickSelectionButtons(
                    currentMonth = state.currentMonth,
                    selectedDates = internalSelectedDates,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Button(
                    onClick = { onStateChange(state.copy(showRecurringOptions = true)) },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DefaultPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Recurring")
                    Spacer(Modifier.width(8.dp))
                    Text("Set Recurring Pattern", style = MaterialTheme.typography.labelLarge)
                }
            }

            item {
                Button(
                    onClick = {
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
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = "Copy")
                    Spacer(Modifier.width(8.dp))
                    Text("Copy to Next 3 Months", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        SaveButton(
            isLoading = state.isLoading,
            enabled = state.selectedDates.isNotEmpty(),
            onClick = {
                coroutineScope.launch {
                    onStateChange(state.copy(isLoading = true))
                    try {
                        onSave(state.selectedDates, state.startTime, state.endTime)
                        snackbarController.showMessage("Availability saved successfully!")
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

    if (state.showMonthPicker) {
        MonthYearPickerDialog(
            currentMonth = state.currentMonth,
            onDismiss = { onStateChange(state.copy(showMonthPicker = false)) },
            onMonthSelected = {
                onStateChange(state.copy(currentMonth = it, showMonthPicker = false))
            }
        )
    }

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
    adminEditingDoctorId: String? = null,
    viewModel: SetAvailabilityViewModel = hiltViewModel()
) {
    val effectiveDoctorId = adminEditingDoctorId ?: viewModel.getCurrentUserId().toString()

    LaunchedEffect(effectiveDoctorId) {
        if (effectiveDoctorId == "null" || effectiveDoctorId.isBlank()) {
            snackbarController.showMessage("Doctor ID is missing for availability management.")
            return@LaunchedEffect
        }
        viewModel.loadAvailabilities(effectiveDoctorId)
    }

    LaunchedEffect(viewModel.error) {
        viewModel.error?.let { snackbarController.showMessage(it); viewModel.clearError() }
    }

    LaunchedEffect(viewModel.saveSuccess) {
        viewModel.saveSuccess?.let { snackbarController.showMessage(it); viewModel.clearSaveSuccess() }
    }

    BaseAvailabilityScreen(
        navController = navController,
        snackbarController = snackbarController,
        state = viewModel.state,
        onStateChange = { viewModel.updateState(it) },
        onSave = { dates, startTime, endTime ->
            viewModel.saveAvailabilities(effectiveDoctorId, dates, startTime, endTime)
        },
        headerTitle = if (adminEditingDoctorId != null) "Edit Doctor Availability" else "Set Availability",
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
    viewModel: SetAvailabilityViewModel = hiltViewModel()
) {
    LaunchedEffect(doctorId) {
        if (doctorId.isBlank()) {
            snackbarController.showMessage("Doctor ID is missing for availability management.")
            return@LaunchedEffect
        }
        viewModel.loadAvailabilities(doctorId)
    }

    LaunchedEffect(viewModel.error) {
        viewModel.error?.let { snackbarController.showMessage(it); viewModel.clearError() }
    }

    LaunchedEffect(viewModel.saveSuccess) {
        viewModel.saveSuccess?.let { snackbarController.showMessage(it); viewModel.clearSaveSuccess() }
    }

    BaseAvailabilityScreen(
        navController = navController,
        snackbarController = snackbarController,
        state = viewModel.state,
        onStateChange = { viewModel.updateState(it) },
        onSave = { dates, startTime, endTime ->
            viewModel.saveAvailabilities(doctorId, dates, startTime, endTime)
        },
        headerTitle = "Edit Doctor Avail.",
        onViewAvailabilityClick = {
            navController.navigate("adminAvailabilityList/$doctorId")
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