package com.example.ladycure.presentation.home.components

import DefaultOnPrimary
import DefaultPrimary
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ladycure.data.doctor.Speciality
import com.example.ladycure.utility.SharedPreferencesHelper

@Composable
fun BookAppointmentSection(
    specialities: List<Speciality>,
    selectedCity: String?,
    initialCity: String?,
    availableCities: List<String>,
    onCitySelected: (String) -> Unit = {},
    onSpecializationSelected: (Speciality) -> Unit,
    context: Context = LocalContext.current
) {
    val lastFetchedCity = remember(initialCity) { mutableStateOf<String?>(initialCity) }

    var showLocationDropdown by remember { mutableStateOf(false) }
    var selectedLocation = remember(selectedCity, initialCity) {
        selectedCity ?: initialCity ?: "Detecting your location..."
    }
    var rememberChoice by remember {
        mutableStateOf(
            SharedPreferencesHelper.shouldRememberChoice(context)
        )
    }

    val showRememberChoice = remember(selectedCity, initialCity) {
        selectedCity != null &&
                selectedCity != "Detecting your location..." &&
                selectedCity != initialCity
    }

    LaunchedEffect(selectedCity, initialCity) {
        selectedCity?.let {
            selectedLocation = it
        } ?: run {
            selectedLocation = if (lastFetchedCity.value != null) {
                lastFetchedCity.value!!
            } else {
                "Detecting your location..."
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
                Column(
                    modifier = Modifier.background(Color.White.copy(alpha = 0.5f))
                ) {
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
                                })
                        })

                    AnimatedVisibility(
                        visible = showRememberChoice,
                        enter = slideInVertically(
                            initialOffsetY = { -it }, // Slides down from above
                            animationSpec = tween(durationMillis = 300)
                        ),
                        exit = slideOutVertically(
                            targetOffsetY = { -it }, // Slides up to hide
                            animationSpec = tween(durationMillis = 300)
                        )
                    ) {
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
                                    } else {
                                        SharedPreferencesHelper.saveCity(
                                            context,
                                            ""
                                        ) // Clear saved city
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
                            // Reset checkbox when selecting new city
                            rememberChoice = false
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

@Composable
fun SpecialityCard(
    speciality: Speciality,
    onSpecialitySelected: (Speciality) -> Unit
) {

    val specializationColors = listOf(
        Color(0xFFFFF0F5),
        Color(0xFFF0F8FF),
        Color(0xFFFAFAD2),
        Color(0xFFE9FFEB),
        Color(0xFFE2DCFA)
    )

    val cardColor = specializationColors[speciality.ordinal % specializationColors.size]
    Surface(
        modifier = Modifier.shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
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
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize(),
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
