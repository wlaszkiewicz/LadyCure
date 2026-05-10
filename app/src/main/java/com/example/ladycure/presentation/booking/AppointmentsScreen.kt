package com.example.ladycure.presentation.booking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ladycure.domain.model.Appointment.Status
import com.example.ladycure.presentation.doctor.ConfirmAppointmentDialog
import com.example.ladycure.ui.theme.DefaultBackground
import com.example.ladycure.ui.theme.DefaultOnPrimary
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.FractionDimens
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import com.example.ladycure.utility.SnackbarController
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppointmentsScreen(
    navController: NavController,
    snackbarController: SnackbarController?,
    viewModel: AppointmentViewModel = hiltViewModel()
) {
    val dimens = rememberResponsiveDimens()
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
                    .padding(
                        horizontal = dimens.w(FractionDimens.paddingSmallW),
                        vertical = dimens.h(FractionDimens.paddingSmallH)
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        if (role == "doctor") navController.navigate("doctor_main") else navController.navigate(
                            "home"
                        )
                    },
                    modifier = Modifier.size(dimens.w(FractionDimens.iconMediumW))
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
                    modifier = Modifier.size(dimens.w(FractionDimens.iconMediumW))
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
                    .padding(horizontal = dimens.w(FractionDimens.paddingSmallW))
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
                modifier = Modifier.padding(horizontal = dimens.w(FractionDimens.paddingSmallW))
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
private fun LoadingView() {
    val dimens = rememberResponsiveDimens()
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = DefaultPrimary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(dimens.w(FractionDimens.iconMediumW))
        )
        Spacer(modifier = Modifier.height(dimens.h(FractionDimens.paddingSmallH)))
        Text(
            "Loading your appointments...",
            color = DefaultOnPrimary,
            fontSize = 16.sp
        )
    }
}
