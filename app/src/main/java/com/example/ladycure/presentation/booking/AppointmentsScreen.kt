package com.example.ladycure.presentation.booking

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import Red
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ladycure.domain.model.Appointment
import com.example.ladycure.domain.model.Appointment.Status
import com.example.ladycure.domain.model.Speciality
import com.example.ladycure.presentation.doctor.ConfirmAppointmentDialog
import com.example.ladycure.presentation.home.components.CancelConfirmationDialog
import com.example.ladycure.presentation.home.components.CancelSuccessDialog
import com.example.ladycure.utility.SnackbarController
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
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

    // Handle errors
    LaunchedEffect(error) {
        error?.let { err ->
            snackbarController?.showMessage(err)
            viewModel.updateError(null)
        }
    }

    val tabs = listOf("Upcoming", "History")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
    {
        // Header and filter toggle (same as before)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back",
                        tint = DefaultOnPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Appointments",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DefaultOnPrimary,
                )
            }

            IconButton(
                onClick = { viewModel.toggleFilters(!showFilters) },
                modifier = Modifier
                    .width(80.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Filter",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (showFilters) DefaultPrimary else DefaultOnPrimary,
                    )
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = "Filter appointments",
                        tint = if (showFilters) DefaultPrimary else DefaultOnPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Filter panel (updated to use ViewModel)
        AnimatedVisibility(
            visible = showFilters,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(DefaultBackground, RoundedCornerShape(8.dp))
            ) {
                if (role == "user") {
                    Text(
                        text = "Specialization",
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 120.dp),
                        modifier = Modifier.padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(viewModel.allSpecializations.size) { specialization ->
                            FilterChip(
                                label = viewModel.allSpecializations[specialization],
                                selected = viewModel.selectedSpecialization == viewModel.allSpecializations[specialization],
                                onSelected = {
                                    viewModel.setSpecializationFilter(
                                        if (viewModel.selectedSpecialization == viewModel.allSpecializations[specialization])
                                            null else viewModel.allSpecializations[specialization]
                                    )
                                }
                            )
                        }
                    }

                    Text(
                        text = "Doctor",
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 120.dp),
                        modifier = Modifier.padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(viewModel.allDoctors.size) { doctor ->
                            FilterChip(
                                label = viewModel.allDoctors[doctor],
                                selected = viewModel.selectedDoctor == viewModel.allDoctors[doctor],
                                onSelected = {
                                    viewModel.setDoctorFilter(
                                        if (viewModel.selectedDoctor == viewModel.allDoctors[doctor])
                                            null else viewModel.allDoctors[doctor]
                                    )
                                }
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Appointment Type",
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 120.dp),
                        modifier = Modifier.padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(viewModel.allTypes.size) { type ->
                            FilterChip(
                                label = viewModel.allTypes[type].displayName,
                                selected = viewModel.selectedTypes == viewModel.allTypes[type],
                                onSelected = {
                                    viewModel.setTypeFilter(
                                        if (viewModel.selectedTypes == viewModel.allTypes[type])
                                            null else viewModel.allTypes[type]
                                    )
                                }
                            )
                        }
                    }

                    Text(
                        text = "Patient",
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 120.dp),
                        modifier = Modifier.padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(viewModel.allPatients.size) { patient ->
                            FilterChip(
                                label = viewModel.allPatients[patient],
                                selected = viewModel.selectedPatient == viewModel.allPatients[patient],
                                onSelected = {
                                    viewModel.setPatientFilter(
                                        if (viewModel.selectedPatient == viewModel.allPatients[patient])
                                            null else viewModel.allPatients[patient]
                                    )
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "Date",
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = DefaultOnPrimary.copy(alpha = 0.8f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        label = "Today",
                        selected = viewModel.selectedDate == LocalDate.now(),
                        onSelected = {
                            viewModel.setDateFilter(
                                if (viewModel.selectedDate == LocalDate.now())
                                    null else LocalDate.now()
                            )
                        }
                    )

                    FilterChip(
                        label = "Tomorrow",
                        selected = viewModel.selectedDate == LocalDate.now().plusDays(1),
                        onSelected = {
                            viewModel.setDateFilter(
                                if (viewModel.selectedDate == LocalDate.now().plusDays(1))
                                    null else LocalDate.now().plusDays(1)
                            )
                        }
                    )

                    FilterChip(
                        label = "Pick date",
                        selected = viewModel.selectedDate != null &&
                                viewModel.selectedDate != LocalDate.now() &&
                                viewModel.selectedDate != LocalDate.now().plusDays(1),
                        onSelected = {
                            // Implement date picker dialog here
                            viewModel.setDateFilter(null)
                        }
                    )
                }

                Button(
                    onClick = { viewModel.clearAllFilters() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DefaultPrimary.copy(alpha = 0.1f),
                        contentColor = DefaultPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Clear all filters")
                }
            }
        }

        ActiveFiltersRow(
            selectedSpecialization = if (role == "user") {
                viewModel.selectedSpecialization
            } else {
                viewModel.selectedTypes?.displayName
            },
            selectedDoctor = if (role == "user") {
                viewModel.selectedDoctor
            } else {
                viewModel.selectedPatient
            },
            selectedDate = viewModel.selectedDate,
            onRemoveSpecialization = {
                if (role == "user") {
                    viewModel.setSpecializationFilter(null)
                } else {
                    viewModel.setTypeFilter(null)
                }
            },
            onRemoveDoctor = {
                if (role == "user") {
                    viewModel.setDoctorFilter(null)
                } else {
                    viewModel.setPatientFilter(null)
                }
            },
            onRemoveDate = { viewModel.setDateFilter(null) },
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

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
                Divider(color = DefaultPrimary.copy(alpha = 0.8f))
            }
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
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = title,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    },
                    selectedContentColor = DefaultPrimary,
                    unselectedContentColor = DefaultOnPrimary.copy(alpha = 0.6f)
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    slideIntoContainer(
                        towards = if (page > pagerState.currentPage) AnimatedContentTransitionScope.SlideDirection.Left
                        else AnimatedContentTransitionScope.SlideDirection.Right
                    ) + fadeIn() with fadeOut()
                }
            ) { targetPage ->
                when {
                    isLoading -> LoadingView()
                    targetPage == 0 -> AppointmentsList(
                        role = role!!,
                        appointments = viewModel.filteredFutureAppointments,
                        emptyMessage = "No upcoming appointments",
                        navController = navController,
                        snackbarController = snackbarController!!,
                        onClickStatus = { appointment ->
                            viewModel.selectAppointment(appointment)
                            if (appointment.status == Status.PENDING) {
                                viewModel.toggleEditStatusDialog(true)
                            }
                        },
                        onCommentUpdated = { appointmentId, newComment ->
                            viewModel.updateAppointmentComment(appointmentId, newComment)
                        },
                        onCancelAppointment = { appointmentId ->
                            viewModel.cancelAppointment(appointmentId)
                        }
                    )

                    else -> AppointmentsList(
                        role = role!!,
                        appointments = viewModel.filteredPastAppointments,
                        emptyMessage = "No past appointments",
                        navController = navController,
                        snackbarController = snackbarController!!,
                        onClickStatus = {},
                        onCommentUpdated = { appointmentId, newComment ->
                            viewModel.updateAppointmentComment(appointmentId, newComment)
                        },
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

@Composable
fun AppointmentsList(
    role: String,
    appointments: List<Appointment>,
    emptyMessage: String,
    navController: NavController,
    snackbarController: SnackbarController,
    onClickStatus: (Appointment) -> Unit,
    onCommentUpdated: (String, String) -> Unit,
    onCancelAppointment: (String) -> Unit
) {
    var showCancelSuccessDialog by remember { mutableStateOf(false) }

    if (appointments.isEmpty()) {
        EmptyAppointmentsView(message = emptyMessage)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(appointments) { appointment ->
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
                        onClickStatus = {
                            onClickStatus(appointment)
                        }
                    )
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
    appointment: Appointment,
    navController: NavController,
    onCancel: () -> Unit,
    onCommentUpdated: (String, String) -> Unit,
    onClickStatus: () -> Unit
) {
    val speciality = Speciality.fromDisplayName(appointment.type.speciality)
    val isPastAppointment = appointment.date.isBefore(LocalDate.now()) ||
            (appointment.date == LocalDate.now() && appointment.time.isBefore(LocalTime.now()))

    var isExpanded by remember { mutableStateOf(false) }
    var showCancelConfirmationDialog by remember { mutableStateOf(false) }
    var showEditComment by remember { mutableStateOf(false) }
    var editedComment by remember { mutableStateOf(appointment.comments) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, DefaultPrimary.copy(alpha = 0.1f)),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Compact header with doctor info and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Doctor info with avatar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar with speciality icon
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
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DefaultOnPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (role == "user") {
                                "Dr. ${appointment.doctorName}"
                            } else {
                                appointment.patientName
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = DefaultOnPrimary.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Status and price
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Status chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (appointment.status) {
                            Status.CONFIRMED -> Color(0xFFE8F5E9)
                            Status.PENDING -> Color(0xFFFFF8E1)
                            Status.CANCELLED -> Color(0xFFFFEBEE)
                        },
                        border = BorderStroke(
                            0.5.dp,
                            when (appointment.status) {
                                Status.CONFIRMED -> Color(0xFF81C784)
                                Status.PENDING -> Color(0xFFFFB74D)
                                Status.CANCELLED -> Color(0xFFE57373)
                            }
                        ),
                        onClick = onClickStatus
                    ) {
                        Text(
                            text = appointment.status.displayName,
                            color = when (appointment.status) {
                                Status.CONFIRMED -> Color(0xFF2E7D32)
                                Status.PENDING -> Color(0xFFF57C00)
                                Status.CANCELLED -> Color(0xFFC62828)
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "$%.2f".format(appointment.price),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f)
                    )
                }
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
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = DefaultOnPrimary
                    )
                    Text(
                        text = appointment.time.format(
                            DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.7f)
                    )
                }

                // Expand/collapse icon
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = DefaultPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Expandable content
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Divider
                    Divider(
                        color = DefaultPrimary.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Address if available
                        Column {
                            Text(
                                text = "Location",
                                style = MaterialTheme.typography.labelSmall,
                                color = DefaultOnPrimary.copy(alpha = 0.6f)
                            )
                            Text(
                                text = appointment.address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DefaultOnPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Column {
                            Text(
                                text = "Duration",
                                style = MaterialTheme.typography.labelSmall,
                                color = DefaultOnPrimary.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "${appointment.type.durationInMinutes} min",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DefaultOnPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column {
                        Text(
                            text = "Preparation Instructions",
                            style = MaterialTheme.typography.labelSmall,
                            color = DefaultOnPrimary.copy(alpha = 0.6f)
                        )
                        Text(
                            text = appointment.type.preparationInstructions,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DefaultOnPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (appointment.comments.isNotBlank() && (role == "user" || (role == "doctor" && isPastAppointment))) {
                        Column {
                            Text(
                                text = "Notes",
                                style = MaterialTheme.typography.labelSmall,
                                color = DefaultOnPrimary.copy(alpha = 0.6f)
                            )
                            Text(
                                text = appointment.comments,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DefaultOnPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (role == "doctor" && !isPastAppointment) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Notes",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DefaultOnPrimary.copy(alpha = 0.6f)
                                )

                                IconButton(
                                    onClick = { showEditComment = !showEditComment },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (showEditComment) Icons.Default.Close else Icons.Default.Edit,
                                        contentDescription = if (showEditComment) "Close" else "Edit",
                                        tint = DefaultPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = showEditComment,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column {
                                    TextField(
                                        value = editedComment,
                                        onValueChange = { editedComment = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White, RoundedCornerShape(8.dp)),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            disabledContainerColor = Color.White,
                                            focusedIndicatorColor = DefaultPrimary,
                                            unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f),
                                            focusedTextColor = DefaultOnPrimary,
                                            unfocusedTextColor = DefaultOnPrimary
                                        ),
                                        textStyle = MaterialTheme.typography.bodyMedium,
                                        placeholder = {
                                            Text(
                                                "Add notes about this appointment",
                                                color = DefaultOnPrimary.copy(alpha = 0.5f)
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

                                        Button(
                                            onClick = {
                                                onCommentUpdated(
                                                    appointment.appointmentId,
                                                    editedComment
                                                )
                                                showEditComment = false
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = DefaultPrimary,
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Save")
                                        }
                                    }
                                }
                            }

                            if (!showEditComment) {
                                Text(
                                    text = appointment.comments.ifEmpty { "No notes added" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DefaultOnPrimary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(13.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (!isPastAppointment && appointment.status != Status.CANCELLED && role == "user") {
                            // Cancel button
                            OutlinedButton(
                                onClick = {
                                    isExpanded = false
                                    showCancelConfirmationDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Red.copy(alpha = 0.8f),
                                ),
                                border = BorderStroke(1.dp, Red.copy(alpha = 0.5f))
                            ) {
                                Text("Cancel")
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Reschedule button
                            Button(
                                onClick = {
                                    navController.navigate("reschedule/${appointment.appointmentId}")
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DefaultPrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Reschedule")
                            }
                        }

                        if (appointment.status != Status.CANCELLED && role == "doctor") {

                            OutlinedButton(
                                onClick = {
                                    ///      navController.navigate("reschedule/${appointment.appointmentId}")
                                },
                                modifier = Modifier.fillMaxWidth(0.6f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = DefaultPrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Message")
                            }
                        }

                    }
                }
            }
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

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) DefaultPrimary else DefaultPrimary.copy(alpha = 0.1f),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) DefaultPrimary else DefaultPrimary.copy(alpha = 0.3f)
        ),
        onClick = onSelected
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else DefaultOnPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
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
            style = MaterialTheme.typography.titleMedium,
            color = DefaultOnPrimary.copy(alpha = 0.6f),
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
fun ActiveFiltersRow(
    selectedSpecialization: String?,
    selectedDoctor: String?,
    selectedDate: LocalDate?,
    onRemoveSpecialization: () -> Unit,
    onRemoveDoctor: () -> Unit,
    onRemoveDate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasFilters =
        selectedSpecialization != null || selectedDoctor != null || selectedDate != null

    AnimatedVisibility(
        visible = hasFilters,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filters:",
                style = MaterialTheme.typography.labelMedium,
                color = DefaultOnPrimary.copy(alpha = 0.8f),
                modifier = Modifier.padding(end = 8.dp)
            )

            if (selectedSpecialization != null) {
                ActiveFilterChip(
                    label = selectedSpecialization,
                    onRemove = onRemoveSpecialization
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            if (selectedDoctor != null) {
                ActiveFilterChip(
                    label = selectedDoctor,
                    onRemove = onRemoveDoctor
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            if (selectedDate != null) {
                val dateLabel = when (selectedDate) {
                    LocalDate.now() -> "Today"
                    LocalDate.now().plusDays(1) -> "Tomorrow"
                    else -> selectedDate.format(DateTimeFormatter.ofPattern("MMM d"))
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
        border = BorderStroke(1.dp, DefaultPrimary.copy(alpha = 0.3f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = DefaultOnPrimary,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp)
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove filter",
                    tint = DefaultPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}


