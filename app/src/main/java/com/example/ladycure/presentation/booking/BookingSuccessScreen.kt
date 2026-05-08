package com.example.ladycure.presentation.booking

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.ThumbUpAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ladycure.R
import com.example.ladycure.ui.theme.BabyBlue
import com.example.ladycure.ui.theme.DefaultBackground
import com.example.ladycure.ui.theme.DefaultOnPrimary
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.Purple
import com.example.ladycure.ui.theme.YellowOrange
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import com.example.ladycure.utility.SnackbarController

@Composable
fun BookingSuccessScreen(
    navController: NavController,
    appointmentId: String,
    referralId: String? = null,
    snackbarController: SnackbarController,
    viewModel: BookingSuccessViewModel = viewModel()
) {
    val dimens = rememberResponsiveDimens()
    // Collect state from ViewModel
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val appointment = viewModel.appointment
    val context = LocalContext.current

    // Initialize data loading
    LaunchedEffect(appointmentId) {
        viewModel.loadAppointment(appointmentId)
    }

    // Handle errors
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarController.showMessage(it)
            viewModel.errorMessage = null
        }
    }


    if (isLoading || appointment == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DefaultOnPrimary.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))
                CircularProgressIndicator()
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground)
                .padding(horizontal = dimens.w(16 / 411f), vertical = 8.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimens.h(180 / 914f))
            ) {
                Box(
                    modifier = Modifier
                        .size(dimens.w(170 / 411f))
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(DefaultPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.kapi_happi),
                        contentDescription = "Success Icon",
                        modifier = Modifier.size(dimens.w(150 / 411f)),
                        tint = Color.Unspecified
                    )
                }

                IconButton(
                    onClick = { navController.navigate("home") },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DefaultOnPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimens.h(32 / 914f)))

            Text(
                text = "Appointment Booked!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = DefaultPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = dimens.w(15 / 411f))
            )

            Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))

            Text(
                text = "Your appointment has been successfully booked",
                style = MaterialTheme.typography.bodyLarge,
                color = DefaultOnPrimary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = dimens.w(15 / 411f))
            )


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DefaultBackground)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = dimens.w(15 / 411f)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(dimens.h(32 / 914f)))

                // Appointment summary card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = dimens.w(20 / 411f),
                            vertical = dimens.h(20 / 914f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Service:",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = appointment.type.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Purple
                            )
                        }

                        Spacer(modifier = Modifier.height(dimens.h(12 / 914f)))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Date:",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = viewModel.formattedDate,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Spacer(modifier = Modifier.height(dimens.h(12 / 914f)))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Time:",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = viewModel.formattedTime,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Spacer(modifier = Modifier.height(dimens.h(12 / 914f)))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Doctor:",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Dr. ${appointment.doctorName}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))

                        Divider(color = Color.LightGray, thickness = 1.dp)

                        Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$${"%.2f".format(appointment.type.price * 1.09)}", // Price + 9% tax
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DefaultPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))

                        if (appointment.type.needsReferral && referralId == null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PriorityHigh,
                                    contentDescription = "Referral",
                                    modifier = Modifier.size(20.dp),
                                    tint = YellowOrange
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Don't forget to bring your referral letter!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = YellowOrange
                                )
                            }
                        } else if (referralId != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ThumbUpAlt,
                                    contentDescription = "Referral",
                                    modifier = Modifier.size(20.dp),
                                    tint = BabyBlue
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Referral letter uploaded successfully!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BabyBlue
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimens.h(32 / 914f)))

                // Reminder card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f),
                    ),
                    elevation = CardDefaults.cardElevation(0.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = dimens.w(16 / 411f),
                            vertical = dimens.h(16 / 914f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Reminder",
                                modifier = Modifier.size(20.dp),
                                tint = Purple
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Reminder",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Purple
                            )
                        }
                        Text(
                            text = "• Arrive 15 minutes before your appointment\n" +
                                    "• Bring your insurance card if applicable\n" +
                                    "• ${appointment.type.preparationInstructions}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DefaultOnPrimary.copy(alpha = 0.8f),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimens.h(32 / 914f)))

            }
            OutlinedButton(
                onClick = { viewModel.addToCalendar(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimens.h(50 / 914f))
                    .padding(horizontal = dimens.w(15 / 411f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, DefaultPrimary),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = DefaultPrimary
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Add to calendar",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add to Calendar",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
