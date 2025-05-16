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
import androidx.compose.runtime.MutableState
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
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.utility.HealthTips.getDailyTip
import com.example.ladycure.utility.HealthTips.getRandomTip
import com.example.ladycure.utility.SharedPreferencesHelper
import com.example.ladycure.utility.SnackbarController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.time.LocalDate
import java.time.LocalTime
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
    val authRepo = remember { AuthRepository() }
    val fusedLocationClient: FusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val selectedSpeciality = remember { mutableStateOf<Speciality?>(null) }
    val userData = remember { mutableStateOf<Map<String, Any>?>(null) }
    val selectedCity = remember { mutableStateOf<String?>(null) }
    var error = remember { mutableStateOf<String?>(null) }
    val appointments = remember { mutableStateOf<List<Appointment>?>(null) }
    val locationPermissionGranted = remember { mutableStateOf(false) }
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    var locationFetched by remember { mutableStateOf(false) }

    val availableCities = listOf(
        "Warszawa", "Kraków", "Wrocław", "Poznań", "Gdańsk", "Łódź",
        "Szczecin", "Bydgoszcz", "Lublin", "Katowice", "Białystok", "Gdynia", "Częstochowa",
        "Radom", "Sosnowiec", "Toruń", "Kielce", "Rzeszów", "Olsztyn", "Zielona Góra"
    )

    fun findNearestCity(latitude: Double, longitude: Double): String {
        val cities = mapOf(
            "Warszawa" to Pair(52.2297, 21.0122),
            "Kraków" to Pair(50.0647, 19.9450),
            "Wrocław" to Pair(51.1079, 17.0385),
            "Poznań" to Pair(52.4064, 16.9252),
            "Gdańsk" to Pair(54.3520, 18.6466),
            "Łódź" to Pair(51.7592, 19.4560)
        )

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

        return cities.minByOrNull { (_, coords) ->
            haversine(latitude, longitude, coords.first, coords.second)
        }?.key ?: "Warszawa"
    }
    
    fun fetchLocation(
        fusedLocationClient: FusedLocationProviderClient,
        selectedCity: MutableState<String?>,
        availableCities: List<String>
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val city = findNearestCity(it.latitude, it.longitude)
                        selectedCity.value = city
                    } ?: run {
                        selectedCity.value = availableCities.firstOrNull()
                    }
                }
                .addOnFailureListener {
                    selectedCity.value = availableCities.firstOrNull()
                }
        } else {
            selectedCity.value = availableCities.firstOrNull()
        }
    }


    LaunchedEffect(Unit) {
        if (SharedPreferencesHelper.shouldRememberChoice(context)) {
            SharedPreferencesHelper.getCity(context)?.let { savedCity ->
                selectedCity.value = savedCity
            }
        }
    }

    LaunchedEffect(locationPermissionState.status) {
        when {
            locationPermissionState.status.isGranted && !locationFetched -> {
                fetchLocation(fusedLocationClient, selectedCity, availableCities)
                locationFetched = true
            }

            locationPermissionState.status.shouldShowRationale -> {
                snackbarController?.showMessage("We need access to location to find the closest doctors")
            }

            !locationPermissionState.status.isGranted && !locationPermissionState.status.shouldShowRationale -> {
                locationPermissionState.launchPermissionRequest()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    //asking for localizatioon
    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted.value = true
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val city = findNearestCity(it.latitude, it.longitude)
                        selectedCity.value = city
                    } ?: run {
                        selectedCity.value = availableCities.firstOrNull()
                    }
                }
        } else {
            // if we dont have a permission we set the default city- Warszawa
            selectedCity.value = availableCities.firstOrNull()
        }

        val result = authRepo.getCurrentUserData()
        if (result.isSuccess) {
            userData.value = result.getOrNull()
        } else {
            error.value = result.exceptionOrNull()?.message
        }
        error.value = null
    }

    LaunchedEffect(userData.value) {
        val result = authRepo.getAppointments("user")
        if (result.isFailure) {
            error.value = result.exceptionOrNull()?.message
        } else {
            appointments.value = result.getOrNull() ?: emptyList()

            appointments.value = appointments.value!!.filter {
                it.date.isAfter(LocalDate.now()) || (it.date == LocalDate.now() && it.time >= LocalTime.now())
            }

            appointments.value = appointments.value!!.sortedWith(
                compareBy({ it.date }, { it.time })
            )
        }
        error.value = null
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
                selectedCity = selectedCity.value,
                availableCities = availableCities,
                onCitySelected = { city ->
                    selectedCity.value = city
                },
                onSpecializationSelected = { specialization ->
                    selectedSpeciality.value = specialization
                    selectedCity.value?.let { city ->
                        navController.navigate("services/$city/${specialization.displayName}")
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