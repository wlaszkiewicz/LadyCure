package com.example.ladycure

import LadyCureTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialogDefaults.shape
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

// Color palette
val Pink10 = Color(0xFFFFF0F3)
val Pink20 = Color(0xFFFED6E1)
val Pink30 = Color(0xFFFDB8CF)
val Pink40 = Color(0xFFFC9ABD)
val Pink80 = Color(0xFFC4809E)
val Pink90 = Color(0xFFA14E73)

val Neutral99 = Color(0xFF0A0A0A)

val DefaultBackground = Pink10
val DefaultPrimary = Pink40
val DefaultPrimaryVariant =Pink80
val DefaultSecondary =Pink30
val DefaultSecondaryVariant =Pink90
val DefaultOnPrimary = Neutral99

// Navigation destinations
sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    object Home : Screen("home", Icons.Default.Home, "Home")
    object Doctors : Screen("doctor", Icons.Default.Face, "Doctors")
    object Chat : Screen("chat", Icons.Default.Call, "Chat")
    object Profile : Screen("profile", Icons.Default.AccountCircle, "Profile")
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(
        Screen.Home,
        Screen.Doctors,
        Screen.Chat,
        Screen.Profile
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(DefaultPrimary.copy(alpha = 0.08f)),
        containerColor = DefaultBackground,
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = screen.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DefaultPrimary,
                    selectedTextColor = DefaultPrimary,
                    unselectedIconColor = DefaultOnPrimary.copy(alpha = 0.6f),
                    unselectedTextColor = DefaultOnPrimary.copy(alpha = 0.6f),
                    indicatorColor = DefaultPrimary.copy(alpha = 0.2f)
                )
            )
        }
    }
}

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) { HomeContent(navController) }
        composable(Screen.Doctors.route) { DoctorsContent(navController) }
        composable(Screen.Chat.route) { ChatContent(navController) }
        composable(Screen.Profile.route) { ProfileContent(navController) }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavigationGraph(navController = navController)
        }
    }
}

@Composable
fun HomeContent(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header with greeting
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hello, User Name", // Replace with actual user name
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = DefaultPrimary
                )
            }

            // User avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(DefaultPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = DefaultPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Health tips card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = DefaultPrimary.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Daily Health Tip",
                    style = MaterialTheme.typography.titleLarge,
                    color = DefaultPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Stay hydrated and drink at least 8 glasses of water daily for optimal health.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DefaultOnPrimary.copy(alpha = 0.8f)
                )
            }
        }

        // Quick actions
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            color = DefaultPrimary,
            modifier = Modifier.padding(top = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuickActionButton(
                icon = Icons.Default.Face,
                label = "Doctors",
                onClick = { navController.navigate(Screen.Doctors.route) }
            )
            QuickActionButton(
                icon = Icons.Default.Call,
                label = "Chat",
                onClick = { navController.navigate(Screen.Chat.route) }
            )
        }
    }
}

@Composable
fun QuickActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DefaultPrimary.copy(alpha = 0.1f)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = DefaultPrimary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = DefaultPrimary
            )
        }
    }
}

@Composable
fun DoctorsContent(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Find Doctors",
            style = MaterialTheme.typography.headlineMedium,
            color = DefaultPrimary,
            fontWeight = FontWeight.Bold
        )

        // Search bar
        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = DefaultBackground,
                unfocusedContainerColor = DefaultBackground,
                focusedIndicatorColor = DefaultPrimary,
                unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f)
            ),
            placeholder = {
                Text("Search for specialists...", color = DefaultOnPrimary.copy(alpha = 0.5f))
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_home),
                    contentDescription = "Search",
                    tint = DefaultPrimary
                )
            }
        )

        // Doctor categories
        Text(
            text = "Specialties",
            style = MaterialTheme.typography.titleMedium,
            color = DefaultPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DoctorCategory("Gynecology")
            DoctorCategory("Cardiology")
            DoctorCategory("Dermatology")
        }
    }
}

@Composable
fun DoctorCategory(name: String) {
    Card(
        modifier = Modifier.size(120.dp, 80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DefaultPrimary.copy(alpha = 0.1f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                color = DefaultPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ChatContent(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Chat with Experts",
            style = MaterialTheme.typography.headlineMedium,
            color = DefaultPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Empty state illustration
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.baseline_chat),
                contentDescription = "No chats",
                modifier = Modifier.size(180.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No active chats",
                style = MaterialTheme.typography.titleMedium,
                color = DefaultOnPrimary.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Start a conversation with a healthcare professional",
                style = MaterialTheme.typography.bodyMedium,
                color = DefaultOnPrimary.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.navigate(Screen.Doctors.route) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultPrimary,
                    contentColor = DefaultOnPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Find Doctors")
            }
        }
    }
}

@Composable
fun ProfileContent(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Profile header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(DefaultPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = DefaultPrimary,
                    modifier = Modifier.size(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "User Name",
                style = MaterialTheme.typography.headlineMedium,
                color = DefaultPrimary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "user@example.com",
                style = MaterialTheme.typography.bodyMedium,
                color = DefaultOnPrimary.copy(alpha = 0.6f)
            )
        }

        // Settings options
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium,
                color = DefaultPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ProfileOption("Account Settings", Icons.Default.AccountCircle)
            ProfileOption("Notifications", Icons.Default.Notifications)
            ProfileOption("Privacy", Icons.Default.Lock)
            ProfileOption("Help & Support", Icons.Default.Home)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* Handle logout */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = DefaultPrimary,
                contentColor = DefaultOnPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Sign Out")
        }
    }
}

@Composable
fun ProfileOption(text: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DefaultPrimary.copy(alpha = 0.1f)
        ),
        onClick = { /* Handle option click */ }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = DefaultPrimary
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = DefaultOnPrimary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenPreview() {
    LadyCureTheme {
        MainScreen()
    }
}