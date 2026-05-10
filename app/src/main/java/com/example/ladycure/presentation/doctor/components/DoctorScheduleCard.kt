package com.example.ladycure.presentation.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ladycure.domain.model.Appointment
import com.example.ladycure.ui.theme.BabyBlue
import com.example.ladycure.ui.theme.DefaultOnPrimary
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.Mauve
import com.example.ladycure.ui.theme.Purple
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

val BookedColor = BabyBlue.copy(alpha = 0.6f)
val AvailableColor = Purple.copy(alpha = 0.4f)
val PastColor = Mauve
val CurrentTimeColor = Purple

@Composable
fun TodaysSchedule(
    allAppointments: List<Appointment>,
    currentTime: LocalTime = LocalTime.now(),
    startOfWorkday: LocalTime = LocalTime.of(9, 0),
    endOfWorkday: LocalTime = LocalTime.of(17, 0),
    onSelectAppointment: (Appointment) -> Unit
) {
    val dimens = rememberResponsiveDimens()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = dimens.w(0.039f), vertical = dimens.h(0.017f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Schedule",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = DefaultOnPrimary
                    )
                )

                var timeRemaining by remember {
                    mutableStateOf(
                        calculateTimeRemaining(
                            now = currentTime,
                            startOfWorkday = startOfWorkday,
                            endOfWorkday = endOfWorkday
                        )
                    )
                }
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(60_000)
                        timeRemaining = calculateTimeRemaining(
                            now = currentTime,
                            startOfWorkday = startOfWorkday,
                            endOfWorkday = endOfWorkday
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DefaultPrimary.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Time remaining",
                            tint = DefaultPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = timeRemaining,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = DefaultPrimary
                            )
                        )
                    }
                }
            }


            Spacer(modifier = Modifier.height(dimens.h(0.017f)))

            Column(modifier = Modifier.fillMaxWidth()) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.h(0.026f))
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    val timelineWidth = this.maxWidth
                    var lastTime = LocalTime.of(9, 0)
                    val workDayStart = lastTime
                    val totalWorkdayMinutes =
                        ChronoUnit.MINUTES.between(workDayStart, LocalTime.of(17, 0))

                    val todaysAppointments = allAppointments
                        .filter { it.date == LocalDate.now() }
                        .sortedBy { it.time }

                    todaysAppointments.forEach { appointment ->
                        val appointmentStart = appointment.time
                        val appointmentEnd =
                            appointment.time.plusMinutes(appointment.type.durationInMinutes.toLong())

                        val freeTimeMinutes = ChronoUnit.MINUTES.between(lastTime, appointmentStart)
                        if (freeTimeMinutes > 0) {
                            val freeSlotWidth =
                                timelineWidth * (freeTimeMinutes.toFloat() / totalWorkdayMinutes)
                            val freeSlotOffset =
                                timelineWidth * (ChronoUnit.MINUTES.between(workDayStart, lastTime)
                                    .toFloat() / totalWorkdayMinutes)
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(freeSlotWidth)
                                    .offset(x = freeSlotOffset)
                                    .background(AvailableColor)
                                    .border(width = 1.dp, color = Color.White)
                            )
                        }

                        val bookedSlotWidth =
                            timelineWidth * (appointment.type.durationInMinutes.toFloat() / totalWorkdayMinutes)
                        val bookedSlotOffset = timelineWidth * (ChronoUnit.MINUTES.between(
                            workDayStart,
                            appointmentStart
                        ).toFloat() / totalWorkdayMinutes)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(bookedSlotWidth)
                                .offset(x = bookedSlotOffset)
                                .background(BookedColor)
                                .clickable {
                                    onSelectAppointment(appointment)
                                }
                                .border(width = 1.dp, color = Color.White)
                        )
                        lastTime = appointmentEnd
                    }

                    val remainingMinutes = ChronoUnit.MINUTES.between(lastTime, LocalTime.of(17, 0))
                    if (remainingMinutes > 0) {
                        val freeSlotWidth =
                            timelineWidth * (remainingMinutes.toFloat() / totalWorkdayMinutes)
                        val freeSlotOffset =
                            timelineWidth * (ChronoUnit.MINUTES.between(workDayStart, lastTime)
                                .toFloat() / totalWorkdayMinutes)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(freeSlotWidth)
                                .offset(x = freeSlotOffset)
                                .background(AvailableColor)
                                .border(width = 1.dp, color = Color.White)
                        )
                    }

                    if (currentTime.isAfter(workDayStart)) {
                        val minutesIntoDay = ChronoUnit.MINUTES.between(workDayStart, currentTime)
                            .coerceAtMost(totalWorkdayMinutes)
                        val pastWidth =
                            timelineWidth * (minutesIntoDay.toFloat() / totalWorkdayMinutes)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(pastWidth)
                                .background(PastColor)
                                .align(Alignment.CenterStart)
                        )
                    }

                    if (currentTime.isAfter(workDayStart) && currentTime.isBefore(
                            LocalTime.of(
                                17,
                                0
                            )
                        )
                    ) {
                        val minutesFromStart = ChronoUnit.MINUTES.between(workDayStart, currentTime)
                        val currentPosition =
                            timelineWidth * (minutesFromStart.toFloat() / totalWorkdayMinutes)
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .fillMaxHeight()
                                .offset(x = currentPosition)
                                .background(CurrentTimeColor)
                        )
                    }
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        startOfWorkday.format(DateTimeFormatter.ofPattern("h a")),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        endOfWorkday.format(DateTimeFormatter.ofPattern("h a")),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(dimens.h(0.017f)))
                TimelineLegend()

            }
        }
    }
}


@Composable
fun TimelineLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = BookedColor, label = "Booked")
        LegendItem(color = AvailableColor, label = "Available")
        LegendItem(color = PastColor, label = "Past")
        LegendItem(color = CurrentTimeColor, label = "Current Time")
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

fun calculateTimeRemaining(
    now: LocalTime,
    startOfWorkday: LocalTime,
    endOfWorkday: LocalTime
): String {
    if (now.isBefore(startOfWorkday)) {
        return "Workday hasn't started yet"
    }
    return if (now.isAfter(endOfWorkday)) {
        "Day completed"
    } else {
        val hours = ChronoUnit.HOURS.between(now, endOfWorkday)
        val minutes = ChronoUnit.MINUTES.between(now, endOfWorkday) % 60
        "${hours}h ${minutes}m left"
    }
}
