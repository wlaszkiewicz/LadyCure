package com.example.ladycure.presentation.booking.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Text
import com.example.ladycure.domain.model.AppointmentType
import com.example.ladycure.domain.model.Speciality
import com.example.ladycure.presentation.booking.LocationSpecialtyRow
import com.example.ladycure.presentation.booking.ServiceInfoChip
import com.example.ladycure.ui.theme.DefaultOnPrimary
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun DateAndTimeSelectionView(
    city: String? = null,
    selectedSpeciality: Speciality? = null,
    selectedService: AppointmentType,
    availableDates: List<LocalDate>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    timeSlots: List<String>,
    selectedTimeSlot: LocalTime?,
    onTimeSlotSelected: (String) -> Unit
) {
    val dimens = rememberResponsiveDimens()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.w(16 / 411f), vertical = dimens.h(16 / 914f))
    ) {
        ServiceInfoChip(selectedService, modifier = Modifier.padding(bottom = dimens.h(16 / 914f)))

        if (city != null && selectedSpeciality != null) {
            LocationSpecialtyRow(city, selectedSpeciality)
        }

        Text(
            text = "Select Date",
            style = MaterialTheme.typography.titleMedium,
            color = DefaultOnPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = dimens.h(12 / 914f), top = 8.dp)
        )

        DateSelector(
            availableDates = availableDates,
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
            modifier = Modifier.padding(bottom = dimens.h(24 / 914f))
        )

        if (selectedDate != null) {
            Text(
                text = "Available Time Slots",
                style = MaterialTheme.typography.titleMedium,
                color = DefaultOnPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = dimens.h(12 / 914f))
            )

            if (timeSlots.isEmpty()) {
                EmptyTimeSlotsView()
            } else {
                TimeSlotGrid(
                    timeSlots = timeSlots,
                    selectedTimeSlot = selectedTimeSlot?.format(
                        DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
                    ),
                    onTimeSlotSelected = onTimeSlotSelected
                )
            }
        } else if (availableDates.isNotEmpty()) {
            PromptToSelectDate()
        }
    }
}

@Composable
private fun DateSelector(
    availableDates: List<LocalDate>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = rememberResponsiveDimens()
    val scrollState = rememberScrollState()

    Column(modifier = modifier) {
        if (availableDates.isEmpty()) {
            Text(
                text = "We are sorry, there's no available dates",
                style = MaterialTheme.typography.bodyMedium,
                color = DefaultOnPrimary.copy(alpha = 0.9f),
                modifier = Modifier.padding(vertical = dimens.h(16 / 914f))
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
                        modifier = Modifier.width(dimens.w(80 / 411f))
                    )
                }
            }
        }
    }
}

@Composable
fun DateCard(
    date: LocalDate,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = rememberResponsiveDimens()
    val formattedDate = formatDateForDisplay(date)
    val dayOfWeek = try {
        date.dayOfWeek.toString().take(3)
    } catch (e: Exception) {
        "error: ${e.message}"
    }
    val dayOfMonth = try {
        date.dayOfMonth.toString()
    } catch (e: Exception) {
        "error: ${e.message}"
    }

    Card(
        onClick = onSelect,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DefaultPrimary else Color.White,
            contentColor = if (isSelected) Color.White else DefaultOnPrimary
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 2.dp),
        border = if (!isSelected) BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)) else null
    ) {
        Column(
            modifier = Modifier
                .padding(dimens.w(12 / 411f))
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayOfWeek,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Normal,
                color = if (isSelected) Color.White.copy(alpha = 0.9f) else DefaultOnPrimary.copy(
                    alpha = 0.7f
                )
            )
            Text(
                text = dayOfMonth,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White.copy(alpha = 0.9f) else DefaultOnPrimary.copy(
                    alpha = 0.7f
                )
            )
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) Color.White.copy(alpha = 0.8f) else DefaultOnPrimary.copy(
                    alpha = 0.5f
                )
            )
        }
    }
}

@Composable
fun TimeSlotGrid(
    timeSlots: List<String>,
    selectedTimeSlot: String?,
    onTimeSlotSelected: (String) -> Unit
) {
    val dimens = rememberResponsiveDimens()
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxHeight()
            .padding(bottom = dimens.h(30 / 914f)),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(timeSlots.size) { index ->
            val slot = timeSlots[index]
            TimeSlotCard(
                time = slot,
                isSelected = slot == selectedTimeSlot,
                onSelect = { onTimeSlotSelected(slot) }
            )
        }
    }
}

@Composable
fun TimeSlotCard(
    time: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val dimens = rememberResponsiveDimens()
    Card(
        onClick = onSelect,
        modifier = Modifier.height(dimens.h(60 / 914f)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DefaultPrimary else Color.White,
            contentColor = if (isSelected) Color.White else DefaultOnPrimary
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 2.dp),
        border = if (!isSelected) BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)) else null
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = time,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else DefaultOnPrimary,
            )
        }
    }
}

@Composable
fun EmptyTimeSlotsView() {
    val dimens = rememberResponsiveDimens()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimens.h(16 / 914f)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = "No slots",
            tint = DefaultOnPrimary.copy(alpha = 0.4f),
            modifier = Modifier.size(dimens.w(48 / 411f))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No available time slots",
            style = MaterialTheme.typography.bodyMedium,
            color = DefaultOnPrimary.copy(alpha = 0.6f)
        )
        Text(
            text = "Please try another date",
            style = MaterialTheme.typography.bodySmall,
            color = DefaultOnPrimary.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun PromptToSelectDate() {
    val dimens = rememberResponsiveDimens()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimens.h(16 / 914f)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = "Select date",
            tint = DefaultOnPrimary.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Select a date to see available time slots",
            style = MaterialTheme.typography.bodyMedium,
            color = DefaultOnPrimary.copy(alpha = 0.6f)
        )
    }
}

fun formatDateForDisplay(date: LocalDate): String {
    return try {
        when (date) {
            LocalDate.now() -> "Today"
            LocalDate.now().plusDays(1) -> "Tomorrow"
            else -> date.format(DateTimeFormatter.ofPattern("dd MMMM", Locale.getDefault()))
        }
    } catch (e: Exception) {
        date.toString() + " (${e.message})"
    }
}


