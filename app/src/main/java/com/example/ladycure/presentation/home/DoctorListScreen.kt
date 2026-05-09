package com.example.ladycure.presentation.home

import SwipeCard
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.example.ladycure.data.repository.DoctorRepository
import com.example.ladycure.domain.model.Doctor
import com.example.ladycure.presentation.booking.RatingBar
import com.example.ladycure.ui.theme.DefaultBackground
import com.example.ladycure.ui.theme.DefaultOnPrimary
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.YellowOrange
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import com.example.ladycure.utility.SnackbarController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun DoctorsListScreen(
    navController: NavHostController,
    speciality: String,
    snackbarController: SnackbarController
) {
    val dimens = rememberResponsiveDimens()
    val doctorRepo = DoctorRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
    val doctors = remember { mutableStateOf<List<Doctor>>(emptyList()) }
    var selectedDoctor = remember { mutableStateOf<Doctor?>(null) }

    var showSwipingScreen by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var swipeableDoctors by remember { mutableStateOf(doctors.value) }

    LaunchedEffect(speciality) {
        val result = doctorRepo.getDoctorsBySpeciality(speciality)
        if (result.isSuccess) {
            doctors.value = result.getOrDefault(emptyList())
            isLoading = false
            swipeableDoctors = doctors.value.shuffled()
        } else {
            isLoading = false
            snackbarController.showMessage(
                message = "Failed to load doctors: ${result.exceptionOrNull()?.message}",
            )
        }
    }

    if (isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = DefaultPrimary)
            Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))
            Text("Loading doctors...", color = DefaultOnPrimary)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground)
                .padding(horizontal = dimens.w(16 / 411f), vertical = dimens.h(16 / 914f)),
            verticalArrangement = Arrangement.spacedBy(dimens.h(16 / 914f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(dimens.w(48 / 411f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back",
                        tint = DefaultOnPrimary,
                    )
                }
                Spacer(modifier = Modifier.width(dimens.w(16 / 411f)))
                Text(
                    text = "Doctors in ${speciality}",
                    style = MaterialTheme.typography.titleLarge,
                    color = DefaultOnPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            if (doctors.value.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "We are sorry, no doctors found in this category.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = DefaultOnPrimary
                    )
                }
            } else {
                OutlinedButton(
                    onClick = {
                        showSwipingScreen = !showSwipingScreen; swipeableDoctors =
                        doctors.value.shuffled()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.w(20 / 411f)),
                    border = BorderStroke(
                        width = 1.dp,
                        color = DefaultPrimary.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DefaultPrimary,
                    )
                ) {
                    Text(
                        text = if (!showSwipingScreen) {
                            "Try a Swiping Function!"
                        } else {
                            "Go back to the list."
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = DefaultPrimary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (!showSwipingScreen) {
                            Icons.Default.Swipe
                        } else {
                            Icons.AutoMirrored.Filled.List
                        },
                        contentDescription = "Toggle Swiping",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(dimens.w(20 / 411f))
                    )
                }

            }

            if (showSwipingScreen) {
                Text(
                    text = "Swipe to select a doctor",
                    style = MaterialTheme.typography.titleLarge,
                    color = DefaultOnPrimary.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        start = dimens.w(16 / 411f),
                        bottom = dimens.h(16 / 914f)
                    )
                )

                Box {
                    for (doctor in swipeableDoctors) {
                        if (swipeableDoctors.isNotEmpty()) {
                            SwipeCard(
                                onSwipeLeft = {
                                    swipeableDoctors = emptyList()
                                    selectedDoctor.value = doctor
                                    navController.navigate(
                                        "services/${
                                            selectedDoctor.value?.id
                                        }"
                                    )

                                },
                                onSwipeRight = {
                                    swipeableDoctors =
                                        swipeableDoctors.filter { it != doctor }

                                },
                            ) {
                                ExpandedDoctorInfoCard(doctor)
                            }
                        }
                    }
                }

                if (swipeableDoctors.isNotEmpty()) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = dimens.w(20 / 411f),
                                end = dimens.w(20 / 411f),
                                bottom = dimens.h(16 / 914f),
                                top = 0.dp
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Don't like",
                            tint = DefaultOnPrimary,
                            modifier = Modifier.size(dimens.w(36 / 411f))
                        )

                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Like",
                            tint = DefaultPrimary,
                            modifier = Modifier.size(dimens.w(36 / 411f))
                        )

                    }
                }

                if (swipeableDoctors.isEmpty() && selectedDoctor.value == null) {
                    Text(
                        text = "Looks like you've seen all the doctors. Didn't find the one yet? Would you like to start again?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = DefaultOnPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimens.w(20 / 411f))
                            .align(Alignment.CenterHorizontally)
                    )
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = dimens.w(20 / 411f),
                                vertical = dimens.h(20 / 914f)
                            ),
                        onClick = {
                            swipeableDoctors = doctors.value.shuffled()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DefaultPrimary.copy(alpha = 0.7f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Start again")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Start again",
                            tint = Color.White,
                            modifier = Modifier.size(dimens.w(20 / 411f))
                        )
                    }
                }

            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(dimens.h(16 / 914f))
                ) {
                    items(doctors.value) { doctor ->
                        DoctorInfoCard(doctor, onSelect = {
                            selectedDoctor.value = doctor
                            navController.navigate("services/${selectedDoctor.value?.id}")
                        }, modifier = Modifier.padding(bottom = dimens.h(16 / 914f)))
                    }
                }
            }
        }
    }

}


@Composable
fun DoctorInfoCard(
    doctor: Doctor,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = rememberResponsiveDimens()

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(dimens.w(16 / 411f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                if (doctor.profilePictureUrl.isEmpty()) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Doctor ${doctor.name}",
                        modifier = Modifier
                            .size(dimens.w(80 / 411f))
                            .clip(CircleShape),
                        tint = Color.Gray
                    )
                } else {
                    SubcomposeAsyncImage(
                        model = doctor.profilePictureUrl,
                        contentDescription = "Doctor ${doctor.name}",
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = DefaultPrimary,
                                    strokeWidth = 3.dp
                                )
                            }
                        },
                        modifier = Modifier
                            .size(dimens.w(80 / 411f))
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }


                Spacer(modifier = Modifier.width(dimens.w(16 / 411f)))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Dr. ${doctor.name} ${doctor.surname}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = DefaultOnPrimary
                    )

                    Text(
                        text = doctor.speciality.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultPrimary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        RatingBar(
                            rating = doctor.rating,
                            modifier = Modifier.width(dimens.w(80 / 411f))
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "(${"%.1f".format(doctor.rating)})",
                            style = MaterialTheme.typography.labelMedium,
                            color = YellowOrange
                        )
                    }

                }
            }

            Spacer(modifier = Modifier.height(dimens.h(12 / 914f)))

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min), // Use intrinsic height
                    verticalAlignment = Alignment.CenterVertically // Center all items vertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = "Experience",
                        tint = DefaultPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp) // Fixed size for all icons
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${doctor.experience} years experience",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Address",
                        tint = DefaultPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = doctor.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Languages",
                        tint = DefaultPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Speaks: ${doctor.languages.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            var expanded by remember { mutableStateOf(false) }
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Bio",
                        tint = DefaultPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "About Dr. ${doctor.surname}",
                        style = MaterialTheme.typography.labelLarge,
                        color = DefaultOnPrimary.copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = doctor.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = if (expanded) "Show less" else "Show more",
                        color = DefaultPrimary
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimens.h(12 / 914f)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Consultation fee",
                        style = MaterialTheme.typography.labelMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "$${doctor.consultationPrice}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DefaultPrimary
                    )
                }

                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DefaultPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.width(dimens.w(120 / 411f))
                ) {
                    Text("Select")
                }
            }
        }
    }
}

@Composable
private fun ExpandedDoctorInfoCard(
    doctor: Doctor,
    modifier: Modifier = Modifier
) {
    val dimens = rememberResponsiveDimens()

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = dimens.w(20 / 411f), vertical = dimens.h(20 / 914f))
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (doctor.profilePictureUrl.isEmpty()) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Doctor ${doctor.name}",
                    modifier = Modifier
                        .size(dimens.w(200 / 411f))
                        .clip(CircleShape),
                    tint = Color.Gray
                )
            } else {
                SubcomposeAsyncImage(
                    model = doctor.profilePictureUrl,
                    contentDescription = "Doctor ${doctor.name}",
                    loading = {
                        Box(
                            modifier = Modifier.size(dimens.w(200 / 411f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = DefaultPrimary,
                                strokeWidth = 3.dp
                            )
                        }
                    },
                    modifier = Modifier
                        .size(dimens.w(200 / 411f))
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {


                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Dr. ${doctor.name} ${doctor.surname}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = DefaultOnPrimary
                    )

                    Text(
                        text = doctor.speciality.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultPrimary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        RatingBar(
                            rating = doctor.rating,
                            modifier = Modifier.width(dimens.w(80 / 411f))
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "(${"%.1f".format(doctor.rating)})",
                            style = MaterialTheme.typography.labelMedium,
                            color = YellowOrange
                        )
                    }

                }
            }

            Spacer(modifier = Modifier.height(dimens.h(12 / 914f)))

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min), // Use intrinsic height
                    verticalAlignment = Alignment.CenterVertically // Center all items vertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = "Experience",
                        tint = DefaultPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp) // Fixed size for all icons
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${doctor.experience} years experience",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Address",
                        tint = DefaultPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = doctor.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Languages",
                        tint = DefaultPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Speaks: ${doctor.languages.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = "Consultation fee",
                        tint = DefaultPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Consultation fee: $${doctor.consultationPrice}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))


            var expanded by remember { mutableStateOf(false) }
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Bio",
                        tint = DefaultPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "About Dr. ${doctor.surname}",
                        style = MaterialTheme.typography.labelLarge,
                        color = DefaultOnPrimary.copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = doctor.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = if (expanded) "Show less" else "Show more",
                        color = DefaultPrimary
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

}
