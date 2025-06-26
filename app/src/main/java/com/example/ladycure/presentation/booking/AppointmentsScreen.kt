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
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.History
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ladycure.domain.model.Appointment.Status
import com.example.ladycure.domain.model.AppointmentSummary
import com.example.ladycure.domain.model.Speciality
import com.example.ladycure.presentation.doctor.ConfirmAppointmentDialog
import com.example.ladycure.presentation.doctor.DetailsDialog
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
    LaunchedEffect(viewModel.futureAppointments) {
    }

    LaunchedEffect(error) {
        error?.let { err ->
            snackbarController?.showMessage(err)
            viewModel.updateError(null)
        }
    }

    // Show loading state while role is being determined
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
                    onClick = {
                        if (role == "doctor") navController.navigate("doctor_main") else navController.navigate(
                            "home"
                        )
                    },
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

            // Filter panel
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

            // Active filters row
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
                            onCancelAppointment = { /* Not applicable for past appointments */ },
                            onLoadMore = { viewModel.loadMorePastAppointments() }
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

@Composable
fun AppointmentsList(
    role: String,
    appointments: List<AppointmentSummary>,
    emptyMessage: String,
    navController: NavController,
    onClickStatus: (AppointmentSummary) -> Unit,
    onCommentUpdated: (String, String) -> Unit,
    onCancelAppointment: (String) -> Unit,
    tab: Int,
    viewModel: AppointmentViewModel = viewModel(),
    onLoadMore: (() -> Unit)? = null
) {
    val currentAppointments by remember(
        viewModel.filteredFutureAppointments,
        viewModel.filteredPastAppointments
    ) {
        mutableStateOf(if (tab == 0) viewModel.filteredFutureAppointments else viewModel.filteredPastAppointments)
    }


    var showCancelSuccessDialog by remember { mutableStateOf(false) }

    if (appointments.isEmpty() || currentAppointments.isEmpty()) {
        EmptyAppointmentsView(message = emptyMessage)

        if (tab == 1) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { onLoadMore?.invoke() },
                    colors = ButtonDefaults.buttonColors(containerColor = DefaultPrimary)
                ) {
                    Text("Load More Appointments")
                }
            }
        }
    } else {
        val groupedAppointments = remember(appointments) {
            viewModel.groupAppointmentsByMonth(appointments, isUpcoming = tab == 0)
        }.toMutableMap()

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

            if (tab == 1) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { onLoadMore?.invoke() },
                            colors = ButtonDefaults.buttonColors(containerColor = DefaultPrimary)
                        ) {
                            Text("Load More Appointments")
                        }
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

@Composable
fun AppointmentCard(
    role: String,
    appointment: AppointmentSummary,
    navController: NavController,
    onCancel: () -> Unit,
    onCommentUpdated: (String, String) -> Unit,
    onClickStatus: () -> Unit,
    viewModel: AppointmentViewModel = viewModel()
) {
    val speciality = Speciality.fromDisplayName(appointment.enumType.speciality)
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showCancelConfirmationDialog by remember { mutableStateOf(false) }
    var showEditStatusDialog by remember { mutableStateOf(false) }

    val statusColor = when (appointment.status) {
        Status.CONFIRMED -> Green
        Status.PENDING -> Yellow
        Status.CANCELLED -> Red
        Status.COMPLETED -> BabyBlue
    }
    val isLoadingDetails = viewModel.isLoadingDetails
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                showDetailsDialog = true
                viewModel.loadDetailsForAppointment(
                    appointment.appointmentId,
                )

            },
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
                            text = appointment.enumType.displayName,
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
                    onClick = {
                        if (appointment.status == Status.PENDING) {
                            showEditStatusDialog = true
                        }
                    },
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

    if (showDetailsDialog && viewModel.selectedAppointment != null) {
        if (role == "doctor") {
            DetailsDialog(
                appointment = viewModel.selectedAppointment!!,
                onDismiss = { showDetailsDialog = false },
                onClickStatus = {
                    if (viewModel.selectedAppointment!!.status == Status.PENDING
                    ) {
                        showEditStatusDialog = true
                    }
                },
                onMessage = {},
                onCommentUpdated = { newComment ->
                    onCommentUpdated(appointment.appointmentId, newComment)

                    viewModel.updateAppointmentComment(
                        appointment.appointmentId,
                        newComment
                    )
                },
            )
        } else if (role == "user") {
            ShowDetailsDialog(
                appointment = viewModel.selectedAppointment!!,
                onDismiss = { showDetailsDialog = false },
                onCancel = {
                    showDetailsDialog = false
                    onCancel()
                },
                onReschedule = {
                    navController.navigate("reschedule/${appointment.appointmentId}")
                    showDetailsDialog = false
                },
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

    var coroutineScope = rememberCoroutineScope()


    if (showEditStatusDialog) {
        ConfirmAppointmentDialog(
            onDismiss = { showEditStatusDialog = false },
            onConfirm = {
                if (viewModel.selectedAppointment != null) {
                    viewModel.updateAppointmentStatus(Status.CONFIRMED)
                } else { // from the list
                    coroutineScope.launch {
                        val result = viewModel.appointmentRepo.updateAppointmentStatus(
                            appointmentId = appointment.appointmentId,
                            status = Status.CONFIRMED.displayName
                        )
                        if (result.isSuccess) {
                            viewModel.futureAppointments = viewModel.futureAppointments.map {
                                if (it.appointmentId == appointment.appointmentId) {
                                    it.copy(status = Status.CONFIRMED)
                                } else {
                                    it
                                }
                            }
                        }
                    }

                }
                showEditStatusDialog = false
            }
        )
    }
}


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


@Composable
fun EnhancedFiltersSection(
    role: String,
    viewModel: AppointmentViewModel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
    ) {
        Surface(
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
                // Header
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

@Composable
fun EnhancedFilterChip(
    label: String,
    selected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
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

@Composable
fun ActiveFilterChip(
    label: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
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


