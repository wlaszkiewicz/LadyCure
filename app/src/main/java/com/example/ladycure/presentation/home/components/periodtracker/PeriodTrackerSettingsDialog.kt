package com.example.ladycure.presentation.home.components.periodtracker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ladycure.domain.model.PeriodTrackerSettings
import com.example.ladycure.ui.theme.DefaultBackground
import com.example.ladycure.ui.theme.DefaultOnPrimary
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
fun SettingsDialog(
    currentSettings: PeriodTrackerSettings,
    onSave: (PeriodTrackerSettings) -> Unit,
    onCancel: () -> Unit
) {
    val dimens = rememberResponsiveDimens()
    Dialog(onDismissRequest = onCancel) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.99f)
                .fillMaxHeight(0.8f)
                .clip(RoundedCornerShape(16.dp)),
            color = DefaultBackground,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dimens.w(16 / 411f), vertical = dimens.h(16 / 914f))
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                PeriodTrackerSettingsContent(
                    currentPeriodSettings = currentSettings,
                    onSave = onSave,
                    onCancel = onCancel
                )
            }
        }
    }
}

@Composable
fun PeriodTrackerSettingsContent(
    currentPeriodSettings: PeriodTrackerSettings,
    onSave: (PeriodTrackerSettings) -> Unit,
    onCancel: () -> Unit
) {
    val dimens = rememberResponsiveDimens()
    var periodLength by remember { mutableStateOf(currentPeriodSettings.averagePeriodLength) }
    var cycleLength by remember { mutableStateOf(currentPeriodSettings.averageCycleLength) }
    var lastPeriodStartDate by remember { mutableStateOf(currentPeriodSettings.lastPeriodStartDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.h(16 / 914f))
    ) {
        Text(
            text = "Period Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = DefaultPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        SettingCard(
            title = "Average Period Length",
            description = "How many days does your period usually last?",
            value = "$periodLength days"
        ) {
            NumberSelector(
                value = periodLength,
                minValue = 1,
                maxValue = 14,
                onValueChange = { periodLength = it }
            )
        }

        SettingCard(
            title = "Average Cycle Length",
            description = "Days between the start of one period and the next",
            value = "$cycleLength days"
        ) {
            NumberSelector(
                value = cycleLength,
                minValue = 21,
                maxValue = 45,
                onValueChange = { cycleLength = it }
            )
        }

        SettingCard(
            title = "Last Period Start Date",
            description = "When did your last period start?",
            value = lastPeriodStartDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                ?: "Select Date"
        ) {
            Button(
                onClick = { showDatePicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimens.h(48 / 914f)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultPrimary.copy(alpha = 0.1f),
                    contentColor = DefaultPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.DateRange, "Select Date", modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = lastPeriodStartDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                        ?: "Select Date",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (showDatePicker) {
                DatePickerDialog(
                    initialDate = lastPeriodStartDate ?: LocalDate.now(),
                    onDateSelected = { date ->
                        lastPeriodStartDate = date
                        showDatePicker = false
                    },
                    onDismiss = { showDatePicker = false }
                )
            }
        }

        Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(dimens.h(50 / 914f)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultOnPrimary.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Cancel",
                    color = DefaultOnPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.width(dimens.w(16 / 411f)))
            Button(
                onClick = {
                    onSave(PeriodTrackerSettings(periodLength, cycleLength, lastPeriodStartDate))
                },
                modifier = Modifier
                    .weight(1f)
                    .height(dimens.h(50 / 914f)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun NumberSelector(
    value: Int,
    minValue: Int,
    maxValue: Int,
    onValueChange: (Int) -> Unit
) {
    val dimens = rememberResponsiveDimens()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onValueChange(maxOf(minValue, value - 1)) },
            modifier = Modifier
                .size(dimens.w(40 / 411f))
                .background(DefaultPrimary.copy(alpha = 0.1f), CircleShape)
        ) {
            Text("-", color = DefaultPrimary, fontWeight = FontWeight.Bold)
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = DefaultOnPrimary,
            modifier = Modifier.padding(horizontal = dimens.w(16 / 411f))
        )
        IconButton(
            onClick = { onValueChange(minOf(maxValue, value + 1)) },
            modifier = Modifier
                .size(dimens.w(40 / 411f))
                .background(DefaultPrimary.copy(alpha = 0.1f), CircleShape)
        ) {
            Text("+", color = DefaultPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val dimens = rememberResponsiveDimens()
    var currentMonth by remember { mutableStateOf(initialDate) }
    var selectedDate by remember { mutableStateOf(initialDate) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(dimens.w(320 / 411f))
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = DefaultBackground
        ) {
            Column(
                modifier = Modifier.padding(dimens.w(16 / 411f)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Previous Month",
                            tint = DefaultPrimary
                        )
                    }
                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM")),
                        style = MaterialTheme.typography.titleLarge,
                        color = DefaultPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            "Next Month",
                            tint = DefaultPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))

                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            color = DefaultOnPrimary.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val daysInCalendar = remember(currentMonth) {
                    val firstDayOfMonth = currentMonth.with(TemporalAdjusters.firstDayOfMonth())
                    val lastDayOfMonth = currentMonth.with(TemporalAdjusters.lastDayOfMonth())

                    var startDay = firstDayOfMonth
                    while (startDay.dayOfWeek != DayOfWeek.SUNDAY) {
                        startDay = startDay.minusDays(1)
                    }

                    mutableListOf<LocalDate>().apply {
                        var tempDay = startDay
                        while (tempDay.isBefore(lastDayOfMonth.plusDays(1)) || tempDay.dayOfWeek != DayOfWeek.SATURDAY) {
                            add(tempDay)
                            tempDay = tempDay.plusDays(1)
                        }
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(dimens.h(240 / 914f)),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(daysInCalendar) { date ->
                        val isCurrentMonth =
                            date.month == currentMonth.month && date.year == currentMonth.year
                        val isSelected = date == selectedDate

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) DefaultPrimary else Color.Transparent)
                                .border(
                                    width = if (date == LocalDate.now()) 1.dp else 0.dp,
                                    color = DefaultPrimary,
                                    shape = CircleShape
                                )
                                .clickable(enabled = isCurrentMonth) { selectedDate = date },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                color = when {
                                    isSelected -> Color.White
                                    isCurrentMonth -> DefaultOnPrimary
                                    else -> DefaultOnPrimary.copy(alpha = 0.4f)
                                },
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DefaultOnPrimary.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = DefaultOnPrimary)
                    }

                    Spacer(modifier = Modifier.width(dimens.w(16 / 411f)))

                    Button(
                        onClick = {
                            onDateSelected(selectedDate)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DefaultPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Select", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingCard(
    title: String,
    description: String,
    value: String,
    content: @Composable () -> Unit
) {
    val dimens = rememberResponsiveDimens()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = dimens.w(16 / 411f),
                vertical = dimens.h(16 / 914f)
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = DefaultPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = DefaultOnPrimary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
