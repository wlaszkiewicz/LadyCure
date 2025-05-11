package com.example.ladycure.screens.user

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import Red
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition.Center
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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.ladycure.data.Appointment
import com.example.ladycure.data.Appointment.Status
import com.example.ladycure.data.AppointmentType
import com.example.ladycure.data.doctor.Speciality
import com.example.ladycure.presentation.home.components.CancelConfirmationDialog
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.utility.SnackbarController
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun AppointmentsScreen(
    navController: NavController,
    snackbarController: SnackbarController?,
    authRepo: AuthRepository = AuthRepository()
) {
    // Existing state variables
    var futureAppointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var pastAppointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // New filter state variables
    var showFilters by remember { mutableStateOf(false) }
    var selectedSpecialization by remember { mutableStateOf<String?>(null) }
    var selectedDoctor by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    // Get unique specializations and doctors for filters
    val allSpecializations = remember(futureAppointments + pastAppointments) {
        (futureAppointments + pastAppointments).map { it.type.speciality }.distinct()
    }

    val allDoctors = remember(futureAppointments + pastAppointments) {
        (futureAppointments + pastAppointments).map { it.doctorName }.distinct()
    }

    // Filtered appointments
    val filteredFutureAppointments =
        remember(futureAppointments, selectedSpecialization, selectedDoctor, selectedDate) {
            futureAppointments.filter { appointment ->
                (selectedSpecialization == null || appointment.type.speciality == selectedSpecialization) &&
                        (selectedDoctor == null || appointment.doctorName == selectedDoctor) &&
                        (selectedDate == null || appointment.date == selectedDate)
            }
        }

    val filteredPastAppointments =
        remember(pastAppointments, selectedSpecialization, selectedDoctor, selectedDate) {
            pastAppointments.filter { appointment ->
                (selectedSpecialization == null || appointment.type.speciality == selectedSpecialization) &&
                        (selectedDoctor == null || appointment.doctorName == selectedDoctor) &&
                        (selectedDate == null || appointment.date == selectedDate)
            }
        }

    LaunchedEffect(Unit) {
        try {
            val result = authRepo.getAppointments("user")
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
        } catch (e: Exception) {
            error = e.message ?: "An error occurred"
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
            .padding(16.dp))
            {   Row(
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
                        modifier = Modifier.width(80.dp).padding(8.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize())
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
                                selectedDoctor = if (selectedDoctor == allDoctors[doctor]) null else allDoctors[doctor]
                            }
                        )
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
                    // Today button
                    FilterChip(
                        label = "Today",
                        selected = selectedDate == LocalDate.now(),
                        onSelected = {
                            selectedDate =
                                if (selectedDate == LocalDate.now()) null else LocalDate.now()
                        }
                    )

                    // Tomorrow button
                    FilterChip(
                        label = "Tomorrow",
                        selected = selectedDate == LocalDate.now().plusDays(1),
                        onSelected = {
                            selectedDate = if (selectedDate == LocalDate.now()
                                    .plusDays(1)
                            ) null else LocalDate.now().plusDays(1)
                        }
                    )

                    // Custom date picker
                    FilterChip(
                        label = "Pick date",
                        selected = selectedDate != null &&
                                selectedDate != LocalDate.now() &&
                                selectedDate != LocalDate.now().plusDays(1),
                        onSelected = {
                            // You'll need to implement a date picker dialog here
                            // For now, we'll just clear the date filter
                            selectedDate = null
                        }
                    )
                }

                // Clear all filters button
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
                        appointments = filteredFutureAppointments, // Use filtered list
                        emptyMessage = "No upcoming appointments",
                        navController = navController,
                        snackbarController = snackbarController!!
                    )

                    else -> AppointmentsList(
                        appointments = filteredPastAppointments, // Use filtered list
                        emptyMessage = "No past appointments",
                        navController = navController,
                        snackbarController = snackbarController!!
                    )
                }
            }
        }
    }
}

@Composable
fun AppointmentsList(
    appointments: List<Appointment>,
    emptyMessage: String,
    navController: NavController,
    snackbarController: SnackbarController
) {
    val coroutineScope = rememberCoroutineScope()
    val authRepo = AuthRepository()

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
                        appointment = appointment,
                        navController = navController,
                        onCancel = {
                            coroutineScope.launch {
                                try {
                                    val result = authRepo.cancelAppointment(appointment.appointmentId)
                                    if (result.isSuccess) {
                                        appointment.status = Status.CANCELLED

                                        snackbarController.showMessage("Appointment cancelled successfully")
                                    } else {
                                        snackbarController.showMessage("Failed to cancel appointment: ${result.exceptionOrNull()?.message}")
                                    }
                                } catch (e: Exception) {
                                    snackbarController.showMessage("Error: ${e.message}")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppointmentCard(
    appointment: Appointment,
    navController: NavController,
    onCancel: () -> Unit
) {
    val speciality = Speciality.fromDisplayName(appointment.type.speciality)
    val isPastAppointment = appointment.date.isBefore(LocalDate.now()) ||
            (appointment.date == LocalDate.now() && appointment.time.isBefore(LocalTime.now()))

    var isExpanded by remember { mutableStateOf(false) }
    var showCancelConfirmationDialog by remember { mutableStateOf(false) }

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
                            text = "Dr. ${appointment.doctorName}",
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
                        )
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
                        text = appointment.time.format(DateTimeFormatter.ofPattern("h:mm a", Locale.US)),
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

                    if (appointment.comments.isNotBlank()) {
                        Column {
                            Text(
                                text = "Comments",
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
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Action buttons for future appointments
                    if (!isPastAppointment && appointment.status != Status.CANCELLED) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Cancel button
                            OutlinedButton(
                                onClick = {
                                   showCancelConfirmationDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor =  Red.copy(alpha = 0.8f),
                                ),
                                border = BorderStroke(1.dp, Red.copy(alpha = 0.5f))
                            ) {
                                Text("Cancel")
                            }

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