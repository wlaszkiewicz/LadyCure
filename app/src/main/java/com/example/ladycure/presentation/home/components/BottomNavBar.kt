package com.example.ladycure.presentation.home.components

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ladycure.data.repository.UserRepository

sealed class Screen(
    val route: String,
    val icon: ImageVector,
    val label: String,
    val allowedRoles: List<String> = listOf("user", "doctor")
) {
    object Home : Screen("home", Icons.Default.Home, "Home")
    object Doctors : Screen("doctor", Icons.Default.Face, "Doctors", listOf("user"))
    object Chat : Screen("chat", Icons.Default.Call, "Chat")
    object PeriodTracker :
        Screen("period_tracker", Icons.Default.DateRange, "Tracker", listOf("user"))

    object Profile : Screen("profile", Icons.Default.AccountCircle, "Profile")
    object AdminDashboard : Screen("admin", Icons.Default.Dashboard, "Dashboard", listOf("admin"))
    object AdminUserManagement : Screen(
        "admin_user_management",
        Icons.Default.AccountCircle,
        "Users",
        listOf("admin")
    )

    object Availability :
        Screen("set_availability", Icons.Default.Schedule, "Availability", listOf("doctor"))

    object Earnings :
        Screen("earnings", Icons.Default.MonetizationOn, "Earnings", listOf("doctor"))

    object AdminDoctorManagement :
        Screen("admin_doctor_management", Icons.Default.Face, "Doctors", listOf("admin"))

    object AdminAnalytics :
        Screen("admin_analytics", Icons.Default.Analytics, "Analytics", listOf("admin"))

    companion object {
        val allScreens = listOf(
            Home,
            Doctors,
            Chat,
            PeriodTracker,
            Availability,
            Earnings,
            Profile,
            AdminDashboard,
            AdminUserManagement,
            AdminDoctorManagement,
            AdminAnalytics,
        )

        fun getRouteForRole(route: String, role: String?): String {
            return if (route == "home" && role == "doctor") "doctor_main" else route
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val userRepo = UserRepository()
    val userRole = remember { mutableStateOf<String?>(null) }

    // Fetch user role
    LaunchedEffect(Unit) {
        val result = userRepo.getUserRole()
        if (result.isSuccess) {
            userRole.value = result.getOrNull()
        }
    }

    // Filter screens based on allowed roles
    val visibleItems = remember(userRole.value) {
        Screen.allScreens.filter { screen ->
            screen.allowedRoles.contains(userRole.value)
        }
    }

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .background(DefaultPrimary.copy(alpha = 0.08f)),
        containerColor = DefaultBackground,
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route

        visibleItems.forEach { screen ->
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
                selected = currentRoute == screen.route ||
                        (screen == Screen.Home && currentRoute == "doctor_main"),
                onClick = {
                    val targetRoute = Screen.getRouteForRole(screen.route, userRole.value)
                    navController.navigate(targetRoute) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DefaultPrimary,
                    selectedTextColor = DefaultPrimary,
                    unselectedIconColor = DefaultOnPrimary.copy(alpha = 0.6f),
                    unselectedTextColor = DefaultOnPrimary.copy(alpha = 0.6f),
                    indicatorColor = DefaultPrimary.copy(alpha = 0.2f)
                ),
            )
        }
    }
}