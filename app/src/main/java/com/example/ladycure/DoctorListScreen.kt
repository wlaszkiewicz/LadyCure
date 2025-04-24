package com.example.ladycure

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import SwipeCard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ladycure.presentation.home.components.BottomNavBar
import com.example.ladycure.repository.AuthRepository
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import kotlin.math.floor

@Composable
fun DoctorsListScreen(navController: NavHostController, specification: String) {
    val repository = AuthRepository()
    val doctors = remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var selectedDoctor = remember { mutableStateOf<Map<String, Any>?>(null) }

    var showSwipingScreen by remember {mutableStateOf(false)}
    var isLoading by remember { mutableStateOf(true) }
    var swipeableDoctors by remember { mutableStateOf(doctors.value) }

    LaunchedEffect(specification) {
        val result = repository.getDoctorsBySpecification(specification)
        if (result.isSuccess) {
            doctors.value = result.getOrDefault(emptyList())
            isLoading = false
            swipeableDoctors = doctors.value.shuffled()
        }
    }


    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = DefaultPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading doctors...", color = DefaultOnPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DefaultBackground)
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go back",
                                tint = DefaultOnPrimary,
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Doctors in ${specification}",
                            style = MaterialTheme.typography.titleLarge,
                            color = DefaultOnPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (doctors.value.isEmpty()) {
                    item {
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
                    }
                } else {
                    item {
                        TextButton(
                            onClick = {
                                showSwipingScreen = !showSwipingScreen; swipeableDoctors =
                                doctors.value.shuffled()
                            },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = DefaultPrimary.copy(alpha = 0.1f) // Example background color
                            )
                        ) {
                            Text(
                                text = if (!showSwipingScreen) {
                                    "Try a Swiping Function!"
                                } else {
                                    "Go back to the list."
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = DefaultPrimary
                            )
                        }
                    }

                }

                if (showSwipingScreen) {
                    item {
                        Text(
                            text = "Swipe to select a doctor",
                            style = MaterialTheme.typography.titleLarge,
                            color = DefaultOnPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    item {
                        if (swipeableDoctors.isNotEmpty()) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 20.dp,
                                        end = 20.dp,
                                        bottom = 16.dp,
                                        top = 0.dp
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Don't like",
                                    tint = DefaultPrimary,
                                    modifier = Modifier.size(30.dp)
                                )

                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Like",
                                    tint = DefaultPrimary,
                                    modifier = Modifier.size(30.dp)
                                )

                            }
                        }
                    }

                    item {
                        Box {
                            for (doctor in swipeableDoctors) {
                                if (swipeableDoctors.isNotEmpty()) {
                                    SwipeCard(
                                        onSwipeLeft = {
                                            swipeableDoctors = emptyList()
                                            selectedDoctor.value = doctor
                                            navController.navigate(
                                                "services/${
                                                    selectedDoctor.value?.get(
                                                        "id"
                                                    )
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
                    }
                    item {
                        if (swipeableDoctors.isEmpty() && selectedDoctor.value == null) {
                            Text(
                                text = "Looks like you’ve seen all the doctors. Didn’t find the one yet? Would you like to start again?",
                                style = MaterialTheme.typography.bodyLarge,
                                color = DefaultOnPrimary,
                            )
                            Button(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
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
                            }
                        }
                    }

                } else {
                    items(doctors.value) { doctor ->
                        DoctorInfoCard(doctor, onSelect = {
                            selectedDoctor.value = doctor
                            navController.navigate("services/${selectedDoctor.value?.get("id")}")
                        }, modifier = Modifier.padding(bottom = 16.dp))
                    }
                }
            }
        }

    }

}

@Composable
fun DoctorInfoCard(
    doctor: Map<String, Any>,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name = doctor["name"] as? String ?: "Dr. Unknown"
    val surname = doctor["surname"] as? String ?: "Unknown"
    val specialization = doctor["specification"] as? String ?: "Specialist"
    val address = doctor["address"] as? String ?: "Unknown"
    val imageUrl = doctor["profilePictureUrl"] as? String ?: ""
    val bio = doctor["bio"] as? String ?: "Experienced medical professional"
    val languages = doctor["languages"] as? List<String> ?: listOf("English")
    val consultationFee = (doctor["consultationFee"] as? Double) ?: 100.0

    val experience = when (val exp = doctor["experience"]) {
        is Int -> exp
        is Long -> exp.toInt()
        is Double -> exp.toInt()
        is String -> exp.toIntOrNull() ?: 5
        else -> 5
    }

    val rating = when (val rat = doctor["rating"]) {
        is Int -> rat.toDouble()
        is Long -> rat.toDouble()
        is Double -> rat
        is String -> rat.toDouble()
        else -> 4.5
    }


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
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row with image and basic info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Doctor image
                if (imageUrl.isEmpty()) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Doctor $name",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        tint = Color.Gray
                    )
                } else {
                    SubcomposeAsyncImage(
                        model = imageUrl,
                        contentDescription = "Doctor $name",
                        loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = DefaultPrimary,
                                    strokeWidth = 3.dp
                                )
                            }
                        },
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }


                Spacer(modifier = Modifier.width(16.dp))

                // Doctor basic info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Dr. $name $surname",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = DefaultOnPrimary
                    )

                    Text(
                        text = specialization,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultPrimary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )

                    // Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        RatingBar(
                            rating = rating,
                            modifier = Modifier.width(80.dp))

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "(${"%.1f".format(rating)})",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFFFA000)
                        )
                    }

                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Doctor details section
            Column {
                // Experience
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
                        text = "$experience years experience",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Address
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
                        text = address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Languages
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
                        text = "Speaks: ${languages.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

                // Bio (collapsible)
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
                            text = "About Dr. ${surname}",
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
                            text = bio,
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

            Spacer(modifier = Modifier.height(12.dp))

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
                        text = "$${"%.2f".format(consultationFee)}",
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
                    modifier = Modifier.width(120.dp)
                ) {
                    Text("Select")
                }
            }
        }
    }
}

@Composable
private fun ExpandedDoctorInfoCard(
    doctor: Map<String, Any>,
    modifier: Modifier = Modifier
) {
    val name = doctor["name"] as? String ?: "Dr. Unknown"
    val surname = doctor["surname"] as? String ?: "Unknown"
    val specialization = doctor["specification"] as? String ?: "Specialist"
    val address = doctor["address"] as? String ?: "Unknown"
    val imageUrl = doctor["profilePictureUrl"] as? String ?: ""
    val bio = doctor["bio"] as? String ?: "Experienced medical professional"
    val languages = doctor["languages"] as? List<String> ?: listOf("English")
    val consultationFee = (doctor["consultationFee"] as? Double) ?: 100.0

    val experience = when (val exp = doctor["experience"]) {
        is Int -> exp
        is Long -> exp.toInt()
        is Double -> exp.toInt()
        is String -> exp.toIntOrNull() ?: 5
        else -> 5
    }

    val rating = when (val rat = doctor["rating"]) {
        is Int -> rat.toDouble()
        is Long -> rat.toDouble()
        is Double -> rat
        is String -> rat.toDouble()
        else -> 4.5
    }


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
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Doctor image
            if (imageUrl.isEmpty()) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Doctor $name",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    tint = Color.Gray
                )
            } else {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "Doctor $name",
                    loading = {
                        Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center
                            ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = DefaultPrimary,
                                strokeWidth = 3.dp
                            )
                        }
                    },
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Header row with image and basic info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {


                // Doctor basic info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Dr. $name $surname",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = DefaultOnPrimary
                    )

                    Text(
                        text = specialization,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultPrimary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )

                    // Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        RatingBar(
                            rating = rating,
                            modifier = Modifier.width(80.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "(${"%.1f".format(rating)})",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFFFA000)
                        )
                    }

                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Doctor details section
            Column {
                // Experience
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
                        text = "$experience years experience",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Address
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
                        text = address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Languages
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
                        text = "Speaks: ${languages.joinToString(", ")}",
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
                        text = "Consultation fee: $${"%.2f".format(consultationFee)}",
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
                        text = "About Dr. $surname",
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
                        text = bio,
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

@Composable
private fun RatingBar(
    rating: Double,
    modifier: Modifier = Modifier
) {
    val filledStars = floor(rating).toInt()
    val halfStar = rating - filledStars >= 0.5
    val emptyStars = 5 - filledStars - (if (halfStar) 1 else 0)

    Row(modifier = modifier) {
        repeat(filledStars) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Filled star",
                tint = Color(0xFFFFA000),
                modifier = Modifier.size(16.dp)
            )
        }
        if (halfStar) {
            Icon(
                imageVector = Icons.Default.StarHalf,
                contentDescription = "Half star",
                tint = Color(0xFFFFA000),
                modifier = Modifier.size(16.dp)
            )
        }
        repeat(emptyStars) {
            Icon(
                imageVector = Icons.Default.StarOutline,
                contentDescription = "Empty star",
                tint = Color(0xFFFFA000),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Preview
@Composable
fun ExpendedDoctorInfoCardPreview() {
    val sampleDoctor = mapOf(
        "name" to "Sarah",
        "surname" to "Johnson",
        "specification" to "Cardiologist",
        "address" to "123 Medical Center Drive, Suite 456, New York, NY 10001",
        "rating" to 4.7,
        "experience" to 12,
        "profilePictureUrl" to "",
        "bio" to "Dr. Johnson is a board-certified cardiologist with over 12 years of experience in treating heart conditions. She specializes in preventive cardiology and non-invasive treatments.",
        "languages" to listOf("English", "Spanish", "French"),
        "consultationFee" to 150.0
    )

    ExpandedDoctorInfoCard(
        doctor = sampleDoctor,
        modifier = Modifier.padding(16.dp)
    )
}

@Preview
@Composable
fun DoctorInfoCardPreview() {
    val sampleDoctor = mapOf(
        "name" to "Sarah",
        "surname" to "Johnson",
        "specification" to "Cardiologist",
        "address" to "123 Medical Center Drive, Suite 456, New York, NY 10001",
        "rating" to 4.7,
        "experience" to 12,
        "profilePictureUrl" to "",
        "bio" to "Dr. Johnson is a board-certified cardiologist with over 12 years of experience in treating heart conditions. She specializes in preventive cardiology and non-invasive treatments.",
        "languages" to listOf("English", "Spanish", "French"),
        "consultationFee" to 150.0
    )

    DoctorInfoCard(
        doctor = sampleDoctor,
        onSelect = {},
        modifier = Modifier.padding(16.dp)
    )
}