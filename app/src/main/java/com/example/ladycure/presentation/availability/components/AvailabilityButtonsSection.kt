package com.example.ladycure.presentation.availability.components

import DefaultPrimary
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ladycure.presentation.availability.selectThisWeek
import com.example.ladycure.presentation.availability.selectWeekdays
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Composable that displays quick selection buttons for dates (Weekdays, This Week, Clear).
 *
 * @param currentMonth The currently displayed month in the calendar.
 * @param selectedDates The mutable state of the set of currently selected dates.
 * @param modifier Modifier for the layout of this composable.
 */
@Composable
internal fun QuickSelectionButtons(
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

/**
 * Composable for a save button with a loading indicator.
 *
 * @param isLoading Whether the save operation is currently in progress.
 * @param enabled Whether the button is enabled for interaction.
 * @param onClick The action to perform when the button is clicked.
 * @param modifier Modifier for the layout of this composable.
 */
@Composable
internal fun SaveButton(
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

/**
 * Composable for picking a time range with "From" and "To" chips and a visual progress bar.
 *
 * @param startTime The currently selected start time.
 * @param endTime The currently selected end time.
 * @param onStartTimeClick The action to perform when the "From" time chip is clicked.
 * @param onEndTimeClick The action to perform when the "To" time chip is clicked.
 * @param modifier Modifier for the layout of this composable.
 */
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

/**
 * Composable for a time picker chip displaying a time and a label.
 *
 * @param time The [LocalTime] to display in the chip.
 * @param label The label to display next to the time.
 * @param onClick The action to perform when the chip is clicked.
 * @param modifier Modifier for the layout of this composable.
 */
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

/**
 * Calculates the progress for a linear progress indicator based on a start and end time.
 * The progress is relative to the current time within the given time range.
 *
 * @param start The start time of the range.
 * @param end The end time of the range.
 * @return A float representing the progress, clamped between 0f and 1f.
 */
private fun calculateProgress(start: LocalTime, end: LocalTime): Float {
    val totalMinutes = ChronoUnit.MINUTES.between(start, end)
    val currentMinutes = ChronoUnit.MINUTES.between(start, LocalTime.now())
    return (currentMinutes.toFloat() / totalMinutes.toFloat()).coerceIn(0f, 1f)
}