package com.example.ladycure.screens.user

import BabyBlue
import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import YellowOrange
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.ThumbUpAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ladycure.data.Appointment
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.utility.SnackbarController
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun BookingSuccessScreen(
    navController: NavController,
    appointmentId: String,
    referralId: String? = null,
    snackbarController: SnackbarController,
    authRepo: AuthRepository = AuthRepository()
) {
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    var appointment by remember { mutableStateOf<Appointment?>(null) }

    LaunchedEffect(appointmentId) {
        try {
            val result = authRepo.getAppointmentById(appointmentId)
            if (result.isSuccess) {
                appointment = result.getOrNull()
            } else {
                errorMessage.value =
                    "Failed to load appointment details: ${result.exceptionOrNull()?.message}"
            }
            isLoading.value = false
        } catch (e: Exception) {
            errorMessage.value = "Error loading appointment details: ${e.message}"
            isLoading.value = false
        }
    }
    // Animation states
    var animationPlayed by remember { mutableStateOf(false) }
    val transition = rememberInfiniteTransition()
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    errorMessage.value?.let {
        snackbarController.showMessage(it)
    }


    val context = LocalContext.current

    fun parseDateTimeToMillis(dateStr: String, timeStr: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dateTimeStr = "$dateStr ${timeStr.substring(0, 5)}"
        return dateFormat.parse(dateTimeStr)?.time ?: System.currentTimeMillis()
    }

    // Function to add event to calendar
    fun addToCalendar() {
        val appointment = appointment ?: return

        try {
            val intent = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(
                    CalendarContract.Events.TITLE,
                    "Appointment with Dr. ${appointment.doctorName}"
                )
                .putExtra(
                    CalendarContract.Events.DESCRIPTION,
                    "Appointment for ${appointment.type.displayName} at LadyCure Clinic"
                )
                .putExtra(
                    CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                    parseDateTimeToMillis(appointment.date.toString(), appointment.time.toString())
                )
                .putExtra(
                    CalendarContract.EXTRA_EVENT_END_TIME,
                    parseDateTimeToMillis(
                        appointment.date.toString(),
                        appointment.time.toString()
                    ) + appointment.type.durationInMinutes * 60 * 1000
                )
                .putExtra(CalendarContract.Events.EVENT_LOCATION, appointment.address)
                .putExtra(
                    CalendarContract.Events.AVAILABILITY,
                    CalendarContract.Events.AVAILABILITY_BUSY
                )

            context.startActivity(intent)
        } catch (e: Exception) {
            snackbarController.showMessage("Failed to open calendar: ${e.message}")
        }
    }

    if (isLoading.value || appointment == null) {
        // Show loading indicator
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Animated checkmark
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(DefaultPrimary.copy(alpha = 0.1f))
                    .scale(if (animationPlayed) pulse else 1f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EventAvailable,
                    contentDescription = "Success",
                    tint = DefaultPrimary,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Appointment Confirmed!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = DefaultPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your appointment has been successfully booked",
                style = MaterialTheme.typography.bodyLarge,
                color = DefaultOnPrimary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                            text = appointment?.type!!.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = DefaultPrimary
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
                            text = formatConfirmationDate(appointment!!.date.toString()),
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
                            text = formatConfirmationTime(appointment!!.time.toString()),
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
                            text = "Dr. ${appointment!!.doctorName}",
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
                            text = "$${"%.2f".format(appointment?.type!!.price * 1.09)}", // Price + 9% tax
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DefaultPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (appointment?.type!!.needsReferral && referralId == null) {
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

            // Reminder card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = DefaultPrimary.copy(alpha = 0.1f),
                    contentColor = DefaultPrimary
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
                            tint = DefaultPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reminder",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = DefaultPrimary
                        )
                    }
                    Text(
                        text = "• Arrive 15 minutes before your appointment\n" +
                                "• Bring your insurance card if applicable\n" +
                                "• ${appointment?.type!!.preparationInstructions}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f),
                    )

                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        navController.popBackStack()
                        navController.navigate("home")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DefaultPrimary.copy(alpha = 0.8f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(17.dp)
                ) {
                    Text(
                        text = "Back to Home",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        addToCalendar()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    border = BorderStroke(1.dp, DefaultPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DefaultPrimary,
                        containerColor = Color.Transparent
                    ), shape = RoundedCornerShape(17.dp)

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

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
