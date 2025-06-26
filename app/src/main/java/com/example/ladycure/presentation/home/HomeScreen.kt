package com.example.ladycure.presentation.home

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.example.ladycure.domain.model.Speciality
import com.example.ladycure.presentation.home.components.AppointmentsSection
import com.example.ladycure.presentation.home.components.BookAppointmentSection
import com.example.ladycure.utility.HealthTips.getDailyTip
import com.example.ladycure.utility.HealthTips.getRandomTip
import com.example.ladycure.utility.SnackbarController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    snackbarController: SnackbarController? = null,
    homeViewModel: HomeViewModel = viewModel()
) {
    val uiState by homeViewModel.uiState.collectAsState()

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION

    )

    var showRationaleDialog by rememberSaveable { mutableStateOf(false) }
    var showPermissionExplanationDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(locationPermissionState.status) {
        when {
            locationPermissionState.status.isGranted -> {
                homeViewModel.onPermissionResult(isGranted = true)
            }

            locationPermissionState.status.shouldShowRationale -> {
                showRationaleDialog = true
            }

            !locationPermissionState.status.isGranted -> {
                if (!uiState.locationFetched) {
                    showPermissionExplanationDialog = true
                }
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarController?.showMessage(it)
            homeViewModel.clearError()
        }
    }


    if (uiState.userData == null) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = DefaultPrimary,
                modifier = Modifier.size(48.dp)
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {

            Header(
                userData = uiState.userData ?: emptyMap(),
                unreadCount = uiState.unreadNotificationCount,
                onProfileClick = { navController.navigate("profile") },
                onNotificationClick = { navController.navigate("notifications/user") }
            )

            var dailyTip by remember { mutableStateOf(getDailyTip()) }

            HealthTipCard(
                dailyTip = dailyTip,
                onRefreshClick = { dailyTip = getRandomTip() },
                onTodayClick = { dailyTip = getDailyTip() }
            )

            BookAppointmentSection(
                specialities = Speciality.entries,
                selectedCity = uiState.selectedCity,
                initialCity = uiState.initialCity,
                availableCities = uiState.availableCities,
                onCitySelected = { city ->
                    homeViewModel.onCitySelected(city)
                },
                onSpecializationSelected = { specialization ->
                    val cityToUse = uiState.selectedCity ?: uiState.initialCity
                    if (cityToUse != null) {
                        navController.navigate("services/$cityToUse/${specialization.displayName}")
                    } else if (!uiState.locationFetched) {
                        snackbarController?.showMessage("Please wait for location to be fetched")
                    } else {
                        snackbarController?.showMessage("Please select a city")
                    }
                },
                context = LocalContext.current
            )

            AppointmentsSection(
                appointments = uiState.appointments,
                onAppointmentChanged = { updatedAppointment ->
                    uiState.appointments = uiState.appointments?.map {
                        if (it.appointmentId == updatedAppointment.appointmentId) updatedAppointment else it
                    }
                },
                snackbarController = snackbarController!!,
                navController = navController
            )
        }
    }




    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = {
                showRationaleDialog = false
                homeViewModel.handlePermissionDenied()
            },
            title = {
                Text(
                    text = "Why do we need your location?",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "We need access to your location to find the doctors near you. This helps us provide a better experience by showing relevant services based on your location.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationaleDialog = false
                        locationPermissionState.launchPermissionRequest()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = DefaultPrimary
                    )
                ) {
                    Text("Make Sense!")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRationaleDialog = false
                        homeViewModel.handlePermissionDenied()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = DefaultOnPrimary.copy(alpha = 0.8f)
                    )
                ) {
                    Text("No, Thanks")
                }
            },
            shape = RoundedCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        )
    }

    if (showPermissionExplanationDialog) {
        AlertDialog(
            onDismissRequest = {
                showPermissionExplanationDialog = false
                homeViewModel.handlePermissionDenied()
            },
            title = {
                Text(
                    text = "Permission Needed \uD83D\uDCCD",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "To provide you with the best experience, we need access to your location to find nearby doctors.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionExplanationDialog = false
                        locationPermissionState.launchPermissionRequest()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = DefaultPrimary,
                        containerColor = Color.Transparent
                    )
                ) {
                    Text("Sure!")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPermissionExplanationDialog = false
                        homeViewModel.handlePermissionDenied()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = DefaultOnPrimary.copy(alpha = 0.8f)
                    )
                ) {
                    Text("Not now")
                }
            },
            shape = RoundedCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        )
    }
}

/**
 * Finds the nearest city from a predefined list of cities based on given latitude and longitude.
 * Uses the haversine formula to calculate the distance between two geographical points.
 *
 * @param latitude The latitude of the current location.
 * @param longitude The longitude of the current location.
 * @return The name of the nearest city, or "Warszawa" if no cities are defined or an error occurs.
 */
fun findNearestCity(latitude: Double, longitude: Double): String {
    val cities = mapOf(
        "Warszawa" to Pair(52.2297, 21.0122),
        "Kraków" to Pair(50.0647, 19.9450),
        "Wrocław" to Pair(51.1079, 17.0385),
        "Poznań" to Pair(52.4064, 16.9252),
        "Gdańsk" to Pair(54.3520, 18.6466),
        "Łódź" to Pair(51.7592, 19.4560)
    )

    return cities.minByOrNull { (_, coords) ->
        haversine(latitude, longitude, coords.first, coords.second)
    }?.key ?: "Warszawa"
}

/**
 * Calculates the great-circle distance between two points on a sphere (earth) given their longitudes and latitudes.
 *
 * @param lat1 Latitude of the first point in degrees.
 * @param lon1 Longitude of the first point in degrees.
 * @param lat2 Latitude of the second point in degrees.
 * @param lon2 Longitude of the second point in degrees.
 * @return The distance between the two points in kilometers.
 */
fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0 // earth radius in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a =
        sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(
            dLon / 2
        ).pow(
            2
        )
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}


/**
 * A composable function that displays a daily health tip with options to refresh or view today's tip.
 *
 * @param dailyTip The health tip string to be displayed.
 * @param onRefreshClick Lambda function to be invoked when the refresh button is clicked.
 * @param onTodayClick Lambda function to be invoked when the "Today's Tip" button is clicked.
 */
@Composable
fun HealthTipCard(
    dailyTip: String,
    onRefreshClick: () -> Unit,
    onTodayClick: () -> Unit
) {
    val setToTodays = remember { mutableStateOf(false) }

    setToTodays.value = dailyTip == getDailyTip()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row {
                Text(
                    text = "Daily Health Tip",
                    style = MaterialTheme.typography.titleLarge,
                    color = DefaultOnPrimary,
                    fontWeight = FontWeight.Normal
                )
                IconButton(
                    onClick = {
                        onRefreshClick()
                        setToTodays.value = false
                    },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Regenerate Tip",
                        tint = DefaultPrimary
                    )
                }
                if (!setToTodays.value) {
                    IconButton(
                        onClick = {
                            onTodayClick()
                            setToTodays.value = true
                        },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Todays Tip",
                            tint = DefaultPrimary
                        )
                    }
                }
            }
            Text(
                text = dailyTip,
                style = MaterialTheme.typography.bodyMedium,
                color = DefaultOnPrimary.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * A composable function that displays the header section of the home screen, including user greeting,
 * notification icon with unread count, and user profile picture.
 *
 * @param userData A map containing user data, typically including "name" and "profilePictureUrl".
 * @param unreadCount The number of unread notifications to display on the badge.
 * @param onProfileClick Lambda function to be invoked when the profile picture is clicked.
 * @param onNotificationClick Lambda function to be invoked when the notification icon is clicked.
 */
@Composable
fun Header(
    userData: Map<String, Any>,
    unreadCount: Int,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Hii, ${userData["name"] ?: ""}",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = DefaultPrimary
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    onNotificationClick()
                },
                modifier = Modifier.fillMaxHeight(),
            ) {
                if (unreadCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(containerColor = DefaultPrimary) {
                                Text(
                                    text = unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = DefaultPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

            }

            Spacer(modifier = Modifier.size(16.dp))

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(DefaultPrimary.copy(alpha = 0.2f))
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                val profileUrl = userData.get("profilePictureUrl") as? String
                if (profileUrl != null) {
                    SubcomposeAsyncImage(
                        model = profileUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        loading = {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = DefaultPrimary
                            )
                        },
                        error = {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = DefaultPrimary
                            )
                        }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = DefaultPrimary,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}