package com.example.ladycure

import DefaultOnPrimary
import DefaultPrimary
import DefaultBackground
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import com.example.ladycure.data.doctor.Specialization
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material3.Text
import com.example.ladycure.repository.AuthRepository


@Composable
fun BookAppointmentScreen(navController: NavController, city: String, selectedSpecialization: Specialization) {

    val authRepo = AuthRepository()
    val allDoctors = remember { mutableStateOf(emptyList<Map<String, Any>>()) }

    LaunchedEffect(Unit) {
        val result = authRepo.getDoctors()
        if (result.isSuccess) {
            val doctorsList = result.getOrNull() ?: emptyList()
            allDoctors.value = doctorsList
        } else {
            // Handle error
        }
    }

    val doctors = remember { mutableStateOf(emptyList<Map<String, Any>>()) }

    LaunchedEffect(allDoctors.value) {
        doctors.value = allDoctors.value.filter { doctor ->
            val specialization = doctor["specialization"] as? String
            specialization == selectedSpecialization.displayName
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultBackground)
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically, // This centers the contents vertically within the Row
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back",
                    tint = DefaultOnPrimary,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Book Appointment in $city",
                color = DefaultOnPrimary
            )
        }

    }
}

    // Your booking screen content
    // For example, you can add a Text or any other UI elements
    // Text(text = "Book Appointment Screen")



@Preview
@Composable
fun BookAppointmentScreenPreview() {
    BookAppointmentScreen(navController = rememberNavController(), "Wrocław", selectedSpecialization = Specialization.CARDIOLOGY)
}