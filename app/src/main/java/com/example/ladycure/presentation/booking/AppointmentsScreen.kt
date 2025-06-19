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
import androidx.navigation.NavController
import com.example.ladycure.data.repository.AppointmentRepository
import com.example.ladycure.data.repository.UserRepository
import com.example.ladycure.domain.model.Appointment
import com.example.ladycure.domain.model.Appointment.Status
import com.example.ladycure.domain.model.AppointmentType
import com.example.ladycure.domain.model.Speciality
import com.example.ladycure.presentation.doctor.ConfirmAppointmentDialog
import com.example.ladycure.presentation.home.components.CancelConfirmationDialog
import com.example.ladycure.presentation.home.components.CancelSuccessDialog
import com.example.ladycure.utility.SnackbarController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(
    ExperimentalAnimationApi::class
)
@Composable
fun AppointmentsScreen(
    navController: NavController,
    snackbarController: SnackbarController?,
    userRepo: UserRepository = UserRepository(),
    appointmentRepo: AppointmentRepository = AppointmentRepository()
) {
    // Existing state variables
    var futureAppointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var pastAppointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }
    var showEditStatusDialog by remember { mutableStateOf(false) }

    // New filter state variables
    var showFilters by remember { mutableStateOf(false) }
    var selectedSpecialization by remember { mutableStateOf<String?>(null) }
    var selectedDoctor by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTypes by remember { mutableStateOf<AppointmentType?>(null) }
    var selectedPatient by remember { mutableStateOf<String?>(null) }
    var role by remember { mutableStateOf<String?>(null) }

    // Get unique specializations and doctors for filters
    val allSpecializations = remember(futureAppointments + pastAppointments) {
        (futureAppointments + pastAppointments).map { it.type.speciality }.distinct()
    }

    val allDoctors = remember(futureAppointments + pastAppointments) {
        (futureAppointments + pastAppointments).map { it.doctorName }.distinct()
    }

    val allPatients = remember(futureAppointments + pastAppointments) {
        (futureAppointments + pastAppointments).map { it.patientName }.distinct()
    }

    val allTypes = remember(futureAppointments + pastAppointments) {
        (futureAppointments + pastAppointments).map { it.type }.distinct()
    }


    // Filtered appointments
    val filteredFutureAppointments =
        if (role == "user") {
            remember(futureAppointments, selectedSpecialization, selectedDoctor, selectedDate) {
                futureAppointments.filter { appointment ->
                    (selectedSpecialization == null || appointment.type.speciality == selectedSpecialization) &&
                            (selectedDoctor == null || appointment.doctorName == selectedDoctor) &&
                            (selectedDate == null || appointment.date == selectedDate)
                }
            }
        } else {
            remember(futureAppointments, selectedTypes, selectedPatient, selectedDate) {
                futureAppointments.filter { appointment ->
                    (selectedTypes == null || appointment.type == selectedTypes) &&
                            (selectedPatient == null || appointment.patientName == selectedPatient) &&
                            (selectedDate == null || appointment.date == selectedDate)
                }
            }
        }

    val filteredPastAppointments = if (role == "user") {
        remember(pastAppointments, selectedSpecialization, selectedDoctor, selectedDate) {
            pastAppointments.filter { appointment ->
                (selectedSpecialization == null || appointment.type.speciality == selectedSpecialization) &&
                        (selectedDoctor == null || appointment.doctorName == selectedDoctor) &&
                        (selectedDate == null || appointment.date == selectedDate)
            }
        }
    } else {
        remember(pastAppointments, selectedTypes, selectedPatient, selectedDate) {
            pastAppointments.filter { appointment ->
                (selectedTypes == null || appointment.type == selectedTypes) &&
                        (selectedPatient == null || appointment.patientName == selectedPatient) &&
                        (selectedDate == null || appointment.date == selectedDate)
            }
        }
    }


    LaunchedEffect(Unit) {
        try {
            val result = userRepo.getUserRole()
            if (result.isSuccess) {
                role = result.getOrNull()
                val result = appointmentRepo.getAppointments(role!!)
                if (result.isSuccess) {
                    val allAppointments = result.getOrNull() ?: emptyList()
                    futureAppointments = allAppointments.filter {
                        it.date.isAfter(LocalDate.now()) ||
                                (it.date == LocalDate.now() && it.time.isAfter(LocalTime.now()))
                    }.sortedWith(compareBy({ it.date }, { it.time }))

                    pastAppointments = allAppointments.filter {
                        (it.date.isBefore(LocalDate.now()) ||
                                (it.date == LocalDate.now() && it.time.isBefore(LocalTime.now())))
                        //       && it.status != Status.PENDING
                    }.sortedWith(compareBy({ it.date }, { it.time })).reversed()
                } else {
                    error = result.exceptionOrNull()?.message ?: "Failed to load appointments"
                }
                isLoading = false
            } else {
                error = result.exceptionOrNull()?.message ?: "Failed to load user role"
            }
        } catch (e: Exception) {
            error = e.message ?: "An error occurred loading appointments"
            isLoading = false
        }
    }


    LaunchedEffect(error) {
        error?.let {
            snackbarController!!.showMessage(it)
            error = null
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
        Row(
            modifier = Modifier
                .fillMaxWidth(),
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

            // Add filter toggle button
            IconButton(
                onClick = { showFilters = !showFilters },
                modifier = Modifier
                    .width(80.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                )
                {
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

        // Add filter panel
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
                // Specialization filter
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
                        items(allSpecializations.size) { specialization ->
                            FilterChip(
                                label = allSpecializations[specialization],
                                selected = selectedSpecialization == allSpecializations[specialization],
                                onSelected = {
                                    selectedSpecialization =
                                        if (selectedSpecialization == allSpecializations[specialization]) null else allSpecializations[specialization]
                                }
                            )
                        }
                    }

                    // Doctor filter
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
                        items(allDoctors.size) { doctor ->
                            FilterChip(
                                label = allDoctors[doctor],
                                selected = selectedDoctor == allDoctors[doctor],
                                onSelected = {
                                    selectedDoctor =
                                        if (selectedDoctor == allDoctors[doctor]) null else allDoctors[doctor]
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
                        items(allTypes.size) { type ->
                            FilterChip(
                                label = allTypes[type].displayName,
                                selected = selectedTypes == allTypes[type],
                                onSelected = {
                                    selectedTypes =
                                        if (selectedTypes == allTypes[type]) null else allTypes[type]
                                }
                            )
                        }
                    }

                    // Doctor filter
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
                        items(allPatients.size) { patient ->
                            FilterChip(
                                label = allPatients[patient],
                                selected = selectedPatient == allPatients[patient],
                                onSelected = {
                                    selectedPatient =
                                        if (selectedPatient == allPatients[patient]) null else allPatients[patient]
                                }
                            )
                        }
                    }

                }

                // Date filter
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
                        selected = selectedDate == LocalDate.now(),
                        onSelected = {
                            selectedDate =
                                if (selectedDate == LocalDate.now()) null else LocalDate.now()
                        }
                    )

                    FilterChip(
                        label = "Tomorrow",
                        selected = selectedDate == LocalDate.now().plusDays(1),
                        onSelected = {
                            selectedDate = if (selectedDate == LocalDate.now()
                                    .plusDays(1)
                            ) null else LocalDate.now().plusDays(1)
                        }
                    )

                    FilterChip(
                        label = "Pick date",
                        selected = selectedDate != null &&
                                selectedDate != LocalDate.now() &&
                                selectedDate != LocalDate.now().plusDays(1),
                        onSelected = {
                            //  implement a date picker dialog here
                            selectedDate = null
                        }
                    )
                }

                Button(
                    onClick = {
                        selectedSpecialization = null
                        selectedDoctor = null
                        selectedDate = null
                    },
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
                selectedSpecialization
            } else {
                selectedTypes?.displayName
            },
            selectedDoctor = if (role == "user") {
                selectedDoctor
            } else {
                selectedPatient
            },
            selectedDate = selectedDate,
            onRemoveSpecialization = {
                if (role == "user") {
                    selectedSpecialization = null
                } else {
                    selectedTypes = null
                }
            },
            onRemoveDoctor = {
                if (role == "user") {
                    selectedDoctor = null
                } else {
                    selectedPatient = null
                }
            },
            onRemoveDate = { selectedDate = null },
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
                        appointments = filteredFutureAppointments, // Use filtered list
                        emptyMessage = "No upcoming appointments",
                        navController = navController,
                        snackbarController = snackbarController!!,
                        onClickStatus = { appointment ->
                            selectedAppointment = appointment
                            if (appointment.status == Status.PENDING) {
                                showEditStatusDialog = true
                            }
                        },
                        onCommentUpdated = { appointmentId, newComment ->
                            futureAppointments = futureAppointments.map {
                                if (it.appointmentId == appointmentId) {
                                    it.copy(comments = newComment)
                                } else {
                                    it
                                }
                            }
                        }
                    )

                    else -> AppointmentsList(
                        role = role!!,
                        appointments = filteredPastAppointments, // Use filtered list
                        emptyMessage = "No past appointments",
                        navController = navController,
                        snackbarController = snackbarController!!,
                        onClickStatus = {},
                        onCommentUpdated = { appointmentId, newComment ->
                            futureAppointments = futureAppointments.map {
                                if (it.appointmentId == appointmentId) {
                                    it.copy(comments = newComment)
                                } else {
                                    it
                                }
                            }
                        }
                    )
                }
            }
        }
    }
    if (showEditStatusDialog) {
        ConfirmAppointmentDialog(
            onDismiss = { showEditStatusDialog = false },
            onConfirm = {
                CoroutineScope(Dispatchers.IO).launch {
                    val result = appointmentRepo.updateAppointmentStatus(
                        appointmentId = selectedAppointment!!.appointmentId,
                        status = Status.CONFIRMED.displayName
                    )
                    if (result.isFailure) {
                        snackbarController!!.showMessage(result.exceptionOrNull()?.message!!)
                    } else {
                        // Change the status of the appointment in the list
                        futureAppointments = futureAppointments.map {
                            if (it.appointmentId == selectedAppointment!!.appointmentId) {
                                it.copy(status = Status.CONFIRMED)
                            } else {
                                it
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
fun AppointmentsList(
    role: String,
    appointments: List<Appointment>,
    emptyMessage: String,
    navController: NavController,
    snackbarController: SnackbarController,
    onClickStatus: (Appointment) -> Unit,
    onCommentUpdated: (String, String) -> Unit
) {
    var showCancelSuccessDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val appointmentRepo = AppointmentRepository()

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
                            coroutineScope.launch {
                                try {
                                    val result =
                                        appointmentRepo.cancelAppointment(appointment.appointmentId)
                                    if (result.isSuccess) {
                                        appointment.status = Status.CANCELLED
                                        showCancelSuccessDialog = true
                                    } else {
                                        snackbarController.showMessage("Failed to cancel appointment: ${result.exceptionOrNull()?.message}")
                                    }
                                } catch (e: Exception) {
                                    snackbarController.showMessage("Error: ${e.message}")
                                }
                            }
                        },
                        onCommentUpdated = { appointmentId, newComment ->
                            coroutineScope.launch {
                                val result = appointmentRepo.updateAppointmentComment(
                                    appointment.appointmentId,
                                    newComment
                                )
                                if (result.isFailure) {
                                    snackbarController.showMessage(
                                        result.exceptionOrNull()?.message
                                            ?: "Failed to update comment"
                                    )
                                } else {
                                    onCommentUpdated(appointment.appointmentId, newComment)
                                }
                            }
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
                            DateTimeFormatter.ofPattern(
                                "h:mm a",
                                Locale.US
                            )
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


