package com.example.ladycure

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import com.example.ladycure.utility.HealthTips.getDailyTip
import com.example.ladycure.utility.HealthTips.getRandomTip
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.data.Appointment
import com.example.ladycure.data.doctor.Speciality
import com.example.ladycure.presentation.home.components.AppointmentsSection
import com.example.ladycure.presentation.home.components.BookAppointmentSection
import com.example.ladycure.utility.SnackbarController
import java.time.LocalDate
import java.time.LocalTime


@Composable
fun HomeScreen(navController: NavHostController, snackbarController: SnackbarController? = null) {
    val authRepo = remember { AuthRepository() }

    val selectedSpeciality = remember { mutableStateOf<Speciality?>(null) }
    val userData = remember { mutableStateOf<Map<String, Any>?>(null) }
    val selectedCity = remember { mutableStateOf("Wrocław") }
    var error = remember { mutableStateOf<String?>(null) }
    val appointments = remember { mutableStateOf<List<Appointment>>(emptyList()) }

    LaunchedEffect(Unit) {
        val result = authRepo.getCurrentUserData()
        if (result.isSuccess) {
            userData.value = result.getOrNull()
        } else {
            error.value = result.exceptionOrNull()?.message
        }
        error.value = null
    }

    LaunchedEffect(userData.value) {
        val result = authRepo.getAppointments("user")
        if (result.isFailure) {
            error.value = result.exceptionOrNull()?.message
        } else {
            appointments.value = result.getOrNull() ?: emptyList()

            appointments.value = appointments.value.filter {
                it.date.isAfter(LocalDate.now()) || (it.date == LocalDate.now() && it.time >= LocalTime.now())
            }

            appointments.value = appointments.value.sortedWith(
                compareBy({ it.date }, { it.time })
            )

        }
        error.value = null
    }

    if (userData.value == null) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = DefaultPrimary,
                modifier = Modifier.size(48.dp)
            )
        }
    } else {
                if (error.value != null) {
                    snackbarController?.showMessage(
                        message = error.value ?: "An error occurred"
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DefaultBackground)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Header with greeting
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hii, ${userData.value?.get("name") ?: ""}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = DefaultPrimary
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { //navController.navigate("notifications")
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = DefaultPrimary,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.size(8.dp))

                            // User avatar
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(DefaultPrimary.copy(alpha = 0.2f))
                                    .clickable { navController.navigate("profile") },
                                contentAlignment = Alignment.Center
                            ) {
                                val profileUrl =  userData.value?.get("profilePictureUrl") as? String
                                if (profileUrl != null) {
                                    SubcomposeAsyncImage(
                                        model = profileUrl,
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        loading = {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = DefaultPrimary
                                            )
                                        },
                                        error = {
                                            Icon(
                                                imageVector = Icons.Default.AccountCircle,
                                                contentDescription = "Profile",
                                                tint = DefaultPrimary
                                            )
                                        }
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Profile",
                                        tint = DefaultPrimary,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                        }
                    }

                    // Health tips card
                    var dailyTip by remember { mutableStateOf(getDailyTip()) }

                    var setToTodays = remember { mutableStateOf(false) }

                    if (dailyTip != getDailyTip()) {
                        setToTodays.value = false
                    } else {
                        setToTodays.value = true
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row {
                                Text(
                                    text = "Daily Health Tip",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = DefaultOnPrimary,
                                    fontWeight = FontWeight.Normal
                                )
                                IconButton(
                                    onClick = { dailyTip = getRandomTip() },
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Regenerate Tip",
                                        tint = DefaultPrimary
                                    )
                                }
                                if (!setToTodays.value) {
                                    IconButton(
                                        onClick = { dailyTip = getDailyTip() },
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = "Todays Tip",
                                            tint = DefaultPrimary
                                        )
                                    }
                                }
                            }
                            Text(
                                text = dailyTip,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DefaultOnPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }

                    BookAppointmentSection(
                        navController = navController,
                        specialities = Speciality.entries,
                        onCitySelected = { city ->
                            selectedCity.value = city
                        },
                        onSpecializationSelected = { specialization ->
                            selectedSpeciality.value = specialization
                            navController.navigate("services/${selectedCity.value}/${specialization.displayName}")
                        }
                    )

                    AppointmentsSection(
                        appointments = appointments.value,
                    )
            }
    }

}
@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController(), snackbarController = null)
}