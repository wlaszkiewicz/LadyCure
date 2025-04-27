package com.example.ladycure.presentation.home.components

import DefaultPrimary
import DefaultOnPrimary
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
import androidx.navigation.NavHostController
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.ladycure.data.doctor.Speciality
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow


@Composable
fun BookAppointmentSection(
    navController: NavHostController,
    specialities: List<Speciality>,
    onCitySelected: (String) -> Unit = {},
    onSpecializationSelected: (Speciality) -> Unit
) {
    var showLocationDropdown by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf("Detecting your location...") }

    val availableLocations = listOf(
        "Wrocław", "Warszawa", "Kraków", "Łódź", "Poznań",
        "Gdańsk", "Szczecin", "Bydgoszcz", "Lublin", "Katowice",
        "Białystok", "Gdynia", "Częstochowa", "Radom", "Sosnowiec",
        "Toruń", "Kielce", "Rzeszów", "Olsztyn", "Zielona Góra"
    )

    LaunchedEffect(Unit) {
        detectNearestPolishCity { nearestCity ->
            selectedLocation = nearestCity ?: "Detecting your location..."
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
                availableLocations.forEach { location ->
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
                            onCitySelected(selectedLocation)
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

fun detectNearestPolishCity(onCityDetected: (String?) -> Unit) {
    // will detect the nearest city and return it

    return onCityDetected("Wrocław")
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
        navController = rememberNavController(),
        specialities = listOf(
            Speciality.CARDIOLOGY,
            Speciality.DERMATOLOGY,
            Speciality.GYNECOLOGY,
            Speciality.PEDIATRICS,
            Speciality.PSYCHIATRY,
        ),
        onSpecializationSelected = {}
    )
}