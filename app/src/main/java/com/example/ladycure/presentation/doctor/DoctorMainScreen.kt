package com.example.ladycure.presentation.doctor

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
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.example.ladycure.domain.model.Appointment
import com.example.ladycure.domain.model.Appointment.Status
import com.example.ladycure.presentation.home.components.AppointmentDetailItem
import com.example.ladycure.presentation.home.components.InfoChip
import com.example.ladycure.utility.SnackbarController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Main screen for the doctor's home view, displaying appointments, stats, and news.
 *
 * @param navController The navigation controller for handling navigation events.
 * @param snackbarController The snackbar controller for displaying messages.
 * @param viewModel The view model providing data for the doctor home screen.
 */
@Composable
fun DoctorHomeScreen(
    navController: NavHostController,
    snackbarController: SnackbarController,
    viewModel: DoctorHomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedAppointment by viewModel.selectedAppointment
    val showEditStatusDialog by viewModel.showEditStatusDialog
    val showDetailsDialog by viewModel.showDetailsDialog
    val nearestAppointment by viewModel.nearestAppointment.collectAsState()


    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarController.showMessage(error)
            viewModel.clearErrorMessage()
        }
    }

    if (uiState.isLoading) {
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
                doctorData = uiState.doctorData,
                unreadNotificationsCount = uiState.unreadNotificationsCount,
                onNotificationClick = { navController.navigate("notifications/doctor") },
                onProfileClick = { navController.navigate("profile") }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StatsRow(
                    todaysCount = uiState.allAppointments.count { it.date == LocalDate.now() },
                    completedCount = uiState.allAppointments.count {
                        it.date == LocalDate.now() && it.time.isBefore(uiState.currentTime)
                    }
                )

                TodaysSchedule(
                    allAppointments = uiState.allAppointments,
                    currentTime = uiState.currentTime,
                    onSelectAppointment = { appointment ->
                        viewModel.selectAppointment(appointment)
                        viewModel.setShowDetailsDialog(true)
                    },
                )

                NextAppointmentCard(
                    nearestAppointment = nearestAppointment,
                    onShowEditStatusDialog = { viewModel.setShowEditStatusDialog(true) },
                    onViewAll = { navController.navigate("appointments") },
                    onShowDetailsDialog = { appointment ->
                        viewModel.selectAppointment(appointment)
                        viewModel.setShowDetailsDialog(true)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                NewsCarousel(
                    navController = navController,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showEditStatusDialog) {
        ConfirmAppointmentDialog(
            onDismiss = { viewModel.setShowEditStatusDialog(false) },
            onConfirm = { viewModel.confirmAppointment() }
        )
    }

    selectedAppointment?.let { appointment ->
        if (showDetailsDialog) {
            DetailsDialog(
                appointment = appointment,
                onDismiss = { viewModel.setShowDetailsDialog(false) },
                onClickStatus = {
                    if (appointment.status == Status.PENDING) {
                        viewModel.setShowEditStatusDialog(true)
                    }
                },
                onMessage = {},
                onCommentUpdated = { newComment ->
                    viewModel.updateAppointmentComment(newComment)
                },
            )
        }
    }
}

val BookedColor = BabyBlue.copy(alpha = 0.6f)
val AvailableColor = Purple.copy(alpha = 0.4f)
val PastColor = Color(0xFFD6A6C2)
val CurrentTimeColor = Purple

/**
 * Displays today's schedule with a timeline of appointments.
 *
 * @param allAppointments A list of all appointments.
 * @param currentTime The current time.
 * @param startOfWorkday The start time of the workday.
 * @param endOfWorkday The end time of the workday.
 * @param onSelectAppointment Callback when an appointment slot is selected.
 */
@Composable
fun TodaysSchedule(
    allAppointments: List<Appointment>,
    currentTime: LocalTime = LocalTime.now(),
    startOfWorkday: LocalTime = LocalTime.of(9, 0),
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

                Spacer(modifier = Modifier.height(16.dp))
                TimelineLegend()

            }
        }
    }
}

/**
 * Displays a legend for the timeline colors.
 */
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

/**
 * Represents a single item in the timeline legend.
 *
 * @param color The color of the legend item.
 * @param label The text label for the legend item.
 */
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

/**
 * Displays the next appointment card.
 *
 * @param nearestAppointment The nearest upcoming appointment.
 * @param onShowEditStatusDialog Callback to show the edit status dialog.
 * @param onViewAll Callback to navigate to all appointments.
 * @param onShowDetailsDialog Callback to show the details dialog for an appointment.
 */
@Composable
fun NextAppointmentCard(
    nearestAppointment: Appointment?,
    onShowEditStatusDialog: () -> Unit,
    onViewAll: () -> Unit,
    onShowDetailsDialog: (Appointment) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Next Appointment",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = DefaultPrimary
                )
            )

            TextButton(onClick = onViewAll) {
                Text("View All", color = DefaultPrimary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (nearestAppointment == null) {
            EmptyState()
        } else {
            AppointmentCardContent(
                appointment = nearestAppointment,
                onShowEditStatusDialog = onShowEditStatusDialog,
                onShowDetailsDialog = onShowDetailsDialog
            )
        }
    }
}

/**
 * Displays the content of an appointment card.
 *
 * @param appointment The appointment to display.
 * @param onShowEditStatusDialog Callback to show the edit status dialog.
 * @param onShowDetailsDialog Callback to show the details dialog for the appointment.
 */
@Composable
private fun AppointmentCardContent(
    appointment: Appointment,
    onShowEditStatusDialog: () -> Unit,
    onShowDetailsDialog: (Appointment) -> Unit
) {
    val isToday = appointment.date == LocalDate.now()
    val isTomorrow = appointment.date == LocalDate.now().plusDays(1)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onShowDetailsDialog(appointment) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateBox(appointment.date)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = appointment.patientName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "$${"%.2f".format(appointment.price)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DefaultPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Time",
                            tint = DefaultPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = appointment.time.format(DateTimeFormatter.ofPattern("h:mm a")),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .width(1.dp)
                            .background(Color.LightGray)
                    )

                    Text(
                        text = appointment.type.displayName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = BabyBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                StatusRow(
                    status = appointment.status,
                    isToday = isToday,
                    isTomorrow = isTomorrow,
                    appointmentDate = appointment.date,
                    onClick = {
                        if (appointment.status == Status.PENDING) {
                            onShowEditStatusDialog()
                        }
                    }
                )
            }
        }
    }
}

/**
 * Displays a formatted date box for an appointment.
 *
 * @param date The date to display.
 */
@Composable
private fun DateBox(date: LocalDate) {
    Box(
        modifier = Modifier
            .width(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DefaultPrimary.copy(alpha = 0.1f))
            .border(
                width = 1.dp,
                color = DefaultPrimary,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.format(
                    DateTimeFormatter.ofPattern(
                        "MMM"
                    )
                ),
                style = MaterialTheme.typography.labelSmall,
                color = DefaultPrimary
            )
            Text(
                text = date.format(
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
                text = date.format(
                    DateTimeFormatter.ofPattern(
                        "EEE"
                    )
                ),
                style = MaterialTheme.typography.labelSmall,
                color = DefaultPrimary
            )
        }
    }
}

/**
 * Displays the status and relative time of an appointment.
 *
 * @param status The status of the appointment.
 * @param isToday True if the appointment is today.
 * @param isTomorrow True if the appointment is tomorrow.
 * @param appointmentDate The date of the appointment.
 * @param onClick Callback when the status is clicked.
 */
@Composable
private fun StatusRow(
    status: Status,
    isToday: Boolean,
    isTomorrow: Boolean,
    appointmentDate: LocalDate,
    onClick: () -> Unit
) {
    val statusColor = when (status) {
        Status.CONFIRMED -> Green
        Status.PENDING -> Yellow
        else -> Red
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(statusColor.copy(alpha = 0.1f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clickable(onClick = onClick),
        ) {
            Text(
                text = status.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = statusColor
            )
        }

        Text(
            text = when {
                isToday -> "Today"
                isTomorrow -> "Tomorrow"
                else -> "In ${ChronoUnit.DAYS.between(LocalDate.now(), appointmentDate)} days"
            },
            style = MaterialTheme.typography.labelMedium,
            color = when {
                isToday -> Purple
                isTomorrow -> Purple.copy(alpha = 0.8f)
                else -> DefaultOnPrimary.copy(alpha = 0.6f)
            }
        )
    }
}

/**
 * Displays an empty state message when there are no upcoming appointments.
 */
@Composable
private fun EmptyState() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = DefaultPrimary.copy(alpha = 0.3f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No upcoming appointments",
                style = MaterialTheme.typography.bodyMedium,
                color = DefaultOnPrimary.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Displays a row of statistics cards.
 *
 * @param todaysCount The count of appointments for today.
 * @param completedCount The count of completed appointments for today.
 */
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
        StatCard(
            value = todaysCount.toString(),
            label = "Today",
            icon = Icons.Default.Schedule,
            colorIcon = DefaultPrimary,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            value = (todaysCount - completedCount).toString(),
            label = "Upcoming",
            icon = Icons.Default.CalendarToday,
            colorIcon = BabyBlue,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            value = completedCount.toString(),
            label = "Completed",
            icon = Icons.Default.CheckCircle,
            colorIcon = Yellow,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Displays the header for the doctor's home screen.
 *
 * @param doctorData A map containing the doctor's data, including name and profile picture URL.
 * @param unreadNotificationsCount The number of unread notifications.
 * @param onNotificationClick Callback for notification icon click.
 * @param onProfileClick Callback for profile avatar click.
 */
@Composable
private fun DoctorHeader(
    doctorData: Map<String, Any>?,
    unreadNotificationsCount: Int,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
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
                text = "Welcome Dr. ${doctorData?.get("name") ?: ""}",
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
                onClick = {
                    onNotificationClick()
                },
                modifier = Modifier.height(40.dp)
            ) {
                if (unreadNotificationsCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(containerColor = DefaultPrimary) {
                                Text(
                                    text = unreadNotificationsCount.toString(),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = DefaultPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

            }


            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(DefaultPrimary.copy(alpha = 0.2f))
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                val profileUrl = doctorData?.get("profilePictureUrl") as? String
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

/**
 * Calculates the time remaining until the end of the workday.
 *
 * @param now The current time.
 * @param startOfWorkday The start time of the workday.
 * @param endOfWorkday The end time of the workday.
 * @return A string representing the time remaining or workday status.
 */
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

/**
 * Displays a carousel of medical news items.
 *
 * @param navController The navigation controller.
 * @param modifier The modifier for this composable.
 */
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

/**
 * Data class representing a news item.
 *
 * @param title The title of the news.
 * @param summary A brief summary of the news.
 * @param category The category of the news.
 * @param imageUrl The URL of the image for the news item.
 * @param time The time when the news was published.
 */
data class NewsItemData(
    val title: String,
    val summary: String,
    val category: String,
    val imageUrl: String,
    val time: String
)

/**
 * Displays a news card.
 *
 * @param title The title of the news.
 * @param summary A brief summary of the news.
 * @param category The category of the news.
 * @param imageUrl The URL of the image for the news item.
 * @param time The time when the news was published.
 * @param modifier The modifier for this composable.
 * @param onClick Callback when the news card is clicked.
 */
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

            Column(modifier = Modifier.weight(1f)) {
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

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

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

/**
 * Displays a confirmation dialog for an appointment.
 *
 * @param onDismiss Callback when the dialog is dismissed.
 * @param onConfirm Callback when the confirm button is clicked.
 */
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

/**
 * Displays a statistics card with an icon, value, and label.
 *
 * @param icon The icon to display.
 * @param value The value to display.
 * @param label The label for the statistic.
 * @param colorIcon The color of the icon.
 * @param modifier The modifier for this composable.
 */
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

/**
 * Displays a dialog with detailed information about an appointment.
 *
 * @param appointment The appointment to display details for.
 * @param onDismiss Callback when the dialog is dismissed.
 * @param onClickStatus Callback when the status chip is clicked.
 * @param onMessage Callback when the message button is clicked.
 * @param onCommentUpdated Callback when the appointment comment is updated.
 */
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            InfoChip(
                                text = appointment.status.displayName,
                                color = statusColor,
                                onClick = if (appointment.status == Status.PENDING) {
                                    onClickStatus
                                } else {
                                    {}
                                }
                            )

                            InfoChip(
                                text = "${appointment.type.durationInMinutes} min",
                                color = DefaultPrimary
                            )

                            InfoChip(
                                text = "$%.2f".format(appointment.price),
                                color = BabyBlue
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
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

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
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
                                        imageVector = Icons.AutoMirrored.Filled.Comment,
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