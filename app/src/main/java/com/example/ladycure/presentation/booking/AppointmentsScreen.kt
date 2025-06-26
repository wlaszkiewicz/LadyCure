package com.example.ladycure.presentation.booking

import BabyBlue
import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import Green
import Red
import Yellow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ladycure.domain.model.Appointment
import com.example.ladycure.domain.model.Appointment.Status
import com.example.ladycure.domain.model.Speciality
import com.example.ladycure.presentation.doctor.ConfirmAppointmentDialog
import com.example.ladycure.presentation.home.components.CancelConfirmationDialog
import com.example.ladycure.presentation.home.components.CancelSuccessDialog
import com.example.ladycure.presentation.home.components.InfoChip
import com.example.ladycure.presentation.home.components.ShowDetailsDialog
import com.example.ladycure.utility.SnackbarController
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Composable function for displaying the Appointments screen.
 * Allows users to view upcoming and past appointments, apply filters, and interact with appointment details.
 *
 * @param navController The [NavController] for navigation actions.
 * @param snackbarController The [SnackbarController] for displaying snackbar messages.
 * @param viewModel The [AppointmentViewModel] providing data and logic for the screen.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppointmentsScreen(
    navController: NavController,
    snackbarController: SnackbarController?,
    viewModel: AppointmentViewModel = viewModel()
) {
    val isLoading = viewModel.isLoading
    val error = viewModel.error
    val selectedAppointment = viewModel.selectedAppointment
    val showEditStatusDialog = viewModel.showEditStatusDialog
    val showFilters = viewModel.showFilters
    val role = viewModel.role

    LaunchedEffect(error) {
        error?.let { err ->
            snackbarController?.showMessage(err)
            viewModel.updateError(null)
        }
    }

    if (isLoading || role == null) {
        LoadingView()
        return
    }

    val tabs = listOf("Upcoming", "History")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "My Appointments",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DefaultPrimary
                    )
                )

                IconButton(
                    onClick = { viewModel.toggleFilters(!showFilters) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = "Filter",
                        tint = if (showFilters) DefaultPrimary else DefaultOnPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = showFilters,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                EnhancedFiltersSection(
                    role = role,
                    viewModel = viewModel
                )
            }

            ActiveFiltersRow(
                selectedSpecializations = if (role == "user") viewModel.selectedSpecializations else emptyList(),
                selectedDoctors = if (role == "user") viewModel.selectedDoctors else emptyList(),
                selectedTypes = if (role == "doctor") viewModel.selectedTypes.map { it.displayName } else emptyList(),
                selectedPatients = if (role == "doctor") viewModel.selectedPatients else emptyList(),
                selectedDate = viewModel.selectedDate,
                onRemoveSpecialization = { viewModel.toggleSpecializationFilter(it) },
                onRemoveDoctor = { viewModel.toggleDoctorFilter(it) },
                onRemoveType = { typeName ->
                    viewModel.toggleTypeFilter(
                        viewModel.allTypes.first { it.displayName == typeName }
                    )
                },
                onRemovePatient = { viewModel.togglePatientFilter(it) },
                onRemoveDate = { viewModel.setDateFilter(null) },
                role = role,
            )

            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                contentColor = DefaultPrimary,
                indicator = { tabPositions ->
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                            .height(3.dp)
                            .background(DefaultPrimary, RoundedCornerShape(12.dp))
                    )
                },
                divider = {
                    Divider(color = DefaultPrimary.copy(alpha = 0.2f))
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (index == 0) Icons.Default.CalendarToday else Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (pagerState.currentPage == index) DefaultPrimary else DefaultOnPrimary.copy(
                                        alpha = 0.6f
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = if (pagerState.currentPage == index) DefaultPrimary else DefaultOnPrimary.copy(
                                        alpha = 0.6f
                                    )
                                )
                            }
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        page == 0 -> AppointmentsList(
                            role = role,
                            appointments = viewModel.filteredFutureAppointments,
                            emptyMessage = "No upcoming appointments",
                            navController = navController,
                            onClickStatus = { appointment ->
                                viewModel.selectAppointment(appointment)
                                if (appointment.status == Status.PENDING) {
                                    viewModel.toggleEditStatusDialog(true)
                                }
                            },
                            onCommentUpdated = { appointmentId, newComment ->
                                viewModel.updateAppointmentComment(appointmentId, newComment)
                            },
                            tab = page,
                            onCancelAppointment = { appointmentId ->
                                viewModel.cancelAppointment(appointmentId)
                            }
                        )

                        else -> AppointmentsList(
                            role = role,
                            appointments = viewModel.filteredPastAppointments,
                            emptyMessage = "No appointments in history",
                            navController = navController,
                            onClickStatus = {},
                            onCommentUpdated = { appointmentId, newComment ->
                                viewModel.updateAppointmentComment(appointmentId, newComment)
                            },
                            tab = page,
                            onCancelAppointment = { /* Not applicable for past appointments */ }
                        )
                    }
                }
            }
        }

        if (showEditStatusDialog) {
            ConfirmAppointmentDialog(
                onDismiss = { viewModel.toggleEditStatusDialog(false) },
                onConfirm = {
                    viewModel.updateAppointmentStatus(Status.CONFIRMED)
                }
            )
        }
    }
}

/**
 * Composable function to display a list of appointments.
 *
 * @param role The role of the current user ("user" or "doctor").
 * @param appointments The list of appointments to display.
 * @param emptyMessage The message to display when the list of appointments is empty.
 * @param navController The [NavController] for navigation.
 * @param onClickStatus Callback for when the status chip of an appointment is clicked.
 * @param onCommentUpdated Callback for when an appointment's comment is updated.
 * @param onCancelAppointment Callback for when an appointment is cancelled.
 * @param tab The current tab index (0 for upcoming, 1 for history).
 * @param viewModel The [AppointmentViewModel] providing data and logic.
 */
@Composable
fun AppointmentsList(
    role: String,
    appointments: List<Appointment>,
    emptyMessage: String,
    navController: NavController,
    onClickStatus: (Appointment) -> Unit,
    onCommentUpdated: (String, String) -> Unit,
    onCancelAppointment: (String) -> Unit,
    tab: Int,
    viewModel: AppointmentViewModel = viewModel()
) {
    var showCancelSuccessDialog by remember { mutableStateOf(false) }

    if (appointments.isEmpty()) {
        EmptyAppointmentsView(message = emptyMessage)
    } else {
        val groupedAppointments = remember(appointments) {
            viewModel.groupAppointmentsByMonth(appointments)
        }.toMutableMap()

        if (tab == 1) {
            groupedAppointments.forEach { (key, monthAppointments) ->
                groupedAppointments[key] = monthAppointments.sortedByDescending { it.date }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedAppointments.forEach { (monthYear, monthAppointments) ->
                item {
                    Text(
                        text = monthYear,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DefaultOnPrimary.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .fillMaxWidth()
                    )
                }

                items(monthAppointments) { appointment ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        AppointmentCard(
                            role = role,
                            appointment = appointment,
                            navController = navController,
                            onCancel = {
                                onCancelAppointment(appointment.appointmentId)
                                showCancelSuccessDialog = true
                            },
                            onCommentUpdated = { appointmentId, newComment ->
                                onCommentUpdated(appointmentId, newComment)
                            },
                            onClickStatus = { onClickStatus(appointment) }
                        )
                    }
                }
            }
        }
    }

    if (showCancelSuccessDialog) {
        CancelSuccessDialog(
            onDismiss = { showCancelSuccessDialog = false },
        )
    }
}

/**
 * Composable function to display an individual appointment card.
 *
 * @param role The role of the current user ("user" or "doctor").
 * @param appointment The [Appointment] to display.
 * @param navController The [NavController] for navigation.
 * @param onCancel Callback for when the cancel action is triggered.
 * @param onCommentUpdated Callback for when the appointment comment is updated.
 * @param onClickStatus Callback for when the status chip is clicked.
 */
@Composable
fun AppointmentCard(
    role: String,
    appointment: Appointment,
    navController: NavController,
    onCancel: () -> Unit,
    onCommentUpdated: (String, String) -> Unit,
    onClickStatus: () -> Unit
) {
    val speciality = Speciality.fromDisplayName(appointment.type.speciality)
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showCancelConfirmationDialog by remember { mutableStateOf(false) }

    val statusColor = when (appointment.status) {
        Status.CONFIRMED -> Green
        Status.PENDING -> Yellow
        Status.CANCELLED -> Red
        Status.COMPLETED -> BabyBlue
    }
    val statusBackgroundColor = statusColor.copy(alpha = 0.1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { showDetailsDialog = true },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(DefaultPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(speciality.icon),
                            contentDescription = null,
                            tint = DefaultPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appointment.type.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DefaultOnPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (role == "user") "Dr. ${appointment.doctorName}" else appointment.patientName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = DefaultOnPrimary.copy(alpha = 0.7f)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                InfoChip(
                    text = appointment.status.displayName,
                    color = statusColor,
                    onClick = onClickStatus,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = appointment.date.format(DateTimeFormatter.ofPattern("EEE, MMM dd")),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = appointment.time.format(
                            DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DefaultOnPrimary.copy(alpha = 0.7f)
                        )
                    )
                }

                Text(
                    text = "$%.2f".format(appointment.price),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DefaultPrimary
                    )
                )
            }
        }
    }

    if (showDetailsDialog) {
        if (role == "doctor") {
            AppointmentDetailsDialog(
                role = role,
                appointment = appointment,
                onDismiss = { showDetailsDialog = false },
                onCancel = {
                    showDetailsDialog = false
                    onCancel()
                },
                onReschedule = {
                    navController.navigate("reschedule/${appointment.appointmentId}")
                    showDetailsDialog = false
                },
                onCommentUpdated = onCommentUpdated
            )
        } else if (role == "user") {
            ShowDetailsDialog(
                appointment = appointment,
                onDismiss = { showDetailsDialog = false },
                onCancel = {
                    showDetailsDialog = false
                    onCancel()
                },
                onReschedule = {
                    navController.navigate("reschedule/${appointment.appointmentId}")
                    showDetailsDialog = false
                }
            )
        }
    }

    if (showCancelConfirmationDialog) {
        CancelConfirmationDialog(
            onDismiss = { showCancelConfirmationDialog = false },
            onConfirm = {
                showCancelConfirmationDialog = false
                onCancel()
            },
            appointment = appointment
        )
    }
}

/**
 * Composable function to display the details of an appointment in a dialog.
 *
 * @param role The role of the current user ("user" or "doctor").
 * @param appointment The [Appointment] to display details for.
 * @param onDismiss Callback for when the dialog is dismissed.
 * @param onCancel Callback for when the appointment is cancelled from the dialog.
 * @param onReschedule Callback for when the appointment is rescheduled from the dialog.
 * @param onCommentUpdated Callback for when the appointment's comment is updated.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppointmentDetailsDialog(
    role: String,
    appointment: Appointment,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onReschedule: () -> Unit,
    onCommentUpdated: (String, String) -> Unit
) {
    val speciality = Speciality.fromDisplayName(appointment.type.speciality)
    var showCancelConfirmation by remember { mutableStateOf(false) }
    var showEditComment by remember { mutableStateOf(false) }
    var editedComment by remember { mutableStateOf(appointment.comments) }
    var isPreparationExpanded by remember { mutableStateOf(false) }

    val statusColor = when (appointment.status) {
        Status.CONFIRMED -> Green
        Status.PENDING -> Yellow
        else -> Red
    }
    val statusBackgroundColor = statusColor.copy(alpha = 0.1f)

    Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    DefaultPrimary.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(DefaultPrimary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(speciality.icon),
                                    contentDescription = null,
                                    tint = DefaultPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = appointment.type.displayName,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = if (role == "user") "Dr. ${appointment.doctorName}" else appointment.patientName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = DefaultOnPrimary.copy(alpha = 0.7f)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            androidx.compose.material3.Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = statusBackgroundColor,
                                border = BorderStroke(1.dp, statusColor)
                            ) {
                                Text(
                                    text = appointment.status.displayName,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            androidx.compose.material3.Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DefaultPrimary.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, DefaultPrimary)
                            ) {
                                Text(
                                    text = "${appointment.type.durationInMinutes} min",
                                    color = DefaultPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            androidx.compose.material3.Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BabyBlue.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, BabyBlue)
                            ) {
                                Text(
                                    text = "$%.2f".format(appointment.price),
                                    color = BabyBlue,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
                                ),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Location",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = DefaultOnPrimary.copy(alpha = 0.6f)
                            )
                        )
                        Text(
                            text = appointment.address.ifEmpty { "Not specified" },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    if (appointment.type.preparationInstructions.isNotEmpty()) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        isPreparationExpanded = !isPreparationExpanded
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Preparation Instructions",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = DefaultOnPrimary.copy(alpha = 0.6f)
                                    )
                                )
                                Icon(
                                    imageVector = if (isPreparationExpanded)
                                        Icons.Default.KeyboardArrowUp
                                    else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isPreparationExpanded)
                                        "Hide instructions"
                                    else "Show instructions",
                                    tint = DefaultPrimary
                                )
                            }

                            AnimatedVisibility(
                                visible = isPreparationExpanded,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Text(
                                    text = appointment.type.preparationInstructions,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }

                    if (role == "doctor" || appointment.comments.isNotEmpty()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Notes",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = DefaultOnPrimary.copy(alpha = 0.6f)
                                    )
                                )

                                if (role == "doctor") {
                                    IconButton(
                                        onClick = { showEditComment = !showEditComment },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (showEditComment)
                                                Icons.Default.Close
                                            else Icons.Default.Edit,
                                            contentDescription = if (showEditComment)
                                                "Close editor"
                                            else "Edit notes",
                                            tint = DefaultPrimary
                                        )
                                    }
                                }
                            }

                            if (showEditComment) {
                                Column {
                                    TextField(
                                        value = editedComment,
                                        onValueChange = { editedComment = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedIndicatorColor = DefaultPrimary,
                                            unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f)
                                        ),
                                        placeholder = {
                                            Text("Add notes about this appointment")
                                        },
                                        maxLines = 3
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = { showEditComment = false }
                                        ) {
                                            Text("Cancel")
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                onCommentUpdated(
                                                    appointment.appointmentId,
                                                    editedComment
                                                )
                                                showEditComment = false
                                            }
                                        ) {
                                            Text("Save")
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = appointment.comments.ifEmpty { "No notes added" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                if (appointment.status != Status.CANCELLED) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (role == "user") {
                            OutlinedButton(
                                onClick = { showCancelConfirmation = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Red
                                ),
                                border = BorderStroke(1.dp, Red.copy(alpha = 0.5f))
                            ) {
                                Text("Cancel Appointment")
                            }

                            Button(
                                onClick = onReschedule,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DefaultPrimary
                                )
                            ) {
                                Text("Reschedule")
                            }
                        } else {
                            Button(
                                onClick = { /* Handle message */ },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DefaultPrimary
                                )
                            ) {
                                Text("Message Patient")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCancelConfirmation) {
        CancelConfirmationDialog(
            onDismiss = { showCancelConfirmation = false },
            onConfirm = {
                onCancel()
                showCancelConfirmation = false
            },
            appointment = appointment
        )
    }
}

/**
 * Composable function to display a view when there are no appointments.
 *
 * @param message The message to display.
 */
@Composable
fun EmptyAppointmentsView(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = "Empty appointments",
            tint = DefaultPrimary.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium.copy(
                color = DefaultOnPrimary.copy(alpha = 0.6f)
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

/**
 * Composable function to display a loading indicator.
 */
@Composable
private fun LoadingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = DefaultPrimary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Loading your appointments...",
            color = DefaultOnPrimary,
            fontSize = 16.sp
        )
    }
}

/**
 * Composable function for the enhanced filters section.
 *
 * @param role The role of the current user ("user" or "doctor").
 * @param viewModel The [AppointmentViewModel] to interact with filter logic.
 * @param modifier The modifier for this composable.
 */
@Composable
fun EnhancedFiltersSection(
    role: String,
    viewModel: AppointmentViewModel,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
    ) {
        androidx.compose.material3.Surface(
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
                        .padding(16.dp),
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
                        modifier = Modifier.size(32.dp)
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
                        .padding(16.dp),
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

/**
 * Composable for a multi-select filter section.
 *
 * @param title The title of the filter section.
 * @param items The list of available items to filter by.
 * @param selectedItems The list of currently selected items.
 * @param onItemSelected Callback when an item is selected or deselected.
 * @param modifier The modifier for this composable.
 */
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

/**
 * Composable for a date filter section, allowing selection of "Today", "Tomorrow", or a custom date.
 *
 * @param selectedDate The currently selected date.
 * @param onDateSelected Callback when a date is selected.
 * @param onDateCleared Callback when the selected date is cleared.
 * @param modifier The modifier for this composable.
 */
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

/**
 * Composable for an enhanced filter chip.
 *
 * @param label The text label for the chip.
 * @param selected Whether the chip is currently selected.
 * @param onSelected Callback when the chip is clicked.
 * @param modifier The modifier for this composable.
 */
@Composable
fun EnhancedFilterChip(
    label: String,
    selected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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

/**
 * Composable function to display a row of active filters.
 *
 * @param selectedSpecializations List of selected specializations for filtering.
 * @param selectedDoctors List of selected doctors for filtering.
 * @param selectedTypes List of selected appointment types for filtering.
 * @param selectedPatients List of selected patients for filtering.
 * @param selectedDate The selected date for filtering.
 * @param onRemoveSpecialization Callback to remove a specialization filter.
 * @param onRemoveDoctor Callback to remove a doctor filter.
 * @param onRemoveType Callback to remove an appointment type filter.
 * @param onRemovePatient Callback to remove a patient filter.
 * @param onRemoveDate Callback to remove the date filter.
 * @param role The role of the current user ("user" or "doctor").
 */
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
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

/**
 * Composable for an active filter chip, displaying a label and a remove button.
 *
 * @param label The text label for the chip.
 * @param onRemove Callback when the remove button is clicked.
 * @param modifier The modifier for this composable.
 */
@Composable
fun ActiveFilterChip(
    label: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
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
                modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
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