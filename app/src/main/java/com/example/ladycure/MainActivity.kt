package com.example.ladycure

import SnackbarBackground
import SnackbarContentColorError
import SnackbarContentColorSuccess
import SnackbarContentColor
import SnackbarActionColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import LadyCureTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import com.example.ladycure.data.AppointmentType
import com.example.ladycure.data.doctor.Specialization
import com.example.ladycure.presentation.home.components.Screen
import com.example.ladycure.utility.SnackbarController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            LadyCureTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "welcome") {
        composable("welcome") { WelcomeScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("admin") { AdminScreen(navController)}
        composable("doctor") { SearchDoctorsScreen(navController) }
        composable("chat") { ChatScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Doctors.route) { SearchDoctorsScreen(navController) }
        composable(Screen.Chat.route) { ChatScreen(navController) }
        composable(Screen.Profile.route) { ProfileScreen(
            navController) }

        composable("doctors/{specification}") { backStackEntry ->
            val specification = backStackEntry.arguments?.getString("specification") ?: ""
            DoctorsListScreen(navController, specification)
        }
        composable("book_appointment/{city}/{service}") { backStackEntry ->
            val city = backStackEntry.arguments?.getString("city") ?: ""
            val service = backStackEntry.arguments?.getString("service") ?: ""
            BookAppointmentScreen(navController, city, AppointmentType.fromDisplayName(service))
        }
        composable("confirmation/{doctorId}/{date}/{time}/{appointmentType}") {backStackEntry ->
            val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
            val time = backStackEntry.arguments?.getString("time") ?: ""
            val date = backStackEntry.arguments?.getString("date") ?: ""
            val appointmentType = backStackEntry.arguments?.getString("appointmentType")

            ConfirmationScreen(navController, doctorId, date, time, AppointmentType.fromDisplayName(appointmentType!!))
        }

        composable("services/{city}/{specialization}") { backStackEntry ->
            val city = backStackEntry.arguments?.getString("city") ?: ""
            val specialization = backStackEntry.arguments?.getString("specialization")
            SelectServiceScreen(navController, city, Specialization.fromDisplayName(specialization!!)) }

//        composable("services/{doctor}") { backStackEntry ->
//            val doctor = backStackEntry.arguments?.getString("doctorId") ?: ""
//            SelectServiceScreen(navController, doctor, null, null)
//        }
//        composable("book_appointment/{doctor}/{service}") { backStackEntry ->
//            val doctor = backStackEntry.arguments?.getString("doctor") ?: ""
//            val service = backStackEntry.arguments?.getString("service") ?: ""
//            BookAppointmentScreen(navController, doctor, AppointmentType.fromDisplayName(service))
//        }

    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LadyCureTheme {
        AppNavigation()
    }

}


@Composable
fun BaseScaffold(
    content: @Composable (SnackbarController) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
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
                        shape = RoundedCornerShape(12.dp),
                        actionOnNewLine = false
                    )
                }
            )
        }
    ) {
        content(snackbarController) // Error but still works so we ignore it
    }
}


