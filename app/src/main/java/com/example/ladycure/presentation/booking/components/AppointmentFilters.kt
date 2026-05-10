package com.example.ladycure.presentation.booking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ladycure.ui.theme.DefaultOnPrimary
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.FractionDimens
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun EnhancedFiltersSection(
    role: String,
    viewModel: AppointmentViewModel,
    modifier: Modifier = Modifier
) {
    val dimens = rememberResponsiveDimens()
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.8f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dimens.w(FractionDimens.paddingSmallW),
                            vertical = dimens.h(FractionDimens.paddingSmallH)
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Filter Appointments",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DefaultOnPrimary
                        )
                    )

                    IconButton(
                        onClick = { viewModel.clearAllFilters() },
                        modifier = Modifier.size(dimens.w(FractionDimens.paddingMediumW))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = "Clear Filters",
                            tint = DefaultOnPrimary.copy(alpha = 0.8f),
                        )
                    }
                }

                Divider(color = DefaultPrimary.copy(alpha = 0.3f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dimens.w(FractionDimens.paddingSmallW),
                            vertical = dimens.h(FractionDimens.paddingSmallH)
                        ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (role == "user") {
                        MultiSelectFilterSection(
                            title = "Specialization",
                            items = viewModel.allSpecializations,
                            selectedItems = viewModel.selectedSpecializations,
                            onItemSelected = { viewModel.toggleSpecializationFilter(it) }
                        )

                        MultiSelectFilterSection(
                            title = "Doctor",
                            items = viewModel.allDoctors,
                            selectedItems = viewModel.selectedDoctors,
                            onItemSelected = { viewModel.toggleDoctorFilter(it) }
                        )
                    } else {
                        MultiSelectFilterSection(
                            title = "Appointment Type",
                            items = viewModel.allTypes.map { it.displayName },
                            selectedItems = viewModel.selectedTypes.map { it.displayName },
                            onItemSelected = { displayName ->
                                viewModel.toggleTypeFilter(
                                    viewModel.allTypes.first { it.displayName == displayName }
                                )
                            }
                        )

                        MultiSelectFilterSection(
                            title = "Patient",
                            items = viewModel.allPatients,
                            selectedItems = viewModel.selectedPatients,
                            onItemSelected = { viewModel.togglePatientFilter(it) }
                        )
                    }

                    DateFilterSection(
                        selectedDate = viewModel.selectedDate,
                        onDateSelected = { viewModel.setDateFilter(it) },
                        onDateCleared = { viewModel.setDateFilter(null) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MultiSelectFilterSection(
    title: String,
    items: List<String>,
    selectedItems: List<String>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium,
                color = DefaultOnPrimary.copy(alpha = 0.8f)
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items.forEach { item ->
                EnhancedFilterChip(
                    label = item,
                    selected = selectedItems.contains(item),
                    onSelected = { onItemSelected(item) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterSection(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDateCleared: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Date",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium,
                color = DefaultOnPrimary.copy(alpha = 0.8f)
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val today = LocalDate.now()
            val tomorrow = today.plusDays(1)

            EnhancedFilterChip(
                label = "Today",
                selected = selectedDate == today,
                onSelected = {
                    if (selectedDate == today) {
                        onDateCleared()
                    } else {
                        onDateSelected(today)
                    }
                }
            )

            EnhancedFilterChip(
                label = "Tomorrow",
                selected = selectedDate == tomorrow,
                onSelected = {
                    if (selectedDate == tomorrow) {
                        onDateCleared()
                    } else {
                        onDateSelected(tomorrow)
                    }
                }
            )

            var showDatePicker by remember { mutableStateOf(false) }

            EnhancedFilterChip(
                label = "Custom Date",
                selected = selectedDate != null && selectedDate != today && selectedDate != tomorrow,
                onSelected = { showDatePicker = true }
            )

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = selectedDate?.atStartOfDay()
                        ?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
                )

                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    val selected =
                                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())
                                            .toLocalDate()
                                    onDateSelected(selected)
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text("OK", color = DefaultPrimary)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDatePicker = false }
                        ) {
                            Text("Cancel", color = DefaultPrimary)
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }
    }
}

@Composable
fun EnhancedFilterChip(
    label: String,
    selected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = rememberResponsiveDimens()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) DefaultPrimary else Color.White,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) DefaultPrimary else DefaultPrimary.copy(alpha = 0.3f)
        ),
        shadowElevation = if (selected) 2.dp else 0.dp,
        onClick = onSelected
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = dimens.w(FractionDimens.spacingTinyW),
                vertical = 8.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) Color.White else DefaultPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ActiveFiltersRow(
    selectedSpecializations: List<String>,
    selectedDoctors: List<String>,
    selectedTypes: List<String>,
    selectedPatients: List<String>,
    selectedDate: LocalDate?,
    onRemoveSpecialization: (String) -> Unit,
    onRemoveDoctor: (String) -> Unit,
    onRemoveType: (String) -> Unit,
    onRemovePatient: (String) -> Unit,
    onRemoveDate: () -> Unit,
    role: String,
) {
    val dimens = rememberResponsiveDimens()
    val hasFilters = selectedSpecializations.isNotEmpty() ||
            selectedDoctors.isNotEmpty() ||
            selectedTypes.isNotEmpty() ||
            selectedPatients.isNotEmpty() ||
            selectedDate != null

    AnimatedVisibility(
        visible = hasFilters,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.w(FractionDimens.paddingSmallW), vertical = 8.dp)
        ) {
            Text(
                text = "Filters:",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = DefaultOnPrimary.copy(alpha = 0.8f)
                ),
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            if (role == "user") {
                selectedSpecializations.forEach { specialization ->
                    ActiveFilterChip(
                        label = specialization,
                        onRemove = { onRemoveSpecialization(specialization) }
                    )
                }

                selectedDoctors.forEach { doctor ->
                    ActiveFilterChip(
                        label = doctor,
                        onRemove = { onRemoveDoctor(doctor) }
                    )
                }
            } else {
                selectedTypes.forEach { type ->
                    ActiveFilterChip(
                        label = type,
                        onRemove = { onRemoveType(type) }
                    )
                }

                selectedPatients.forEach { patient ->
                    ActiveFilterChip(
                        label = patient,
                        onRemove = { onRemovePatient(patient) }
                    )
                }
            }

            selectedDate?.let {
                val dateLabel = when (it) {
                    LocalDate.now() -> "Today"
                    LocalDate.now().plusDays(1) -> "Tomorrow"
                    else -> it.format(DateTimeFormatter.ofPattern("MMM d"))
                }
                ActiveFilterChip(
                    label = dateLabel,
                    onRemove = onRemoveDate
                )
            }
        }
    }
}

@Composable
fun ActiveFilterChip(
    label: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = rememberResponsiveDimens()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = DefaultPrimary.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, DefaultPrimary.copy(alpha = 0.3f)),
        onClick = onRemove
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = DefaultPrimary,
                modifier = Modifier.padding(
                    start = dimens.w(FractionDimens.spacingTinyW),
                    top = 4.dp,
                    bottom = 4.dp,
                    end = 4.dp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove filter",
                tint = DefaultPrimary,
                modifier = Modifier
                    .size(16.dp)
                    .padding(4.dp)
            )
        }
    }
}
