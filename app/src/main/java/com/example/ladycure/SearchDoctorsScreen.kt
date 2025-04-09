package com.example.ladycure

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController


@Composable
fun SearchDoctorsScreen(navController: NavHostController) {
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
                    text = "Find Doctors",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DefaultPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DefaultBackground,
                        unfocusedContainerColor = DefaultBackground,
                        focusedIndicatorColor = DefaultPrimary,
                        unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f)
                    ),
                    placeholder = {
                        Text("Search for specialists...", color = DefaultOnPrimary.copy(alpha = 0.5f))
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_home),
                            contentDescription = "Search",
                            tint = DefaultPrimary
                        )
                    }
                )
            }

            item {
                Text(
                    text = "Specialties",
                    style = MaterialTheme.typography.titleMedium,
                    color = DefaultPrimary
                )
            }

            items(
                listOf(
                    "Gynecology",
                    "Cardiology",
                    "Dermatology",
                    "Pediatrics",
                    "Neurology",
                    "Orthopedics",
                    "Psychiatry",
                    "Ophthalmology",
                    "Oncology",
                    "Endocrinology"
                )
            ) { specification ->
                DoctorSpecification(name = specification) {
                    navController.navigate("doctors/$specification")
                }
            }
        }
    }
}

@Composable
fun DoctorSpecification(name: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(120.dp, 80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DefaultPrimary.copy(alpha = 0.1f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                color = DefaultPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}