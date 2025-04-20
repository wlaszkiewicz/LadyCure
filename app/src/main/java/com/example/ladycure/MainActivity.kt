package com.example.ladycure
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import LadyCureTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ladycure.data.doctor.Specialization

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

