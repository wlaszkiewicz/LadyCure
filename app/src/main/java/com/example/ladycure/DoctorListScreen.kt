package com.example.ladycure

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ladycure.repository.AuthRepository

@Composable
fun DoctorsListScreen(navController: NavHostController, specification: String) {
    val repository = AuthRepository()
    val doctors = remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    LaunchedEffect(specification) {
        val result = repository.getDoctorsBySpecification(specification)
        if (result.isSuccess) {
            doctors.value = result.getOrDefault(emptyList())
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Doctors in $specification",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DefaultPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            items(doctors.value) { doctor ->
                DoctorCard(doctor)
            }
        }
    }
}

@Composable
fun DoctorCard(doctor: Map<String, Any>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DefaultPrimary.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_doctor),
                    contentDescription = "Doctor Icon",
                    tint = DefaultPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "${doctor["name"] as? String ?: "Unknown"} ${doctor["surname"] as? String ?: ""}",
                    style = MaterialTheme.typography.titleMedium,
                    color = DefaultPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}