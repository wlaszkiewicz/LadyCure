package com.example.ladycure.screens.doctor

import BabyBlue
import DefaultOnPrimary
import DefaultPrimary
import Green
import Purple
import Red
import Yellow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.example.ladycure.data.Appointment
import com.example.ladycure.data.Appointment.Status
import com.example.ladycure.presentation.home.components.AppointmentDetailItem
import com.example.ladycure.presentation.home.components.InfoChip
import com.example.ladycure.repository.AppointmentRepository
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.repository.UserRepository
import com.example.ladycure.utility.SnackbarController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun DoctorHomeScreen(
    navController: NavHostController,
    snackbarController: SnackbarController,
    authRepo: AuthRepository = AuthRepository(),
    userRepo: UserRepository = UserRepository(),
    appointmentsRepo: AppointmentRepository = AppointmentRepository()
) {
    val doctorData = remember { mutableStateOf<Map<String, Any>?>(null) }
    var upcomingAppointments = remember { mutableStateOf<List<Appointment>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val allAppointments = remember { mutableStateOf<List<Appointment>>(emptyList()) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    remember { mutableStateOf(false) }
    var showEditStatusDialog by remember { mutableStateOf(false) }
    val selectedAppointment = remember { mutableStateOf<Appointment?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }


    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val result = userRepo.getCurrentUserData()
        if (result.isSuccess) {
            doctorData.value = result.getOrNull()

            val appointmentsResult = appointmentsRepo.getAppointments("doctor")
            if (appointmentsResult.isSuccess) {
                allAppointments.value = appointmentsResult.getOrNull() ?: emptyList()

                upcomingAppointments.value = allAppointments.value.filter {
                    it.date.isAfter(LocalDate.now()) || (it.date == LocalDate.now() && it.time >= LocalTime.now())
                }

                upcomingAppointments.value = upcomingAppointments.value.sortedWith(
                    compareBy({ it.date }, { it.time })
                )

            }
            isLoading.value = false
        } else {
            errorMessage.value = result.exceptionOrNull()?.message
            isLoading.value = false
        }
    }

    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        val now = LocalTime.now()
        val initialDelay =
            (60_000 - (now.second * 1000 + now.nano / 1_000_000)).toLong()
        delay(initialDelay)
        while (true) {
            currentTime = LocalTime.now()
            delay(60_000)
        }
    }


    if (isLoading.value) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = DefaultPrimary)
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            DoctorHeader(
                doctorData = doctorData,
                navController = navController
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StatsRow(
                    todaysCount = allAppointments.value.count { it.date == LocalDate.now() },
                    completedCount = allAppointments.value.count {
                        it.date == LocalDate.now() && it.time.isBefore(currentTime)
                    }
                )

                TodaysSchedule(
                    allAppointments = allAppointments,
                    currentTime = currentTime,
                    onSelectAppointment = { appointment ->
                        selectedAppointment.value = appointment
                        showDetailsDialog = true
                    },
                )


                NextAppointmentCard(
                    upcomingAppointments = upcomingAppointments,
                    selectedAppointment = selectedAppointment,
                    onShowEditStatusDialog = { showEditStatusDialog = true },
                    onViewAll = {
                        navController.navigate("doctor_appointments")
                    },
                    onShowDetailsDialog = { appointment ->
                        selectedAppointment.value = appointment
                        showDetailsDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // In your DoctorHomeScreen, replace the news section with:
                NewsCarousel(
                    navController = navController,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showEditStatusDialog) {
        ConfirmAppointmentDialog(
            onDismiss = { showEditStatusDialog = false },
            onConfirm = {
                CoroutineScope(Dispatchers.IO).launch {
                    val result = appointmentsRepo.updateAppointmentStatus(
                        appointmentId = selectedAppointment.value!!.appointmentId,
                        status = Status.CONFIRMED.displayName
                    )
                    if (result.isFailure) {
                        errorMessage.value = result.exceptionOrNull()?.message
                    } else {
                        // Change the status of the appointment in the list
                        upcomingAppointments.value = upcomingAppointments.value.map {
                            if (it.appointmentId == selectedAppointment.value!!.appointmentId) {
                                it.copy(status = Status.CONFIRMED)
                            } else {
                                it
                            }
                        }

                        allAppointments.value = allAppointments.value.map {
                            if (it.appointmentId == selectedAppointment.value!!.appointmentId) {
                                it.copy(status = Status.CONFIRMED)
                            } else {
                                it
                            }
                        }

                        selectedAppointment.value = selectedAppointment.value?.copy(status = Status.CONFIRMED)

                        snackbarController.showMessage("Appointment confirmed successfully")
                    }
                }
            },
        )
    }


    if (showDetailsDialog) {
        DetailsDialog(
            appointment = selectedAppointment.value!!,
            onDismiss = { showDetailsDialog = false },
            onClickStatus = {
                showEditStatusDialog = true
            },
            onMessage = {},
            onCommentUpdated = { newComment ->
                coroutineScope.launch {
                    val result = appointmentsRepo.updateAppointmentComment(
                        appointmentId = selectedAppointment.value!!.appointmentId,
                        newComment
                    )
                    if (result.isFailure) {
                        snackbarController.showMessage(
                            result.exceptionOrNull()?.message
                                ?: "Failed to update comment"
                        )
                    } else {
                        // Update the comment in the selected appointment
                        selectedAppointment.value = selectedAppointment.value?.copy(comments = newComment)
                        upcomingAppointments.value = upcomingAppointments.value.map {
                            if (it.appointmentId == selectedAppointment.value!!.appointmentId) {
                                it.copy(comments = newComment)
                            } else {
                                it
                            }
                        }
                    }
                }
            },
        )
    }
}

val BookedColor =  BabyBlue.copy(alpha = 0.6f) // A distinct color for booked slots
val AvailableColor = Purple.copy(alpha = 0.4f) // A lighter color for available slots
val PastColor = Color(0xFFD6A6C2)
val CurrentTimeColor = Purple

@Composable
fun TodaysSchedule(
    allAppointments: State<List<Appointment>>,
    currentTime: LocalTime = LocalTime.now(),
    startOfWorkday: LocalTime = LocalTime.of(9, 0) ,
    endOfWorkday: LocalTime = LocalTime.of(17, 0),
    onSelectAppointment: (Appointment) -> Unit
) {

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
            modifier = Modifier.padding(16.dp)
        ) {
            // ... Header Row with date and countdown remains the same ...
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

                var timeRemaining by remember { mutableStateOf(calculateTimeRemaining(
                    now = currentTime,
                    startOfWorkday = startOfWorkday,
                    endOfWorkday = endOfWorkday
                )) }
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


            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    val timelineWidth = this.maxWidth
                    var lastTime = LocalTime.of(9, 0)
                    val workDayStart = lastTime
                    val totalWorkdayMinutes = ChronoUnit.MINUTES.between(workDayStart, LocalTime.of(17, 0))

                    // Layer 1: Base schedule
                    val todaysAppointments = allAppointments.value
                        .filter { it.date == LocalDate.now() }
                        .sortedBy { it.time }

                    todaysAppointments.forEach { appointment ->
                        val appointmentStart = appointment.time
                        val appointmentEnd = appointment.time.plusMinutes(appointment.type.durationInMinutes.toLong())

                        // Draw AVAILABLE slot
                        val freeTimeMinutes = ChronoUnit.MINUTES.between(lastTime, appointmentStart)
                        if (freeTimeMinutes > 0) {
                            val freeSlotWidth = timelineWidth * (freeTimeMinutes.toFloat() / totalWorkdayMinutes)
                            val freeSlotOffset = timelineWidth * (ChronoUnit.MINUTES.between(workDayStart, lastTime).toFloat() / totalWorkdayMinutes)
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(freeSlotWidth)
                                    .offset(x = freeSlotOffset)
                                    .background(AvailableColor)
                                    .border(width = 1.dp, color = Color.White) // ADD BORDER
                            )
                        }

                        // Draw BOOKED slot
                        val bookedSlotWidth = timelineWidth * (appointment.type.durationInMinutes.toFloat() / totalWorkdayMinutes)
                        val bookedSlotOffset = timelineWidth * (ChronoUnit.MINUTES.between(workDayStart, appointmentStart).toFloat() / totalWorkdayMinutes)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(bookedSlotWidth)
                                .offset(x = bookedSlotOffset)
                                .background(BookedColor)
                                .clickable {
                                    onSelectAppointment(appointment)
                                }
                                .border(width = 1.dp, color = Color.White) // ADD BORDER
                        )
                        lastTime = appointmentEnd
                    }

                    // Draw final AVAILABLE slot
                    val remainingMinutes = ChronoUnit.MINUTES.between(lastTime, LocalTime.of(17, 0))
                    if (remainingMinutes > 0) {
                        val freeSlotWidth = timelineWidth * (remainingMinutes.toFloat() / totalWorkdayMinutes)
                        val freeSlotOffset = timelineWidth * (ChronoUnit.MINUTES.between(workDayStart, lastTime).toFloat() / totalWorkdayMinutes)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(freeSlotWidth)
                                .offset(x = freeSlotOffset)
                                .background(AvailableColor)
                                .border(width = 1.dp, color = Color.White) // ADD BORDER
                        )
                    }

                    // ... Layer 2 (Past Overlay) and Layer 3 (Current Time) remain the same ...
                    if (currentTime.isAfter(workDayStart)) {
                        val minutesIntoDay = ChronoUnit.MINUTES.between(workDayStart, currentTime).coerceAtMost(totalWorkdayMinutes)
                        val pastWidth = timelineWidth * (minutesIntoDay.toFloat() / totalWorkdayMinutes)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(pastWidth)
                                .background(PastColor)
                                .align(Alignment.CenterStart)
                        )
                    }

                    if (currentTime.isAfter(workDayStart) && currentTime.isBefore(LocalTime.of(17, 0))) {
                        val minutesFromStart = ChronoUnit.MINUTES.between(workDayStart, currentTime)
                        val currentPosition = timelineWidth * (minutesFromStart.toFloat() / totalWorkdayMinutes)
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
                    Text(startOfWorkday.format(DateTimeFormatter.ofPattern("h a")), style = MaterialTheme.typography.labelSmall)
                    Text(endOfWorkday.format(DateTimeFormatter.ofPattern("h a")), style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.height(16.dp))
                TimelineLegend()

            }
        }
    }
}


@Composable
private fun TimelineLegend() {
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
private fun LegendItem(color: Color, label: String) {
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
@Composable
fun NextAppointmentCard(
    upcomingAppointments: State<List<Appointment>>,
    selectedAppointment: MutableState<Appointment?>,
    onShowEditStatusDialog: () -> Unit,
    onViewAll: () -> Unit,
    onShowDetailsDialog: (Appointment) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Find the nearest appointment
        val nearestAppointment = remember(upcomingAppointments.value) {
            upcomingAppointments.value.minByOrNull {
                ChronoUnit.DAYS.between(LocalDate.now(), it.date).absoluteValue
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title
            Text(
                text = "Next Appointment",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = DefaultPrimary
                )
            )

            TextButton(
                onClick = onViewAll,
            ) {
                Text("View All", color = DefaultPrimary)
            }

        }
        Spacer(modifier = Modifier.height(8.dp))

        if (nearestAppointment == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No upcoming appointments scheduled",
                    color = DefaultOnPrimary.copy(alpha = 0.6f)
                )
            }
        } else {
            val isToday = nearestAppointment.date == LocalDate.now()
            val isTomorrow = nearestAppointment.date == LocalDate.now().plusDays(1)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onShowDetailsDialog(nearestAppointment)
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Calendar date box
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DefaultPrimary.copy(alpha = 0.1f))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = nearestAppointment.date.format(
                                    DateTimeFormatter.ofPattern(
                                        "MMM"
                                    )
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = DefaultPrimary
                            )
                            Text(
                                text = nearestAppointment.date.format(
                                    DateTimeFormatter.ofPattern(
                                        "dd"
                                    )
                                ),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = DefaultPrimary
                            )
                            Text(
                                text = nearestAppointment.date.format(
                                    DateTimeFormatter.ofPattern(
                                        "EEE"
                                    )
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = DefaultPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Appointment details
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = nearestAppointment.patientName ?: "Patient",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = "$${"%.2f".format(nearestAppointment.price)}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DefaultPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Time chip
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DefaultPrimary.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = nearestAppointment.time.format(
                                        DateTimeFormatter.ofPattern(
                                            "h:mm a"
                                        )
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = DefaultPrimary
                                )
                            }

                            // Type chip
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BabyBlue.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = nearestAppointment.type.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = BabyBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Status and relative time
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val statusColor = when (nearestAppointment.status) {
                                Status.CONFIRMED -> Green
                                Status.PENDING -> Yellow
                                else -> Red
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(statusColor.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clickable(onClick = {
                                        selectedAppointment.value = nearestAppointment
                                        if (nearestAppointment.status == Status.PENDING) {
                                            onShowEditStatusDialog()
                                        }
                                    })
                            ) {
                                Text(
                                    text = nearestAppointment.status.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = statusColor
                                )
                            }

                            Text(
                                text = when {
                                    isToday -> "Today"
                                    isTomorrow -> "Tomorrow"
                                    else -> "In ${
                                        ChronoUnit.DAYS.between(
                                            LocalDate.now(),
                                            nearestAppointment.date
                                        )
                                    } days"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = DefaultOnPrimary.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

    }

}


@Composable
private fun StatsRow(
    todaysCount: Int,
    completedCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
    ) {
        // Appointments Today
        StatCard(
            value = todaysCount.toString(),
            label = "Today",

            icon = Icons.Default.Schedule,
            colorIcon = DefaultPrimary,
            modifier = Modifier.weight(1f)
        )

        // Upcoming Appointments
        StatCard(
            value = (todaysCount - completedCount).toString(),
            label = "Upcoming",
            icon = Icons.Default.CalendarToday,
            colorIcon = BabyBlue,
            modifier = Modifier.weight(1f)
        )

        // Completed Today
        StatCard(
            value = completedCount.toString(),
            label = "Completed",
            icon = Icons.Default.CheckCircle,
            colorIcon = Yellow,
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
private fun DoctorHeader(
    doctorData: androidx.compose.runtime.State<Map<String, Any>?>,
    navController: NavHostController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome Dr. ${doctorData.value?.get("name") ?: ""}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = DefaultPrimary
            )
            Text(
                text = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                style = MaterialTheme.typography.bodyMedium,
                color = DefaultOnPrimary.copy(alpha = 0.6f)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { navController.navigate("notifications") },
                modifier = Modifier.size(40.dp)
            ) {
                BadgedBox(badge = {
                    Badge(modifier = Modifier.offset((-4).dp, 4.dp))
                }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Doctor avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(DefaultPrimary.copy(alpha = 0.2f))
                    .clickable { navController.navigate("profile") },
                contentAlignment = Alignment.Center
            ) {
                val profileUrl = doctorData.value?.get("profilePictureUrl") as? String
                if (profileUrl != null) {
                    SubcomposeAsyncImage(
                        model = profileUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        loading = {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = DefaultPrimary
                            )
                        },
                        error = {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = DefaultPrimary
                            )
                        }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = DefaultPrimary,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}


// Helper function for countdown
fun calculateTimeRemaining(now: LocalTime, startOfWorkday: LocalTime, endOfWorkday: LocalTime): String {
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


@OptIn(ExperimentalPagerApi::class)
@Composable
fun NewsCarousel(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState()
    val newsItems = listOf(
        NewsItemData(
            title = "New Guidelines for Diabetes Management",
            summary = "The ADA has released updated guidelines emphasizing personalized treatment plans...",
            category = "Endocrinology",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/telecure-6bbb6.firebasestorage.app/o/chat_attachments%2F01248eae-6322-4256-b29e-3c221ed37cfd?alt=media&token=8e692775-797f-470a-8d84-c6db801939c9",
            time = "2 hours ago"
        ),
        NewsItemData(
            title = "New Advances in Cancer Treatment",
            summary = "Recent studies show promising results in immunotherapy for breast cancer...",
            category = "Oncology",
            imageUrl = "https://example.com/cancer-news.jpg",
            time = "1 hour ago"
        ),
        NewsItemData(
            title = "Cardiology Breakthroughs in 2023",
            summary = "New techniques in heart surgery are improving patient outcomes significantly...",
            category = "Cardiology",
            imageUrl = "https://example.com/cardiology-news.jpg",
            time = "3 hours ago"
        )
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Medical News & Updates",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = DefaultPrimary
                )
            )
            Text(
                text = "Today, ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d"))}",
                style = MaterialTheme.typography.bodySmall,
                color = DefaultOnPrimary.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalPager(
            count = newsItems.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) { page ->
            val item = newsItems[page]
            NewsCard(
                title = item.title,
                summary = item.summary,
                category = item.category,
                imageUrl = item.imageUrl,
                time = item.time,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                onClick = { /* Handle click */ }
            )
        }

        // Add page indicators
        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp),
            activeColor = DefaultPrimary,
            inactiveColor = DefaultPrimary.copy(alpha = 0.2f)
        )
    }
}


data class NewsItemData(
    val title: String,
    val summary: String,
    val category: String,
    val imageUrl: String,
    val time: String
)

@Composable
fun NewsCard(
    title: String,
    summary: String,
    category: String,
    imageUrl: String,
    time: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image on the left
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "News image",
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(DefaultPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = DefaultPrimary)
                        }
                    },
                    error = {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Error loading image",
                            tint = DefaultPrimary,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(DefaultPrimary.copy(alpha = 0.1f))
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content on the right
            Column(modifier = Modifier.weight(1f)) {
                // Category chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DefaultPrimary.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall,
                        color = DefaultPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Summary
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Time and read more
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelSmall,
                        color = DefaultOnPrimary.copy(alpha = 0.5f)
                    )

                    TextButton(
                        onClick = onClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Read more",
                            style = MaterialTheme.typography.labelSmall,
                            color = DefaultPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmAppointmentDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm") },
        text = { Text("Do you want to confirm the appointment?") },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    colorIcon: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colorIcon,
                )
                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = colorIcon,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = DefaultOnPrimary.copy(alpha = 0.6f)
            )
        }
    }
}


@Composable
fun DetailsDialog(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onClickStatus: () -> Unit,
    onMessage: () -> Unit,
    onCommentUpdated: (String) -> Unit,
) {
    var editedComment by remember { mutableStateOf(appointment.comments) }
    var showEditComment by remember { mutableStateOf(false) }


    remember { mutableStateOf(false) }
    val statusColor = when (appointment.status) {
        Status.CONFIRMED -> Green
        Status.PENDING -> Yellow
        else -> Red
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
            ) {
                // Header with patient info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    DefaultPrimary.copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = 100f
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Doctor avatar
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(DefaultPrimary.copy(alpha = 0.1f))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Patient",
                                    tint = DefaultPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = appointment.patientName,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                )
                                Text(
                                    text = appointment.type.displayName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = DefaultOnPrimary.copy(alpha = 0.7f)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Info chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Status chip
                            InfoChip(
                                text = appointment.status.displayName,
                                color = statusColor,
                                onClick = if (appointment.status == Status.PENDING) {
                                    onClickStatus
                                } else {
                                    {}
                                }
                            )

                            // Duration chip
                            InfoChip(
                                text = "${appointment.type.durationInMinutes} min",
                                color = DefaultPrimary
                            )

                            // Price chip
                            InfoChip(
                                text = "$%.2f".format(appointment.price),
                                color = BabyBlue
                            )
                        }
                    }
                }

                // Appointment details
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    // Date and time section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Date",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DefaultOnPrimary.copy(alpha = 0.6f)
                                )
                            )
                            Text(
                                text = appointment.date.format(DateTimeFormatter.ofPattern("EEE, MMM dd")),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Time",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DefaultOnPrimary.copy(alpha = 0.6f)
                                )
                            )
                            Text(
                                text = appointment.time.format(
                                    DateTimeFormatter.ofPattern(
                                        "h:mm a",
                                        Locale.US
                                    )
                                ),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Details section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Location
                        AppointmentDetailItem(
                            icon = Icons.Default.LocationOn,
                            title = "Location",
                            value = appointment.address.ifEmpty { "Not specified" }
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Comment,
                                        contentDescription = "Notes",
                                        tint = DefaultPrimary.copy(alpha = 0.8f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Notes",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = DefaultOnPrimary.copy(alpha = 0.6f)
                                        )
                                    )
                                }

                                IconButton(
                                    onClick = { showEditComment = !showEditComment },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (showEditComment) Icons.Default.Close else Icons.Default.Edit,
                                        contentDescription = if (showEditComment) "Close" else "Edit",
                                        tint = DefaultPrimary,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = !showEditComment,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Text(
                                    text = appointment.comments.ifEmpty { "No notes added" },
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = DefaultOnPrimary
                                    ),
                                    modifier = Modifier.padding(start = 32.dp, top = 8.dp)
                                )
                            }

                            AnimatedVisibility(
                                visible = showEditComment,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier.padding(start = 32.dp, top = 8.dp)
                                ) {
                                    TextField(
                                        value = editedComment,
                                        onValueChange = { editedComment = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedIndicatorColor = DefaultPrimary,
                                            unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f),
                                            focusedTextColor = DefaultOnPrimary,
                                            unfocusedTextColor = DefaultOnPrimary
                                        ),
                                        textStyle = MaterialTheme.typography.bodyLarge,
                                        placeholder = {
                                            Text(
                                                "Add notes about this appointment",
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    color = DefaultOnPrimary.copy(alpha = 0.5f)
                                                )
                                            )
                                        },
                                        maxLines = 3,
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = {
                                                showEditComment = false
                                                editedComment = appointment.comments
                                            }
                                        ) {
                                            Text("Cancel", color = DefaultPrimary)
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        OutlinedButton(
                                            onClick = {
                                                onCommentUpdated(editedComment)
                                                showEditComment = false
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = Color.White,
                                                contentColor = DefaultPrimary
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Save")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons
                    if (appointment.status != Status.CANCELLED) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White,
                                    contentColor = DefaultPrimary
                                ),
                            ) {
                                Text(
                                    text = "Go back",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = DefaultPrimary
                                )
                            }
                            // Reschedule button
                            Button(
                                onClick = onMessage,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DefaultPrimary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.padding(6.dp)
                            ) {
                                Text(
                                    "Message",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}
