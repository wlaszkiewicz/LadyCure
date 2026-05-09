package com.example.ladycure.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.ladycure.presentation.availability.SetAvailabilityScreenAdmin
import com.example.ladycure.ui.theme.DefaultBackground
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import com.example.ladycure.utility.SnackbarController

@Composable
fun AdminEditAvailabilityScreen(
    navController: NavController,
    snackbarController: SnackbarController,
    doctorId: String
) {
    val dimens = rememberResponsiveDimens()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultBackground)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.w(0.039f), vertical = dimens.h(0.017f))
        ) {

        }

        SetAvailabilityScreenAdmin(
            navController = navController as NavHostController,
            snackbarController = snackbarController,
            doctorId = doctorId
        )
    }
}
