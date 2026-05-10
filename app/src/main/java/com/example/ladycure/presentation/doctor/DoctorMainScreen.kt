package com.example.ladycure.presentation.doctor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.ladycure.domain.model.Appointment.Status
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import com.example.ladycure.utility.SnackbarController

@Composable
fun DoctorHomeScreen(
    navController: NavHostController,
    snackbarController: SnackbarController,
    viewModel: DoctorHomeViewModel = hiltViewModel()
) {
    val dimens = rememberResponsiveDimens()
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
                    .padding(horizontal = dimens.w(0.039f), vertical = dimens.h(0.017f))
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
//                    }
//                )

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

                Spacer(modifier = Modifier.height(dimens.h(0.017f)))

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
