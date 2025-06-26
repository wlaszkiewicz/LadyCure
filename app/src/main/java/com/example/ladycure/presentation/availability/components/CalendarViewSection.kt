package com.example.ladycure.presentation.availability.components

import DefaultOnPrimary
import DefaultPrimary
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ladycure.domain.model.DoctorAvailability
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Composable that displays a legend for the availability calendar.
 *
 * @param modifier The modifier for this composable.
 */
@Composable
fun AvailabilityLegend(modifier: Modifier = Modifier) {
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

/**
 * Composable that displays a single item in the availability legend.
 *
 * @param color The color of the legend item.
 * @param text The text to display for the legend item.
 * @param modifier The modifier for this composable.
 */
@Composable
fun LegendItem(color: Color, text: String, modifier: Modifier = Modifier) {
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

/**
 * Composable that displays the header for the calendar, including month navigation and weekday headers.
 *
 * @param currentMonth The currently displayed month.
 * @param minMonth The minimum month that can be navigated to.
 * @param onMonthChange Lambda that is invoked when the month changes.
 * @param onShowMonthPicker Lambda that is invoked when the month picker should be shown.
 * @param modifier The modifier for this composable.
 */
@Composable
fun CalendarHeader(
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

    val weekDays = DayOfWeek.entries
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
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

/**
 * Composable that displays a calendar view for selecting dates.
 *
 * @param currentMonth The currently displayed month in the calendar.
 * @param selectedDates A set of dates that are currently selected.
 * @param existingAvailabilities A list of existing doctor availabilities.
 * @param today The current date.
 * @param onDateSelected Lambda that is invoked when a date is selected.
 * @param modifier The modifier for this composable.
 */
@Composable
fun CalendarView(
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
        if (it < 0) 6 else it
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = modifier.height(280.dp)
    ) {
        items(offset) { Spacer(Modifier) }

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