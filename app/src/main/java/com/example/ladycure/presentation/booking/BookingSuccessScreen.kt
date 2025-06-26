package com.example.ladycure.presentation.booking

import BabyBlue
import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import Purple
import YellowOrange
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
import com.example.ladycure.utility.SnackbarController

/**
 * Composable function that displays the booking success screen.
 *
 * @param navController The NavController used for navigation.
 * @param appointmentId The ID of the successfully booked appointment.
 * @param referralId The ID of the referral, if applicable.
 * @param snackbarController The SnackbarController to show messages.
 * @param viewModel The BookingSuccessViewModel to manage state and logic.
 */
@Composable
fun BookingSuccessScreen(
    navController: NavController,
    appointmentId: String,
    referralId: String? = null,
    snackbarController: SnackbarController,
    viewModel: BookingSuccessViewModel = viewModel()
) {
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val appointment = viewModel.appointment
    val context = LocalContext.current

    LaunchedEffect(appointmentId) {
        viewModel.loadAppointment(appointmentId)
    }

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
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(DefaultPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.kapi_happi),
                        contentDescription = "Success Icon",
                        modifier = Modifier.size(150.dp),
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

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Appointment Booked!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = DefaultPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 15.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your appointment has been successfully booked",
                style = MaterialTheme.typography.bodyLarge,
                color = DefaultOnPrimary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 15.dp)
            )


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DefaultBackground)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(32.dp))

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
                        modifier = Modifier.padding(20.dp)
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

                        Spacer(modifier = Modifier.height(12.dp))

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

                        Spacer(modifier = Modifier.height(12.dp))

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

                        Spacer(modifier = Modifier.height(12.dp))

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

                        Spacer(modifier = Modifier.height(16.dp))

                        Divider(color = Color.LightGray, thickness = 1.dp)

                        Spacer(modifier = Modifier.height(16.dp))

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
                                text = "$${"%.2f".format(appointment.type.price * 1.09)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DefaultPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

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

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f),
                    ),
                    elevation = CardDefaults.cardElevation(0.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
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

                Spacer(modifier = Modifier.height(32.dp))

            }
            OutlinedButton(
                onClick = { viewModel.addToCalendar(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 15.dp),
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