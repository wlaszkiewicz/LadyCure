package com.example.ladycure
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
import com.example.ladycure.data.doctor.Specialization
import com.example.ladycure.presentation.home.components.Screen

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
        composable("book_appointment/{city}/{specialization}") { backStackEntry ->
            val city = backStackEntry.arguments?.getString("city") ?: ""
            val specialization = backStackEntry.arguments?.getString("specialization") ?: ""
            BookAppointmentScreen(navController, city, Specialization.valueOf(specialization.uppercase()))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LadyCureTheme {
        AppNavigation()
    }

}

