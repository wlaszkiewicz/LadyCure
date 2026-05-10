package com.example.ladycure.presentation.booking

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ladycure.domain.model.AppointmentType
import com.example.ladycure.ui.theme.DefaultBackground
import com.example.ladycure.ui.theme.DefaultOnPrimary
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import com.example.ladycure.utility.SnackbarController
import com.google.firebase.Timestamp

@Composable
fun ConfirmationScreen(
    navController: NavController,
    snackbarController: SnackbarController?,
    doctorId: String,
    timestamp: Timestamp,
    appointmentType: AppointmentType,
    referralId: String? = null,
    viewModel: ConfirmationViewModel = hiltViewModel()
) {
    val dimens = rememberResponsiveDimens()
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val doctorInfo = viewModel.doctorInfo
    val referral = viewModel.referral
    val isUploading = viewModel.isUploading
    val showUploadSuccess = viewModel.showUploadSuccess
    val uploadProgress = viewModel.uploadProgress
    val tooLarge = viewModel.tooLarge
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadInitialData(doctorId, timestamp, referralId)
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { err ->
            snackbarController?.showMessage(err)
            viewModel.errorMessage = null
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.uploadReferral(
                    uri = it,
                    referralId = referralId.toString(),
                    appointmentType = appointmentType,
                    context = context,
                    onSuccess = { /* Success handled in ViewModel */ },
                    onError = { message ->
                        snackbarController?.showMessage(message)
                    }
                )
            }
        }
    )

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground),
            contentAlignment = Alignment.Center
        ) {
            ConfirmationLoadingView()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground)
                .padding(
                    top = dimens.h(20 / 914f),
                    bottom = dimens.h(16 / 914f),
                    start = dimens.w(16 / 411f),
                    end = dimens.w(16 / 411f)
                ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(dimens.w(48 / 411f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back",
                        tint = DefaultOnPrimary,
                    )
                }
                Spacer(modifier = Modifier.width(dimens.w(16 / 411f)))
                Text(
                    text = "Confirm Appointment",
                    style = MaterialTheme.typography.titleLarge,
                    color = DefaultOnPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            when {
                doctorInfo == null -> snackbarController?.showMessage("Doctor info is unavailable")
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = dimens.h(16 / 914f)),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White,
                                contentColor = DefaultPrimary
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = dimens.w(16 / 411f),
                                    vertical = dimens.h(16 / 914f)
                                )
                            ) {
                                Text(
                                    text = "Appointment Scheduled",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Date:", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = viewModel.formattedDate,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = DefaultOnPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Time:", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = viewModel.formattedTime,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = DefaultOnPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        AppointmentTypeCard(
                            appointmentType = appointmentType,
                            referralId = referralId,
                            modifier = Modifier.padding(bottom = dimens.h(16 / 914f))
                        )

                        if (referralId != null) {
                            ReferralInfoCard(
                                referral = referral,
                                onUploadNew = {
                                    pdfLauncher.launch("application/pdf")
                                },
                                modifier = Modifier.padding(bottom = dimens.h(16 / 914f)),
                                isUploading = isUploading,
                                uploadProgress = uploadProgress,
                                showUploadSuccess = showUploadSuccess
                            )
                        }

                        DoctorConfirmationCard(
                            doctor = doctorInfo,
                            modifier = Modifier.padding(bottom = dimens.h(16 / 914f))
                        )

                        LocationCard(
                            doctor = doctorInfo,
                            modifier = Modifier.padding(bottom = dimens.h(16 / 914f))
                        )

                        PaymentCard(
                            modifier = Modifier.padding(bottom = dimens.h(24 / 914f)),
                            appointmentType = appointmentType
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = dimens.h(16 / 914f)),
                            horizontalArrangement = Arrangement.spacedBy(dimens.w(16 / 411f))
                        ) {
                            OutlinedButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = DefaultOnPrimary,
                                ),
                                border = BorderStroke(1.dp, DefaultOnPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                enabled = !isUploading, // Disable if uploading
                                onClick = {
                                    viewModel.bookAppointment(
                                        doctorId = doctorId,
                                        timestamp = timestamp,
                                        appointmentType = appointmentType,
                                        onSuccess = { appointmentId ->
                                            if (referralId == null) {
                                                navController.navigate("booking_success/$appointmentId")
                                            } else {
                                                navController.navigate("booking_success/$appointmentId/$referralId")
                                            }
                                        },
                                        onError = { message ->
                                            snackbarController?.showMessage(message)
                                        }
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DefaultPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Confirm Booking")
                            }
                        }
                    }
                }
            }
        }
        if (tooLarge) {
            FileTooLargeDialog(
                onDismiss = { viewModel.tooLarge = false },
            )
        }
    }
}
