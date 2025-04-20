package com.example.ladycure

import DefaultOnPrimary
import DefaultPrimary
import DefaultBackground
import androidx.compose.material3.AlertDialog
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material3.Text
import coil.compose.AsyncImage
import com.example.ladycure.repository.AuthRepository

@Composable
fun BookAppointmentScreen(navController: NavController, city: String, selectedSpecialization: Specialization) {
    val authRepo = AuthRepository()
    val allDoctors = remember { mutableStateOf(emptyList<Map<String, Any>>()) }
    val isLoading = remember { mutableStateOf(true) }
    val selectedDate = remember { mutableStateOf("Today") }
    val selectedTimeSlot = remember { mutableStateOf<String?>(null) }
    val showDoctorsForSlot = remember { mutableStateOf(false) }
    var showAlertDialog by remember { mutableStateOf(true) }

    // Sample available dates
    val availableDates = listOf("Today", "Tomorrow", "Apr 25", "Apr 26")

    // Sample time slots
    val allTimeSlots = listOf(
        "9:00 AM", "10:30 AM", "12:00 PM",
        "2:00 PM", "3:30 PM", "5:00 PM"
    )


    LaunchedEffect(Unit) {
        val result = authRepo.getDoctors()
        if (result.isSuccess) {
            val doctorsList = result.getOrNull() ?: emptyList()
            allDoctors.value = doctorsList
        }
        isLoading.value = false
    }

    val filteredDoctors = remember { mutableStateOf(emptyList<Map<String, Any>>()) }

    LaunchedEffect(allDoctors.value, selectedTimeSlot.value) {
        filteredDoctors.value = allDoctors.value.filter { doctor ->
            val specialization = doctor["specification"] as? String
            specialization == selectedSpecialization.displayName
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = {
                    if (showDoctorsForSlot.value) {
                        showDoctorsForSlot.value = false
                    } else {
                        navController.popBackStack()
                    }
                },
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
                text = if (showDoctorsForSlot.value) "Select Doctor" else "Book Appointment",
                style = MaterialTheme.typography.titleLarge,
                color = DefaultOnPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(48.dp)) // Balance the back button space
        }

        if (!showDoctorsForSlot.value) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Location and specialty
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "📍 $city",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Text(
                        text = selectedSpecialization.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = DefaultPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(DefaultPrimary.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                // Date selection
                Text(
                    text = "Select Date",
                    style = MaterialTheme.typography.titleMedium,
                    color = DefaultOnPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    availableDates.forEach { date ->
                        DateChip(
                            date = date,
                            isSelected = date == selectedDate.value,
                            onSelect = { selectedDate.value = date }
                        )
                    }
                }

                // Time slots
                Text(
                    text = "Available Time Slots",
                    style = MaterialTheme.typography.titleMedium,
                    color = DefaultOnPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(allTimeSlots.chunked(2)) { rowSlots ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            rowSlots.forEach { slot ->
                                TimeSlotChip(
                                    time = slot,
                                    isSelected = slot == selectedTimeSlot.value,
                                    onSelect = {
                                        selectedTimeSlot.value = slot
                                        showDoctorsForSlot.value = true
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Selected time info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = DefaultPrimary.copy(alpha = 0.2f),
                        contentColor = DefaultOnPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = selectedDate.value,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DefaultOnPrimary
                            )
                            Text(
                                text = selectedTimeSlot.value ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DefaultOnPrimary
                            )
                        }
                        Button(
                            onClick = { showDoctorsForSlot.value = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = DefaultPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp
                            )
                        ) {
                            Text("Change", color = DefaultOnPrimary)
                        }
                    }
                }

                if (isLoading.value) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Loading doctors...", color = DefaultOnPrimary)
                    }
                } else if (filteredDoctors.value.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No doctors available",
                            style = MaterialTheme.typography.titleMedium,
                            color = DefaultOnPrimary.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                        Text(
                            text = "We couldn't find any ${selectedSpecialization.displayName.lowercase()} specialists for ${selectedTimeSlot.value}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DefaultOnPrimary.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                } else {
                    // Doctors list
                    Text(
                        text = "Available Doctors",
                        style = MaterialTheme.typography.titleMedium,
                        color = DefaultOnPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        items(filteredDoctors.value) { doctor ->
                            DoctorCard(
                                doctor = doctor,
                                onBookClick = {

                                    // Handle final booking//navController.navigate("confirmation/${doctor["id"]}")
                                },
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAlertDialog) {
        Alert2137Dialog(
            onDismiss = { showAlertDialog = false }  // Correct implementation
        )
    }
}

@Composable
fun Alert2137Dialog(onDismiss: () -> Unit) {
    var message by remember { mutableStateOf("HI, the code you're about to see does not work, and is the first version of something that will be pretty (one day).")}
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Alert 2137", color = Color.Red) },
        text = { Text(message, color = DefaultOnPrimary) },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("oki")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    message = "I am not oki"
                }
            ) {
                Text("nie oki ? ")
            }
        }
    )
}
@Composable
fun DateChip(date: String, isSelected: Boolean, onSelect: () -> Unit) {
    Button(
        onClick = onSelect,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) DefaultPrimary else DefaultOnPrimary.copy(alpha = 0.3f),
            contentColor = if (isSelected) Color.White else DefaultPrimary
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        ),
        modifier = Modifier
            .height(48.dp)
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun TimeSlotChip(time: String, isSelected: Boolean, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onSelect,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) DefaultPrimary else DefaultOnPrimary.copy(alpha = 0.3f),
            contentColor = if (isSelected) Color.White else DefaultPrimary
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        ),
        modifier = modifier.height(60.dp)
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun DoctorCard(doctor: Map<String, Any>, onBookClick: () -> Unit, modifier: Modifier = Modifier) {
    val name = doctor["surname"] as? String ?: "Unknown"
    val specialization = doctor["specification"] as? String ?: "Specialist"
    val rating = doctor["rating"] as? Double ?: 4.5
    val experience = doctor["experience"] as? Int ?: 5
    val imageUrl = doctor["profilePictureUrl"] as? String ?: ""

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Doctor image
            if (imageUrl.isEmpty()) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Doctor Image",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    tint = Color.Gray
                )
            } else{
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Doctor $name",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Doctor info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Dr. $name",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = DefaultOnPrimary
                )
                Text(
                    text = specialization,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⭐ $rating",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFFA000)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$experience yrs exp",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
            }

            // Book button
            Button(
                onClick = onBookClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.width(120.dp)
            ) {
                Text(
                    text = "Select",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Preview
@Composable
fun BookAppointmentScreenPreview() {
    BookAppointmentScreen(
        navController = rememberNavController(),
        city = "Wrocław",
        selectedSpecialization = Specialization.CARDIOLOGY
    )
}

@Preview
@Composable
fun DateChipPreview() {
    DateChip(
        date = "Today",
        isSelected = true,
        onSelect = {}
    )
}

@Preview
@Composable
fun TimeSlotChipPreview() {
    TimeSlotChip(
        time = "10:30 AM",
        isSelected = false,
        onSelect = {}
    )
}