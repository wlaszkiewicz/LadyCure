package com.example.ladycure

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import  androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.ladycure.data.doctor.Specialization
import com.example.ladycure.presentation.home.components.BottomNavBar
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow

@Composable
fun SearchDoctorsScreen(navController: NavHostController) {
    val searchQuery = remember { mutableStateOf("") }

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground)
                .padding(innerPadding)
                .scrollable(enabled = false, state = rememberScrollState(), orientation = Orientation.Vertical),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, top = 20.dp),
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
                        text = "Find your specialist",
                        style = MaterialTheme.typography.titleLarge,
                        color = DefaultOnPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                SearchBar(
                    value = searchQuery.value,
                    onValueChange = { searchQuery.value = it },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                PopularCategories(navController)
            }

            item {
                Text(
                    text = "All Specializations",
                    style = MaterialTheme.typography.titleLarge,
                    color = DefaultPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            items(Specialization.entries.chunked(2)) { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    rowItems.forEach { spec ->
                        DoctorSpecializationCard(
                            specialization = spec,
                            navController = navController,
                            modifier = Modifier.weight(1f)
                        )
                    }

//                    // If there's only 1 item in this row, add an empty box to balance
//                    if (rowItems.size == 1) {
//                        Spacer(modifier = Modifier.weight(1f))
//                    }
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
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
                color = DefaultOnPrimary.copy(alpha = 0.5f))
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = DefaultOnPrimary,
                modifier = Modifier.size(24.dp))
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = DefaultOnPrimary.copy(alpha = 0.5f))
                }
            }
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun PopularCategories(navController: NavHostController) {

    Column(
        modifier = Modifier
    ) {
        Text(
            text = "Popular Categories",
            style = MaterialTheme.typography.titleLarge,
            color = DefaultPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp, start = 16.dp))


        val specializationColors = listOf(Color(0xFFFFF0F5),
            Color(0xFFF0F8FF),
            Color(0xFFFAFAD2),
            Color(0xFFE9FFEB),
            Color(0xFFE2DCFA)
        )

        val categories = Specialization.popularCategories

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            items(categories) { category ->
                val cardColor = specializationColors[categories.indexOf(category) % specializationColors.size]

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
    category: Specialization,
    onClick: () -> Unit
) {
    Surface(modifier = Modifier.shadow(elevation = 2.dp, shape =RoundedCornerShape(20.dp)) // Apply shadow here
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .width(140.dp)
                .height(80.dp),
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
                        modifier = Modifier.size(28.dp)
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
private fun DoctorSpecializationCard(
    specialization: Specialization,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.shadow(elevation = 2.dp, shape =RoundedCornerShape(20.dp))
    ) {
        Card(
            onClick = {
                navController.navigate("doctors/${specialization.displayName}")
            },
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f),
                contentColor = DefaultOnPrimary
            ),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DefaultPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = specialization.icon),
                        contentDescription = specialization.displayName,
                        tint = DefaultPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = specialization.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View",
                    tint = DefaultPrimary.copy(alpha = 0.5f)
                )
            }

        }
    }

}

@Preview
@Composable
fun SearchDoctorsScreenPreview() {
    SearchDoctorsScreen(navController = rememberNavController())
}


