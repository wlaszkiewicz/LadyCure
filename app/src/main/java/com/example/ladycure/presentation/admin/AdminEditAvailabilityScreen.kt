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

/**
 * A screen composable for admins to edit a doctor's availability schedule.
 *
 * This screen includes a top app bar with a back button and a title,
 * and embeds the [SetAvailabilityScreenAdmin] composable that handles
 * the actual availability management UI and logic.
 *
 * @param navController The navigation controller used to handle navigation events.
 * @param snackbarController Controller to show snackbar messages for feedback.
 * @param doctorId The unique identifier of the doctor whose availability is being edited.
 */
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

        }

        SetAvailabilityScreenAdmin(
            navController = navController as NavHostController,
            snackbarController = snackbarController,
            doctorId = doctorId
        )
    }
}