package com.example.ladycure.screens.user

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import com.example.ladycure.data.Appointment
import com.example.ladycure.data.doctor.Speciality
import com.example.ladycure.presentation.home.components.AppointmentsSection
import com.example.ladycure.presentation.home.components.BookAppointmentSection
import com.example.ladycure.repository.AppointmentRepository
import com.example.ladycure.repository.UserRepository
import com.example.ladycure.utility.HealthTips.getDailyTip
import com.example.ladycure.utility.HealthTips.getRandomTip
import com.example.ladycure.utility.SharedPreferencesHelper
import com.example.ladycure.utility.SnackbarController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
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
    context: Context = LocalContext.current
) {
    val userRepo = UserRepository()
    val appointmentRepo = AppointmentRepository()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // State variables
    val selectedSpeciality = remember { mutableStateOf<Speciality?>(null) }
    val userData = remember { mutableStateOf<Map<String, Any>?>(null) }
    val error = remember { mutableStateOf<String?>(null) }
    val appointments = remember { mutableStateOf<List<Appointment>?>(null) }
    var locationFetched = remember { mutableStateOf(false) }

    var selectedCity by remember { mutableStateOf<String?>(null) }
    var initialCity by remember { mutableStateOf<String?>(null) }

    val availableCities = listOf(
        "Warszawa", "Kraków", "Wrocław", "Poznań", "Gdańsk", "Łódź",
        "Szczecin", "Bydgoszcz", "Lublin", "Katowice", "Białystok", "Gdynia", "Częstochowa",
        "Radom", "Sosnowiec", "Toruń", "Kielce", "Rzeszów", "Olsztyn", "Zielona Góra"
    )

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(Unit) {
        try {
            if (SharedPreferencesHelper.shouldRememberChoice(context)) {
                SharedPreferencesHelper.getCity(context)?.let { savedCity ->
                    selectedCity = savedCity
                    locationFetched.value = true
                }
            }

            userRepo.getCurrentUserData().getOrNull()?.let { data ->
                userData.value = data

                appointmentRepo.getAppointments("user").getOrNull()?.let { apps ->
                    appointments.value = apps
                }
            }
        } catch (e: Exception) {
            error.value = e.message
        }
    }

    LaunchedEffect(locationPermissionState.status, locationFetched.value) {
        if (locationFetched.value) return@LaunchedEffect

        when {
            locationPermissionState.status.isGranted -> {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        var lastLocation: Location? = null
                        try {
                            lastLocation = fusedLocationClient.lastLocation.await()
                        } catch (e: Exception) {
                            error.value = "Error fetching location: ${e.message}"
                        }

                        if (lastLocation == null) {
                            try {
                                LocationRequest.create().apply {
                                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                                    numUpdates = 1
                                    interval = 10000
                                    fastestInterval = 5000
                                }

                                lastLocation = fusedLocationClient.getCurrentLocation(
                                    LocationRequest.PRIORITY_HIGH_ACCURACY,
                                    null
                                ).await()
                            } catch (e: Exception) {
                                error.value = "Error getting fresh location: ${e.message}"
                            }
                        }

                        lastLocation?.let { loc ->
                            initialCity = findNearestCity(loc.latitude, loc.longitude)
                            locationFetched.value = true
                        } ?: run {
                            initialCity = availableCities.firstOrNull()
                            locationFetched.value = true
                            snackbarController?.showMessage("Couldn't determine your location. Using default city.")
                        }
                    }
                } catch (e: Exception) {
                    initialCity = availableCities.firstOrNull()
                    locationFetched.value = true
                    snackbarController?.showMessage("Error getting location: $e. Using default city.")
                }
            }

            locationPermissionState.status.shouldShowRationale -> {
                snackbarController?.showMessage("We need access to location to find the closest doctors")
                initialCity = availableCities.firstOrNull()
                locationFetched.value = true
            }

            !locationPermissionState.status.isGranted -> {
                locationPermissionState.launchPermissionRequest()
                initialCity = availableCities.firstOrNull()
                locationFetched.value = true
            }
        }
    }

    // Show error if any
    LaunchedEffect(error.value) {
        error.value?.let { err ->
            snackbarController?.showMessage(err)
            error.value = null
        }
    }

    if (userData.value == null) {
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
        if (error.value != null) {
            snackbarController?.showMessage(
                message = error.value ?: "An error occurred"
            )
            error.value = null
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {

            Header(
                userData = userData.value ?: emptyMap(),
                onProfileClick = { navController.navigate("profile") },
                onNotificationClick = { /* TODO: Handle notification click */ }
            )

            // Health tips card
            var dailyTip by remember { mutableStateOf(getDailyTip()) }

            HealthTipCard(
                dailyTip = dailyTip,
                onRefreshClick = {
                    dailyTip = getRandomTip()
                },
                onTodayClick = {
                    dailyTip = getDailyTip()
                },
            )

            BookAppointmentSection(
                specialities = Speciality.entries,
                selectedCity = selectedCity,
                initialCity = initialCity,
                availableCities = availableCities,
                onCitySelected = { city ->
                    selectedCity = city
                },
                onSpecializationSelected = { specialization ->
                    selectedSpeciality.value = specialization
                    selectedCity?.let { city ->
                        navController.navigate("services/$city/${specialization.displayName}")
                    } ?: run {
                        if (!locationFetched.value) {
                            snackbarController?.showMessage("Please wait for location to be fetched")
                        } else if (initialCity != null) {
                            navController.navigate("services/$initialCity/${specialization.displayName}")
                        } else {
                            snackbarController?.showMessage("Please select a city")
                        }
                    }
                },
                context = context
            )

            AppointmentsSection(
                appointments = appointments.value,
                onAppointmentChanged = { updatedAppointment ->
                    appointments.value = appointments.value?.map {
                        if (it.appointmentId == updatedAppointment.appointmentId) updatedAppointment else it
                    }
                },
                snackbarController = snackbarController!!,
                navController = navController
            )
        }
    }
}

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


@Composable
fun HealthTipCard(
    dailyTip: String,
    onRefreshClick: () -> Unit,
    onTodayClick: () -> Unit
) {
    var setToTodays = remember { mutableStateOf(false) }

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

@Composable
fun Header(
    userData: Map<String, Any>,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
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
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = DefaultPrimary,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.size(8.dp))

            // User avatar
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

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        navController = rememberNavController(),
        snackbarController = null,
        context = LocalContext.current
    )
}