package com.example.ladycure

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import android.graphics.Color
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ladycure.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    var showConfirmationDialog by remember { mutableStateOf(false) }
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
                    text = "dr. ${doctor["name"] as? String ?: "Unknown"} ${doctor["surname"] as? String ?: ""}",
                    style = MaterialTheme.typography.titleMedium,
                    color = DefaultPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Add specialization
            Text(
                text = "Specialization: ${doctor["specification"] as? String ?: "Not specified"}",
                style = MaterialTheme.typography.bodyMedium,
                color = DefaultOnPrimary
            )

            // Add address
            Text(
                text = "Address: ${doctor["address"] as? String ?: "Not available"}",
                style = MaterialTheme.typography.bodyMedium,
                color = DefaultOnPrimary
            )

            // Add consultation price
            Text(
                text = "Consultation price: ${doctor["consultationPrice"] as? String ?: doctor["consultationPrice"] as? Number ?: "Not specified"} zł",
                style = MaterialTheme.typography.bodyMedium,
                color = DefaultOnPrimary,
                fontWeight = FontWeight.Bold
            )

            // Add star ratings
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                val rating = (doctor["rating"] as? Number)?.toFloat() ?: 0f
                val reviewCount = doctor["reviewCount"] as? Int ?: 0

                StarRating(
                    rating = rating,
                    modifier = Modifier.padding(end = 4.dp)
                )

                Text(
                    text = "%.1f ($reviewCount reviews)".format(rating),
                    style = MaterialTheme.typography.bodySmall,
                    color = DefaultOnPrimary.copy(alpha = 0.8f)
                )

                Button(
                    onClick = { showConfirmationDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DefaultPrimary,
                        //contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Book an appointment")
                }
            }
        }

        if (showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmationDialog = false },
                title = {
                    Text(
                        text= "Confirm an appointment",
                        style = MaterialTheme.typography.headlineSmall,
                        color = DefaultPrimary
                    )
                },
                text = {
                    Text("Are you sure you want to book an appointment with ${doctor["name"]} ${doctor["surname"]}?",)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmationDialog = false
                            // Tutaj można dodać logikę umawiania wizyty
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DefaultPrimary,
                            //contentColor = Color.White
                        )
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showConfirmationDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DefaultOnPrimary.copy(alpha = 0.1f),
                            contentColor = DefaultOnPrimary
                        )
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun StarRating(
    rating: Float,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    starSize: Int = 16
) {
    Row(modifier = modifier) {
        for (i in 1..maxStars) {
            val starIcon = when {
                i <= rating -> R.drawable.baseline_star_rate // Filled star
                i - 0.5 <= rating -> R.drawable.baseline_star_half // Half star
                else -> R.drawable.baseline_star_border// Empty star
            }

            Icon(
                painter = painterResource(id = starIcon),
                contentDescription = "Star",
                tint = DefaultPrimary,
                modifier = Modifier.size(starSize.dp)
            )
        }
    }
}