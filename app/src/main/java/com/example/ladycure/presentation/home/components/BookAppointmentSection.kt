package com.example.ladycure.presentation.home.components

import DefaultPrimary
import DefaultOnPrimary
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.ladycure.data.doctor.Speciality
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.example.ladycure.utility.SharedPreferencesHelper
import com.google.android.gms.location.LocationServices


@Composable
fun BookAppointmentSection(
    specialities: List<Speciality>,
    selectedCity: String?,
    availableCities: List<String>,
    onCitySelected: (String) -> Unit = {},
    onSpecializationSelected: (Speciality) -> Unit,
    context: Context = LocalContext.current
) {
    var showLocationDropdown by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf(selectedCity ?: "Detecting your location...") }

    var rememberChoice by remember { mutableStateOf(SharedPreferencesHelper.shouldRememberChoice(context)) }

    var lastCityChangeSource by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(rememberChoice) {
        if (rememberChoice) {
            SharedPreferencesHelper.getCity(context)?.let { savedCity ->
                selectedLocation = savedCity
                onCitySelected(savedCity)
                lastCityChangeSource = "remember"
            }
        }
    }

    LaunchedEffect(selectedCity) {
        if (selectedCity != null && lastCityChangeSource != "remember") {
            selectedLocation = selectedCity
            rememberChoice = false // resetowanie checkboxa
            lastCityChangeSource = "external"
        } else if (!rememberChoice && selectedCity == null) {
            detectNearestPolishCity(context) { nearestCity ->
                nearestCity?.let {
                    selectedLocation = it
                    onCitySelected(it)
                    rememberChoice = false //resetowanie checkboxa
                    lastCityChangeSource = "detection"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Book Appointment",
            style = MaterialTheme.typography.titleLarge,
            color = DefaultPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    OutlinedTextField(
                        value = selectedLocation,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.5f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
                            focusedIndicatorColor = DefaultOnPrimary.copy(alpha = 0.1f),
                            unfocusedIndicatorColor = DefaultOnPrimary.copy(alpha = 0.1f),
                            focusedLeadingIconColor = DefaultPrimary,
                            unfocusedLeadingIconColor = DefaultPrimary,
                            focusedTextColor = DefaultPrimary,
                            unfocusedTextColor = DefaultPrimary
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = DefaultPrimary
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = DefaultPrimary,
                                modifier = Modifier.clickable {
                                    showLocationDropdown = !showLocationDropdown
                                }
                            )
                        }
                    )

                    // Checkbox "Remember my choice"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, bottom = 8.dp, top = 4.dp)
                            .height(30.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberChoice,
                            onCheckedChange = { isChecked ->
                                rememberChoice = isChecked
                                SharedPreferencesHelper.saveRememberChoice(context, isChecked)
                                if (isChecked && selectedLocation.isNotEmpty() && selectedLocation != "Detecting your location...") {
                                    SharedPreferencesHelper.saveCity(context, selectedLocation)
                                    lastCityChangeSource = "checkbox"
                                } else {
                                    SharedPreferencesHelper.saveCity(context, "") // czyszceznie zapisanego miasta
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = DefaultPrimary,
                                uncheckedColor = DefaultPrimary.copy(alpha = 0.6f)
                            )
                        )
                        Text(
                            text = "Remember my choice",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DefaultPrimary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            DropdownMenu(
                expanded = showLocationDropdown,
                onDismissRequest = { showLocationDropdown = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(max = 300.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                availableCities.forEach { location ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = location,
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            selectedLocation = location
                            showLocationDropdown = false
                            onCitySelected(location)
                            rememberChoice = false // Resetuj checkbox przy zmianie miasta
                            lastCityChangeSource = "dropdown"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp),
                        colors = MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.onSurface,
                        )
                    )
                }
            }
        }

        // Horizontal scrollable list of specializations
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            specialities.forEach { specialization ->
                SpecialityCard(specialization, onSpecializationSelected)
            }
        }
    }
}



//val specializationColors = listOf(
//    Color(0xFFFFF0F5), // Lavender Blush (very light pink)
//    Color(0xFFF0F8FF),
//    Color.White
//)

fun detectNearestPolishCity(
    context: Context,
    onCityDetected: (String?) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        onCityDetected(null)
        return
    }

    fusedLocationClient.lastLocation
        .addOnSuccessListener { location: Location? ->
            location?.let {
                onCityDetected("Warszawa")
            } ?: run {
                onCityDetected(null)
            }
        }
        .addOnFailureListener {
            onCityDetected(null)
        }
}


@Composable
fun SpecialityCard(
    speciality: Speciality,
    onSpecialitySelected: (Speciality) -> Unit
) {

    val specializationColors = listOf(Color(0xFFFFF0F5),
        Color(0xFFF0F8FF),
        Color(0xFFFAFAD2),
        Color(0xFFE9FFEB),
        Color(0xFFE2DCFA)
    )

    val cardColor = specializationColors[speciality.ordinal % specializationColors.size]
    Surface(modifier = Modifier.shadow(elevation = 2.dp, shape =RoundedCornerShape(20.dp)) // Apply shadow here
    ) {
        Card(
            modifier = Modifier
                .width(150.dp)
                .height(140.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardColor.copy(alpha = 0.9f) // Slightly transparent
            ),
            onClick = { onSpecialitySelected(speciality) }
        ) {
            Column(
                modifier = Modifier.padding(12.dp).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(speciality.icon),
                    contentDescription = speciality.displayName,
                    modifier = Modifier.size(32.dp),
                    tint = DefaultPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = speciality.displayName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
                    color = DefaultOnPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewBookAppointmentSection() {
    BookAppointmentSection(
        specialities = listOf(
            Speciality.CARDIOLOGY,
            Speciality.DERMATOLOGY,
            Speciality.GYNECOLOGY,
            Speciality.PEDIATRICS,
            Speciality.PSYCHIATRY,
        ),
        selectedCity = null,
        availableCities = emptyList(),
        onSpecializationSelected = {}
    )
}