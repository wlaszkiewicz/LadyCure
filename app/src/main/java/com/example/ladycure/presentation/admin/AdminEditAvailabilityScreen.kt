package com.example.ladycure.presentation.admin

import DefaultBackground
import DefaultPrimary
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController // Import NavHostController
import com.example.ladycure.presentation.availability.SetAvailabilityScreen
import com.example.ladycure.presentation.availability.SetAvailabilityScreenAdmin
import com.example.ladycure.utility.SnackbarController

@Composable
fun AdminEditAvailabilityScreen(
    navController: NavController,
    snackbarController: SnackbarController,
    doctorId: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultBackground)
    ) {
        // Header with back navigation
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

        }

        // Pass the doctorId to SetAvailabilityScreenAdmin
        SetAvailabilityScreenAdmin(
            navController = navController as NavHostController,
            snackbarController = snackbarController,
            doctorId = doctorId
        )
    }
}