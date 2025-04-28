package com.example.ladycure

import LadyCureTheme
import SnackbarBackground
import SnackbarContentColor
import SnackbarActionColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ladycure.data.AppointmentType
import com.example.ladycure.data.doctor.Speciality
import com.example.ladycure.presentation.home.components.BottomNavBar
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.utility.SnackbarController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            LadyCureTheme {
                val navController = rememberNavController()
                MainScreen(navController = navController)
            }
        }
    }
}

    @Composable
    fun MainScreen(navController: NavHostController) {
        val authRepo = AuthRepository()

        val navBackStackEntry = navController.currentBackStackEntryAsState().value
        val currentRoute = navBackStackEntry?.destination?.route

        // List of screens where BottomNavBar should be hidden
        val showBottomNavRoutes = listOf(
            "home",
            "doctor",
            "chat",
            "profile",
            "doctor_main",
        )

        val showBottomNav = currentRoute in showBottomNavRoutes

        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        val snackbarController = remember { SnackbarController(scope, snackbarHostState) }

        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    snackbar = { data ->
                        Snackbar(
                            snackbarData = data,
                            containerColor = SnackbarBackground,
                            contentColor = SnackbarContentColor,
                            actionContentColor = SnackbarActionColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                )
            },
            bottomBar = {
                if (showBottomNav) {
                    BottomNavBar(navController = navController)
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavHost(
                    navController = navController,
                    startDestination = "welcome"
                ) {
                    composable("home") { HomeScreen(navController, snackbarController) }

                    composable("profile") { ProfileScreen(navController) }
                    composable("doctor") { SearchDoctorsScreen(navController) }
                    composable("chat") { ChatScreen(navController, snackbarController) }

                    composable("welcome") { WelcomeScreen(navController) }
                    composable("login") { LoginScreen(navController, snackbarController ) }
                    composable("register") { RegisterScreen(navController) }

                    composable("doctor_main") { DoctorHomeScreen(navController) }
                    composable("set_availability") { SetAvailabilityScreen(navController, snackbarController) }

                    composable("doctors/{speciality}") { backStackEntry ->
                        val speciality = backStackEntry.arguments?.getString("speciality") ?: ""
                        DoctorsListScreen(navController, speciality)
                    }
                    composable("book_appointment/{city}/{service}") { backStackEntry ->
                        val city = backStackEntry.arguments?.getString("city") ?: ""
                        val service = backStackEntry.arguments?.getString("service") ?: ""
                        BookAppointmentScreen(navController, snackbarController, city, AppointmentType.fromDisplayName(service))
                    }
                    composable("confirmation/{doctorId}/{date}/{time}/{appointmentType}") {backStackEntry ->
                        val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
                        val time = backStackEntry.arguments?.getString("time") ?: ""
                        val date = backStackEntry.arguments?.getString("date") ?: ""
                        val appointmentType = backStackEntry.arguments?.getString("appointmentType")

                        ConfirmationScreen(navController, snackbarController, doctorId, date, time, AppointmentType.fromDisplayName(appointmentType!!))
                    }

                    composable("services/{city}/{speciality}") { backStackEntry ->
                        val city = backStackEntry.arguments?.getString("city") ?: ""
                        val speciality = backStackEntry.arguments?.getString("speciality")
                        SelectServiceScreen(navController, snackbarController, null, city, Speciality.fromDisplayName(speciality!!)) }

                    composable("services/{doctor}") { backStackEntry ->
                        val doctor = backStackEntry.arguments?.getString("doctor") ?: ""
                        SelectServiceScreen(navController, snackbarController, doctor, null, null)
                    }

                    composable("book_appointment_dir/{doctor}/{service}") { backStackEntry ->
                        val doctor = backStackEntry.arguments?.getString("doctor") ?: ""
                        val service = backStackEntry.arguments?.getString("service") ?: ""
                        BookAppointmentDirectlyScreen(navController,snackbarController, doctor, AppointmentType.fromDisplayName(service))
                    }
                    
                }
            }
        }
    }


