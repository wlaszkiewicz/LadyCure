package com.example.ladycure.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ladycure.domain.model.DailyPeriodData
import com.example.ladycure.domain.model.PeriodTrackerSettings
import com.example.ladycure.ui.theme.DarkMagenta
import com.example.ladycure.ui.theme.DefaultOnPrimary
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.Lilac
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
fun MonthNavigationHeader(
    currentMonth: LocalDate,
    onMonthChange: (LocalDate) -> Unit
) {
    val dimens = rememberResponsiveDimens()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onMonthChange(currentMonth.minusMonths(1)) },
            modifier = Modifier.size(dimens.w(40 / 411f))
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                "Previous Month",
                tint = DefaultPrimary
            )
        }

        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleLarge,
            color = DefaultPrimary,
            fontWeight = FontWeight.Bold
        )

        IconButton(
            onClick = { onMonthChange(currentMonth.plusMonths(1)) },
            modifier = Modifier.size(dimens.w(40 / 411f))
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                "Next Month",
                tint = DefaultPrimary
            )
        }
    }
}

@Composable
fun WeekdayHeaders() {
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = DefaultOnPrimary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun PredictionCard(
    predictedPeriodStarts: Set<LocalDate>,
    predictedOvulationDays: Set<LocalDate>
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Cycle Predictions",
                style = MaterialTheme.typography.titleLarge,
                color = DefaultPrimary,
                fontWeight = FontWeight.Bold
            )

            val nextPredictedPeriod =
                predictedPeriodStarts.filter { it.isAfter(LocalDate.now()) }.minOrNull()
            nextPredictedPeriod?.let {
                Text(
                    text = "Next period: ${it.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DefaultOnPrimary.copy(alpha = 0.8f)
                )
            } ?: Text(
                text = "Set your last period start date in settings to see predictions.",
                style = MaterialTheme.typography.bodyMedium,
                color = DefaultOnPrimary.copy(alpha = 0.6f)
            )

            val nextPredictedOvulation =
                predictedOvulationDays.filter { it.isAfter(LocalDate.now()) }.minOrNull()
            nextPredictedOvulation?.let {
                Text(
                    text = "Ovulation day: ${it.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DefaultPrimary
                )
            }
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: LocalDate,
    periodSettings: PeriodTrackerSettings,
    dailyDataMap: Map<LocalDate, DailyPeriodData>,
    predictedPeriodStarts: Set<LocalDate>,
    predictedOvulationDays: Set<LocalDate>,
    onDayClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.with(TemporalAdjusters.firstDayOfMonth())
    val lastDayOfMonth = currentMonth.with(TemporalAdjusters.lastDayOfMonth())

    var startDay = firstDayOfMonth
    while (startDay.dayOfWeek != DayOfWeek.SUNDAY) {
        startDay = startDay.minusDays(1)
    }

    val daysInCalendar = remember(currentMonth) {
        mutableListOf<LocalDate>().apply {
            var tempDay = startDay
            while (tempDay.isBefore(lastDayOfMonth.plusDays(1)) || tempDay.dayOfWeek != DayOfWeek.SATURDAY) {
                add(tempDay)
                tempDay = tempDay.plusDays(1)
            }
            while (size < 42) {
                add(last().plusDays(1))
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(daysInCalendar) { date ->
            val isCurrentMonth = date.month == currentMonth.month && date.year == currentMonth.year
            val isToday = date == LocalDate.now()
            val dailyData = dailyDataMap[date]

            val isPeriodDay = dailyData?.isPeriodDay ?: false
            val hasNotesOrMood =
                dailyData?.notes?.isNotBlank() == true || dailyData?.moodEmoji != null || dailyData?.symptoms?.isNotEmpty() == true

            val isPredictedPeriodStart = predictedPeriodStarts.contains(date) && !isPeriodDay
            val isPredictedPeriodDayRange = predictedPeriodStarts.any { periodStartDate ->
                date.isAfter(periodStartDate.minusDays(1)) &&
                        date.isBefore(periodStartDate.plusDays(periodSettings.averagePeriodLength.toLong()))
            } && !isPeriodDay

            val isPredictedOvulationDay = predictedOvulationDays.contains(date)

            CalendarDay(
                date = date,
                isCurrentMonth = isCurrentMonth,
                isToday = isToday,
                isPeriodDay = isPeriodDay,
                isPredictedPeriodStart = isPredictedPeriodStart,
                isPredictedPeriodDayRange = isPredictedPeriodDayRange,
                isPredictedOvulationDay = isPredictedOvulationDay,
                hasNotesOrMood = hasNotesOrMood,
                onClick = { if (isCurrentMonth) onDayClick(date) }
            )
        }
    }
}

@Composable
fun CalendarDay(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    isPeriodDay: Boolean,
    isPredictedPeriodStart: Boolean,
    isPredictedPeriodDayRange: Boolean,
    isPredictedOvulationDay: Boolean,
    hasNotesOrMood: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isPeriodDay -> DefaultPrimary.copy(alpha = 0.8f)
                    isPredictedPeriodDayRange -> DefaultPrimary.copy(alpha = 0.2f)
                    isPredictedOvulationDay -> Lilac.copy(alpha = 0.4f)
                    isToday -> DefaultPrimary.copy(alpha = 0.1f)
                    else -> Color.White.copy(alpha = if (isCurrentMonth) 0.05f else 0.02f)
                }
            )
            .border(
                width = if (isToday) 2.dp else 0.dp,
                color = if (isToday) DefaultPrimary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = when {
                    isPeriodDay -> Color.White
                    isPredictedPeriodDayRange -> DefaultPrimary
                    isPredictedOvulationDay -> DarkMagenta
                    isToday -> DefaultPrimary
                    isCurrentMonth -> DefaultOnPrimary
                    else -> DefaultOnPrimary.copy(alpha = 0.4f)
                },
                fontWeight = if (isToday || isPeriodDay || isPredictedPeriodDayRange || isPredictedOvulationDay)
                    FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium
            )

            if (hasNotesOrMood) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(DefaultPrimary)
                )
            }

            if (isPredictedPeriodStart) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(DefaultPrimary)
                )
            }

            if (isPredictedOvulationDay) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(DarkMagenta)
                )
            }
        }
    }
}
