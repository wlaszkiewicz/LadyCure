package com.example.ladycure.presentation.booking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.example.ladycure.ui.theme.BabyBlue
import com.example.ladycure.ui.theme.DefaultOnPrimary
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.FractionDimens
import com.example.ladycure.ui.theme.Green
import com.example.ladycure.ui.theme.Red
import com.example.ladycure.ui.theme.Yellow
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class)
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
    viewModel: AppointmentViewModel = hiltViewModel(),
    onLoadMore: (() -> Unit)? = null
) {
    val dimens = rememberResponsiveDimens()
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
            if (!appointments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dimens.w(FractionDimens.paddingSmallW),
                            vertical = dimens.h(FractionDimens.paddingSmallH)
                        ),
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
                            .padding(
                                horizontal = dimens.w(FractionDimens.spacingTinyW),
                                vertical = 8.dp
                            )
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
                            .padding(
                                horizontal = dimens.w(FractionDimens.paddingSmallW),
                                vertical = dimens.h(FractionDimens.paddingSmallH)
                            ),
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
    viewModel: AppointmentViewModel = hiltViewModel()
) {
    val dimens = rememberResponsiveDimens()
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
            .padding(horizontal = dimens.w(FractionDimens.paddingSmallW), vertical = 8.dp)
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
            modifier = Modifier.padding(
                horizontal = dimens.w(FractionDimens.paddingSmallW),
                vertical = dimens.h(FractionDimens.paddingSmallH)
            )
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
                            .size(dimens.w(FractionDimens.iconMediumW))
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

                    Spacer(modifier = Modifier.width(dimens.w(FractionDimens.spacingTinyW)))

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

            Spacer(modifier = Modifier.height(dimens.h(FractionDimens.spacingTinyH)))

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
    val dimens = rememberResponsiveDimens()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = dimens.w(FractionDimens.paddingMediumW),
                vertical = dimens.h(FractionDimens.paddingMediumH)
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = "Empty appointments",
            tint = DefaultPrimary.copy(alpha = 0.3f),
            modifier = Modifier.size(dimens.w(80 / 411f))
        )
        Spacer(modifier = Modifier.height(dimens.h(24 / 914f)))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium.copy(
                color = DefaultOnPrimary.copy(alpha = 0.6f)
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = dimens.w(FractionDimens.paddingMediumW))
        )
    }
}
