package com.example.ladycure.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ladycure.data.repository.DoctorRepository
import com.example.ladycure.domain.model.Doctor
import com.example.ladycure.domain.model.Speciality
import com.example.ladycure.presentation.booking.DoctorCard
import com.example.ladycure.ui.theme.AliceBlue
import com.example.ladycure.ui.theme.DefaultBackground
import com.example.ladycure.ui.theme.DefaultOnPrimary
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.Honeydew
import com.example.ladycure.ui.theme.Lavender
import com.example.ladycure.ui.theme.LavenderBlush
import com.example.ladycure.ui.theme.LightGoldenrod
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import com.example.ladycure.utility.SnackbarController

@Composable
fun SearchDoctorsScreen(navController: NavHostController, snackbarController: SnackbarController) {
    val dimens = rememberResponsiveDimens()
    val searchQuery = remember { mutableStateOf("") }
    val doctorRepo = DoctorRepository()
    var allDoctors by remember { mutableStateOf(emptyList<Doctor>()) }
    var error by remember { mutableStateOf("") }
    var filteredDoctors by remember { mutableStateOf(emptyList<Doctor>()) }
    LaunchedEffect(Unit) {
        val result = doctorRepo.getDoctors()
        if (result.isSuccess) {
            allDoctors = result.getOrNull()!!
        } else {
            error = result.exceptionOrNull()?.message ?: "Unknown error"
        }
    }

    LaunchedEffect(error) {
        if (error.isNotEmpty()) {
            snackbarController.showMessage(
                message = error,
            )
        }
    }

    LaunchedEffect(searchQuery.value) {
        val queryWords = searchQuery.value.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        filteredDoctors = if (queryWords.isNotEmpty()) {
            allDoctors.filter { doctor ->
                queryWords.all { word ->
                    doctor.name.contains(word, ignoreCase = true) ||
                            doctor.surname.contains(word, ignoreCase = true) ||
                            doctor.speciality.displayName.contains(word, ignoreCase = true)
                }
            }
        } else {
            allDoctors
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(dimens.h(16 / 914f))
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DefaultBackground,
        ) {
            Column(
                modifier = Modifier.padding(dimens.w(16 / 411f)),
            ) {
                Text(
                    text = "Doctors",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DefaultPrimary
                    )
                )
                Text(
                    text = "Find the best doctors for you",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DefaultOnPrimary.copy(alpha = 0.8f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground),
            verticalArrangement = Arrangement.spacedBy(dimens.h(16 / 914f))
        ) {
            SearchBar(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                modifier = Modifier.padding(horizontal = dimens.w(16 / 411f))
            )
            if (searchQuery.value.isNotEmpty()) {
                if (filteredDoctors.isNotEmpty()) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = dimens.w(16 / 411f))
                    ) {
                        items(filteredDoctors) { doctor ->
                            DoctorCard(
                                doctor = doctor,
                                onSelect = {
                                    navController.navigate("services/${doctor.id}")
                                },
                                modifier = Modifier.padding(bottom = dimens.h(16 / 914f))
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No doctors found in this category",
                        style = MaterialTheme.typography.bodyLarge,
                        color = DefaultOnPrimary.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = dimens.w(16 / 411f))
                    )
                }
            } else {

                PopularCategories(navController)

                Text(
                    text = "All Specialities",
                    style = MaterialTheme.typography.titleLarge,
                    color = DefaultPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = dimens.w(16 / 411f))
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    items(Speciality.entries.chunked(2)) { rowItems ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = dimens.w(16 / 411f))
                        ) {
                            rowItems.forEach { spec ->
                                DoctorSpecialityCard(
                                    speciality = spec,
                                    navController = navController,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                        }
                    }
                }

            }
        }
    }
}

@Composable
private fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = rememberResponsiveDimens()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(dimens.h(56 / 914f)),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = DefaultBackground,
            unfocusedContainerColor = DefaultBackground,
            focusedIndicatorColor = DefaultPrimary,
            unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.3f),
            focusedTextColor = DefaultOnPrimary,
            unfocusedTextColor = DefaultOnPrimary
        ),
        placeholder = {
            Text(
                "Search for doctors or specialties...",
                color = DefaultOnPrimary.copy(alpha = 0.5f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = DefaultOnPrimary,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = DefaultOnPrimary.copy(alpha = 0.5f)
                    )
                }
            }
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun PopularCategories(navController: NavHostController) {
    val dimens = rememberResponsiveDimens()

    Column(
        modifier = Modifier
    ) {
        Text(
            text = "Popular Categories",
            style = MaterialTheme.typography.titleLarge,
            color = DefaultPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = dimens.h(12 / 914f), start = dimens.w(16 / 411f))
        )


        val specializationColors = listOf(
            LavenderBlush,
            AliceBlue,
            LightGoldenrod,
            Honeydew,
            Lavender
        )

        val categories = Speciality.popularCategories

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.w(16 / 411f))
        ) {
            items(categories) { category ->
                val cardColor =
                    specializationColors[categories.indexOf(category) % specializationColors.size]

                PopularCategoryCard(
                    cardColor = cardColor,
                    category = category,
                    onClick = {
                        navController.navigate("doctors/${category.displayName}")
                    }
                )
            }
        }
    }
}

@Composable
private fun PopularCategoryCard(
    cardColor: Color,
    category: Speciality,
    onClick: () -> Unit
) {
    val dimens = rememberResponsiveDimens()
    Surface(
        modifier = Modifier.shadow(
            elevation = 2.dp,
            shape = RoundedCornerShape(20.dp)
        ) // Apply shadow here
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .width(dimens.w(150 / 411f))
                .height(dimens.h(100 / 914f)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardColor.copy(alpha = 0.9f)
            ),
            border = BorderStroke(1.dp, DefaultPrimary.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = category.icon),
                        contentDescription = category.displayName,
                        tint = DefaultPrimary,
                        modifier = Modifier.size(dimens.w(28 / 411f))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.labelLarge,
                        color = DefaultOnPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun DoctorSpecialityCard(
    speciality: Speciality,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val dimens = rememberResponsiveDimens()
    Surface(
        modifier = modifier.shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
    ) {
        Card(
            onClick = {
                navController.navigate("doctors/${speciality.displayName}")
            },
            modifier = modifier.padding(vertical = 2.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f),
                contentColor = DefaultOnPrimary
            ),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = dimens.w(12 / 411f), vertical = dimens.h(12 / 914f))
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(dimens.w(48 / 411f))
                        .clip(RoundedCornerShape(12.dp))
                        .background(DefaultPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = speciality.icon),
                        contentDescription = speciality.displayName,
                        tint = DefaultPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = speciality.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

        }
    }

}


