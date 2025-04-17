package com.example.ladycure

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import LadyCureTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.ladycure.HealthTips.getDailyTip
import com.example.ladycure.HealthTips.getRandomTip
import com.example.ladycure.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.ladycure.data.Appointment
import com.example.ladycure.data.AppointmentType
import com.example.ladycure.data.doctor.Specialization
import com.example.ladycure.data.Status
import com.example.ladycure.presentation.home.components.AppointmentsSection
import com.example.ladycure.presentation.home.components.BookAppointmentSection


@Composable
fun QuickActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DefaultPrimary.copy(alpha = 0.1f)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = DefaultPrimary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = DefaultPrimary
            )
        }
    }
}


@Composable
fun HomeScreen(navController: NavHostController) {

    var auth = FirebaseAuth.getInstance()
    var user = auth.currentUser
    var firestore = FirebaseFirestore.getInstance()
    var authRepo = AuthRepository()

    val showBookingDialog = remember { mutableStateOf(false) }
    val selectedSpecialization = remember { mutableStateOf<Specialization?>(null) }

    var userData = remember { mutableStateOf(Result.success(emptyMap<String, Any>())) }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            userData.value = authRepo.getUserData(uid)
        }
    }

    val doctors = remember { mutableStateOf(emptyList<Map<String, Any>>()) }

    LaunchedEffect(Unit) {
        val result = authRepo.getDoctors()
        if (result.isSuccess) {
            val doctorsList = result.getOrNull() ?: emptyList()
            doctors.value = doctorsList
        } else {
            // Handle error
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground)
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header with greeting
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hii, ${userData.value.getOrNull()?.get("name") ?: ""}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = DefaultPrimary
                    )
                }

                // User avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(DefaultPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(48.dp)
                    )
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
                    Row{
                        Text(
                            text = "Daily Health Tip",
                            style = MaterialTheme.typography.titleLarge,
                            color = DefaultPrimary,
                            fontWeight = FontWeight.Bold
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
                specializations = Specialization.entries,
                onSpecializationSelected = { specialization ->
                    selectedSpecialization.value = specialization
                    showBookingDialog.value = true
                }
            )

            AppointmentsSection(
                appointments = listOf(
                    Appointment(
                        appointmentId = "123",
                        doctorId = "7PF99RFwlAc85r1760yyaMnvfo33",
                        patientId = "P001",
                        date = "30th Dec 2023",
                        time = "10:00 AM",
                        status = Status.CONFIRMED,
                        type = AppointmentType.EYE_TEST,
                        price = 50.0,
                        address = "123 Main St, City",
                        doctorName = "Ava Kum",
                        comments = "Don't forget your glasses!"
                    ),
                    Appointment(
                        appointmentId = "124",
                        doctorId = "RE2CoEAtEmXbYdhQ7PotN1rFqMk1",
                        patientId = "P002",
                        date = "31st Dec 2023",
                        time = "11:00 AM",
                        status = Status.PENDING,
                        type = AppointmentType.DENTAL_IMPLANT,
                        price = 30.0,
                        doctorName = "Artur Kot",
                        comments = "Make sure to arrive 15 minutes early. Bring your ID.",
                    )
                )
            )

//            // Quick actions
//            Text(
//                text = "Quick Actions",
//                style = MaterialTheme.typography.titleMedium,
//                color = DefaultPrimary,
//                modifier = Modifier.padding(top = 8.dp)
//            )
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                QuickActionButton(
//                    icon = Icons.Default.Face,
//                    label = "Doctors",
//                    onClick = { navController.navigate(Screen.Doctors.route) }
//                )
//                QuickActionButton(
//                    icon = Icons.Default.Call,
//                    label = "Chat",
//                    onClick = { navController.navigate(Screen.Chat.route) }
//                )
//            }
        }
    }
}
@Preview
@Composable
fun HomeScreenPreview() {
    LadyCureTheme {
        HomeScreen(navController = rememberNavController())
    }
}