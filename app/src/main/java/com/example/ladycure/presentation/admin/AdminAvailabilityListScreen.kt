package com.example.ladycure.presentation.admin

import DefaultBackground
import DefaultPrimary
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ladycure.presentation.availability.AvailabilityListScreen
import com.example.ladycure.utility.SnackbarController
import androidx.compose.foundation.background

@Composable
fun AdminAvailabilityListScreen(
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

        // Reuse the existing AvailabilityListScreen but with admin context
        AvailabilityListScreen(
            navController = navController,
            snackbarController = snackbarController,
            isAdminView = true,
            doctorId = doctorId
        )
    }
}