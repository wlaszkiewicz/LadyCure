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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with doctor icon and name
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_doctor),
                    contentDescription = "Doctor Icon",
                    tint = DefaultPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = "dr. ${doctor["name"] as? String ?: "Unknown"} ${doctor["surname"] as? String ?: ""}",
                        style = MaterialTheme.typography.titleLarge,
                        color = DefaultPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = doctor["specification"] as? String ?: "Not specified",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            // Doctor details section
            Column(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Address and rating row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_location_pin),
                        contentDescription = "Location Icon",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = " ${doctor["address"] as? String ?: "Not available"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    val rating = (doctor["rating"] as? Number)?.toFloat() ?: 0f
                    //val reviewCount = doctor["reviewCount"] as? Int ?: 0
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StarRating(
                            rating = rating,
                            modifier = Modifier.padding(end = 4.dp)
                        )
//                        Text(
//                            text = "%.1f ($reviewCount)".format(rating),
//                            style = MaterialTheme.typography.labelMedium,
//                            color = DefaultOnPrimary
//                        )
                    }
                }

                // Price section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Consultation price:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary
                    )
                    Text(
                        text = "${doctor["consultationPrice"] as? String ?: doctor["consultationPrice"] as? Number ?: "Not specified"} zł",
                        style = MaterialTheme.typography.bodyLarge,
                        color = DefaultPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Book appointment button
            Button(
                onClick = { showConfirmationDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultPrimary,
                    //contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Text(
                    text = "Book an appointment",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // Confirmation dialog
        if (showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmationDialog = false },
                title = {
                    Text(
                        text = "Confirm appointment",
                        style = MaterialTheme.typography.headlineSmall,
                        color = DefaultPrimary
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to book an appointment with dr. ${doctor["name"]} ${doctor["surname"]}?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showConfirmationDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DefaultPrimary,
                            //contentColor = Color.White
                        )
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showConfirmationDialog = false }
                    ) {
                        Text("Cancel", color = DefaultPrimary)
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