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

/**
 * Composable screen displaying the availability list for a specific doctor from an admin perspective.
 *
 * This screen wraps the [AvailabilityListScreen] composable, enabling admin-specific functionality.
 * It also sets up the basic layout with background and padding.
 *
 * @param navController Navigation controller used for handling navigation actions.
 * @param snackbarController Controller for showing snackbars to the user.
 * @param doctorId The unique identifier of the doctor whose availability is being managed/viewed.
 */
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

        }

        AvailabilityListScreen(
            navController = navController,
            snackbarController = snackbarController,
            isAdminView = true,
            doctorId = doctorId
        )
    }
}