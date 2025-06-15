package com.example.ladycure.screens.doctor

import BabyBlue
import DefaultOnPrimary
import DefaultPrimary
import Green
import Red
import Yellow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalDensity
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
                QuickStatsRow(
                    upcomingCount = upcomingAppointments.value.size,
                    todaysCount = allAppointments.value.count { it.date == LocalDate.now() },
                    completedCount = allAppointments.value.count {
                        it.date == LocalDate.now() && it.time.isBefore(currentTime)
                    }
                )

                TodaysSchedule(
                    allAppointments = allAppointments,
                    //  currentTime = currentTime
                )


                NextAppointmentCard(
                    upcomingAppointments = upcomingAppointments,
                    selectedAppointment = selectedAppointment,
                    onShowEditStatusDialog = { showEditStatusDialog = true },
                    onViewAll = {
                        navController.navigate("doctor_appointments")
                    }
                )

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
                    }
                }
            }
        )
    }
}

@Composable
fun TodaysSchedule(
    allAppointments: androidx.compose.runtime.State<List<Appointment>>,
    currentTime: LocalTime = LocalTime.of(16, 0) // Default to noon for testing
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LocalDate.now()
                        .format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = DefaultOnPrimary
                    )
                )

                // Dynamic countdown to end of workday
                var timeRemaining by remember { mutableStateOf(calculateTimeRemaining()) }

                LaunchedEffect(Unit) {
                    while (true) {
                        delay(60_000) // Update every minute
                        timeRemaining = calculateTimeRemaining()
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

            Spacer(modifier = Modifier.height(12.dp))

            val todaysAppointments = allAppointments.value
                .filter { it.date == LocalDate.now().minusDays(1) } // TESTING Change to .now latr
                .sortedBy { it.time }

            if (todaysAppointments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No appointments scheduled today",
                        color = DefaultOnPrimary.copy(alpha = 0.6f)
                    )
                }
            } else {
                val workDayStart = LocalTime.of(9, 0)
                val workDayEnd = LocalTime.of(17, 0)
                val totalMinutes = ChronoUnit.MINUTES.between(workDayStart, workDayEnd).toFloat()

                // Calculate progress
                val progress = if (currentTime.isBefore(workDayStart)) {
                    0f
                } else if (currentTime.isAfter(workDayEnd)) {
                    1f
                } else {
                    ChronoUnit.MINUTES.between(workDayStart, currentTime).toFloat() / totalMinutes
                }


                Column(modifier = Modifier.fillMaxWidth()) {
                    // Timeline visualization
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(vertical = 8.dp)
                    ) {
                        // Progress indicator background
                        LinearProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterStart),
                            color = Color.LightGray.copy(alpha = 0.3f),
                            trackColor = Color.Transparent,
                        )

                        // Current progress indicator
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterStart),
                            color = DefaultPrimary,
                            trackColor = Color.Transparent
                        )

                        // Appointment time markers
                        todaysAppointments.forEach { appointment ->
                            val appointmentPosition = ChronoUnit.MINUTES.between(
                                workDayStart,
                                appointment.time
                            ).toFloat() / totalMinutes

                            if (appointmentPosition in 0f..1f) {
                                // Vertical line for appointment time
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .fillMaxHeight()
                                        .offset(x = (appointmentPosition * (LocalDensity.current.density * 360)).dp)
                                        .background(BabyBlue)
                                        .align(Alignment.CenterStart)
                                )

                                // Time label above the line
                                Text(
                                    text = appointment.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BabyBlue,
                                    modifier = Modifier
                                        .offset(
                                            x = (appointmentPosition * (LocalDensity.current.density * 360)).dp,
                                            y = (-16).dp
                                        )
                                        .align(Alignment.TopStart)
                                )
                            }
                        }
                    }

                    // Time markers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            workDayStart.format(DateTimeFormatter.ofPattern("h a")),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            workDayEnd.format(DateTimeFormatter.ofPattern("h a")),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

//                    // Appointment details (similar to availability screen)
//                    appointmentsByHour.forEach { (hour, hourAppointments) ->
//                        val hourText =
//                            LocalTime.of(hour, 0).format(DateTimeFormatter.ofPattern("h a"))
//
//                        Column(
//                            modifier = Modifier.fillMaxWidth()
//                        ) {
//                            Text(
//                                text = hourText,
//                                style = MaterialTheme.typography.labelMedium.copy(
//                                    fontWeight = FontWeight.Bold,
//                                    color = DefaultPrimary.copy(alpha = 0.8f)
//                                ),
//                                modifier = Modifier.padding(vertical = 4.dp)
//                            )
//
//                            FlowRow(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                                verticalArrangement = Arrangement.spacedBy(8.dp)
//                            ) {
//                                hourAppointments.forEach { appointment ->
//                                    val isCompleted = appointment.time.isBefore(currentTime)
//                                    val isCurrent =
//                                        appointment.time.truncatedTo(ChronoUnit.HOURS) ==
//                                                currentTime.truncatedTo(ChronoUnit.HOURS)
//
//                                    Box(
//                                        modifier = Modifier
//                                            .clip(RoundedCornerShape(4.dp))
//                                            .background(
//                                                when {
//                                                    isCurrent -> Red.copy(alpha = 0.1f)
//                                                    isCompleted -> DefaultPrimary.copy(alpha = 0.1f)
//                                                    else -> Color.White
//                                                }
//                                            )
//                                            .border(
//                                                1.dp,
//                                                when {
//                                                    isCurrent -> Red
//                                                    isCompleted -> DefaultPrimary
//                                                    else -> DefaultPrimary.copy(alpha = 0.5f)
//                                                },
//                                                RoundedCornerShape(4.dp)
//                                            )
//                                            .clickable { /* Handle click */ }
//                                    ) {
//                                        Row(
//                                            modifier = Modifier.padding(8.dp),
//                                            verticalAlignment = Alignment.CenterVertically
//                                        ) {
//                                            Column {
//                                                Text(
//                                                    appointment.time.format(
//                                                        DateTimeFormatter.ofPattern(
//                                                            "h:mm a"
//                                                        )
//                                                    ),
//                                                    style = MaterialTheme.typography.bodySmall.copy(
//                                                        fontWeight = FontWeight.Bold,
//                                                        color = when {
//                                                            isCurrent -> Red
//                                                            isCompleted -> DefaultPrimary
//                                                            else -> DefaultOnPrimary
//                                                        }
//                                                    )
//                                                )
//                                                Text(
//                                                    appointment.patientName ?: "Patient",
//                                                    style = MaterialTheme.typography.labelSmall.copy(
//                                                        color = DefaultOnPrimary.copy(alpha = 0.7f)
//                                                    )
//                                                )
//                                            }
//
//                                            Spacer(modifier = Modifier.width(8.dp))
//
//                                            Icon(
//                                                imageVector = when {
//                                                    isCurrent -> Icons.Default.Schedule
//                                                    isCompleted -> Icons.Default.CheckCircle
//                                                    else -> Icons.Default.Person
//                                                },
//                                                contentDescription = "Status",
//                                                tint = when {
//                                                    isCurrent -> Red
//                                                    isCompleted -> DefaultPrimary
//                                                    else -> DefaultPrimary.copy(alpha = 0.7f)
//                                                },
//                                                modifier = Modifier.size(16.dp)
//                                            )
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }

                    // Stats at the bottom
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${todaysAppointments.count { it.time.isBefore(currentTime) }} completed",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = BabyBlue
                                )
                            )
                            Text(
                                text = "${todaysAppointments.size} total",
                                style = MaterialTheme.typography.labelSmall,
                                color = DefaultOnPrimary.copy(alpha = 0.6f)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                        ) {
                            Text(
                                text = "${todaysAppointments.count { !it.time.isBefore(currentTime) }} remaining",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Yellow
                                )
                            )
                            Text(
                                text = "Next: ${
                                    todaysAppointments.firstOrNull { !it.time.isBefore(currentTime) }?.time?.format(
                                        DateTimeFormatter.ofPattern("h:mm a")
                                    ) ?: "None"
                                }",
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
fun NextAppointmentCard(
    upcomingAppointments: androidx.compose.runtime.State<List<Appointment>>,
    selectedAppointment: androidx.compose.runtime.MutableState<Appointment?>,
    onShowEditStatusDialog: () -> Unit,
    onViewAll: () -> Unit,
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
                        selectedAppointment.value = nearestAppointment
                        if (nearestAppointment.status == Status.PENDING) {
                            onShowEditStatusDialog()
                        }
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
        TextButton(
            onClick = onViewAll,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("View All Appointments", color = DefaultPrimary)
        }
    }
}


@Composable
private fun QuickStatsRow(
    upcomingCount: Int,
    todaysCount: Int,
    completedCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
    ) {
        // Upcoming Appointments
        StatCard(
            value = (todaysCount - completedCount).toString(),
            label = "Upcoming",
            icon = Icons.Default.Schedule,
            colorIcon = DefaultPrimary,
            modifier = Modifier.weight(1f)
        )

        // Today's Appointments
        StatCard(
            value = todaysCount.toString(),
            label = "Today",
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
                        tint = DefaultPrimary
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
fun calculateTimeRemaining(): String {
    val endOfWorkday = LocalTime.of(17, 0) // 5 PM
    val now = LocalTime.of(12, 0) // Replace with LocalTime.now() in production

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
            containerColor = Color.White.copy(alpha = 0.9f)
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
private fun DoctorAppointmentCard(
    appointment: Appointment,
    onClickStatus: () -> Unit = {},
    onCommentUpdated: (String) -> Unit = {}
) {
    var showDetailsDialog by remember { mutableStateOf(false) }

    val statusColor = when (appointment.status) {
        Status.CONFIRMED -> Color(0xFF4CAF50)
        Status.PENDING -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier
            .width(280.dp)
            .padding(vertical = 4.dp)
            .clickable { showDetailsDialog = true },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header with doctor info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DefaultPrimary.copy(alpha = 0.1f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Patient",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column {
                    Text(
                        text = appointment.patientName ?: "Patient",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = appointment.type.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date and time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        appointment.date.format(DateTimeFormatter.ofPattern("MMM dd yyyy")),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Time",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        appointment.time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US)),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer with status and price
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.1f))
                        .clickable(onClick = onClickStatus)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = appointment.status.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Text(
                    text = "$${"%.2f".format(appointment.price)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DefaultPrimary
                    )
                )
            }
        }
    }

    // Details Dialog
    if (showDetailsDialog) {
        DetailsDialog(
            appointment = appointment,
            onDismiss = { showDetailsDialog = false },
            onClickStatus = {
                onClickStatus()
            },
            onMessage = {},
            onCommentUpdated = {
                onCommentUpdated(it)
                appointment.comments = it
            }
        )
    }
}

@Composable
fun DetailsDialog(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onClickStatus: () -> Unit,
    onMessage: () -> Unit,
    onCommentUpdated: (String) -> Unit
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

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = DefaultPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = DefaultPrimary
            )
        }
    }
}

@Composable
private fun ActivityItem(
    icon: ImageVector,
    title: String,
    description: String,
    time: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(DefaultPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = DefaultPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = DefaultOnPrimary.copy(alpha = 0.6f)
            )
        }
        Text(
            text = time,
            style = MaterialTheme.typography.labelSmall,
            color = DefaultOnPrimary.copy(alpha = 0.5f)
        )
    }
}

