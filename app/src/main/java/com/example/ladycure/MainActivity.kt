package com.example.ladycure

import LadyCureTheme
import SnackbarActionColor
import SnackbarBackground
import SnackbarContentColor
import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ladycure.data.repository.AuthRepository
import com.example.ladycure.domain.model.AppointmentType
import com.example.ladycure.domain.model.Speciality
import com.example.ladycure.presentation.admin.AdminAnalyticsScreen
import com.example.ladycure.presentation.admin.AdminDashboardScreen
import com.example.ladycure.presentation.admin.AdminDoctorManagementScreen
import com.example.ladycure.presentation.admin.AdminUserManagementScreen
import com.example.ladycure.presentation.applications.DoctorApplicationScreen
import com.example.ladycure.presentation.applications.DoctorPendingMainScreen
import com.example.ladycure.presentation.availability.AvailabilityListScreen
import com.example.ladycure.presentation.availability.SetAvailabilityScreen
import com.example.ladycure.presentation.booking.AppointmentsScreen
import com.example.ladycure.presentation.booking.BookAppointmentDirectlyScreen
import com.example.ladycure.presentation.booking.BookAppointmentScreen
import com.example.ladycure.presentation.booking.BookingSuccessScreen
import com.example.ladycure.presentation.booking.ConfirmationScreen
import com.example.ladycure.presentation.booking.RescheduleScreen
import com.example.ladycure.presentation.booking.SelectServiceScreen
import com.example.ladycure.presentation.chat.ChatScreen
import com.example.ladycure.presentation.chat.DoctorChatScreen
import com.example.ladycure.presentation.doctor.DoctorEarningsScreen
import com.example.ladycure.presentation.doctor.DoctorHomeScreen
import com.example.ladycure.presentation.home.DoctorsListScreen
import com.example.ladycure.presentation.home.HomeScreen
import com.example.ladycure.presentation.home.PeriodTrackerScreen
import com.example.ladycure.presentation.home.ProfileScreen
import com.example.ladycure.presentation.home.SearchDoctorsScreen
import com.example.ladycure.presentation.home.components.BottomNavBar
import com.example.ladycure.presentation.login.LoginScreen
import com.example.ladycure.presentation.register.RegisterScreen
import com.example.ladycure.presentation.welcome.WelcomeScreen
import com.example.ladycure.utility.SnackbarController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp

class MainActivity : AppCompatActivity() {
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
    AuthRepository()

    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    // List of screens where BottomNavBar should be shown
    val showBottomNavRoutes = listOf(
        "home",
        "doctor",
        "chat",
        "profile",
        "period_tracker",
        "doctor_main",
        "admin",
        "admin_user_management",
        "admin_doctor_management",
        "admin_analytics",
        "set_availability",
        "earnings",
    )

    val showBottomNav = currentRoute in showBottomNavRoutes

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val snackbarController = remember { SnackbarController(scope, snackbarHostState) }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        RequestNotificationPermissionDialog()
    }

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
                composable("home") {
                    HomeScreen(
                        navController = navController,
                        snackbarController = snackbarController,
                    )
                }

                composable("profile") { ProfileScreen(navController) }
                composable("doctor") { SearchDoctorsScreen(navController, snackbarController) }
                composable("chat") { ChatScreen(navController, snackbarController) }
                composable("period_tracker") { PeriodTrackerScreen(navController) }

                composable("admin") { AdminDashboardScreen(navController, snackbarController) }

                composable("welcome") { WelcomeScreen(navController) }
                composable("login") { LoginScreen(navController, snackbarController) }
                composable("register") { RegisterScreen(navController, snackbarController) }


                composable("admin_user_management") {
                    AdminUserManagementScreen(
                        snackbarController
                    )
                }
                composable("admin_doctor_management") {
                    AdminDoctorManagementScreen(
                        snackbarController
                    )
                }
                composable("admin_analytics") {
                    AdminAnalyticsScreen(
                        navController,
                        snackbarController
                    )
                }

                composable("doctor_application") {
                    DoctorApplicationScreen(
                        navController,
                        snackbarController
                    )
                }

                composable("doctor_pending") {
                    DoctorPendingMainScreen(
                        navController,
                        snackbarController,
                    )
                }

                composable("doctor_main") { DoctorHomeScreen(navController, snackbarController) }
                composable("set_availability") {
                    SetAvailabilityScreen(
                        navController,
                        snackbarController
                    )
                }

                composable("earnings") {
                    DoctorEarningsScreen(navController, snackbarController)
                }

                composable("availabilityList") {
                    AvailabilityListScreen(
                        navController,
                        snackbarController
                    )
                }

                composable("booking_success/{appointmentId}") { backStackEntry ->
                    val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
                    BookingSuccessScreen(navController, appointmentId, null, snackbarController)
                }

                composable("booking_success/{appointmentId}/{referral}") { backStackEntry ->
                    val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
                    val referral = backStackEntry.arguments?.getString("referral") ?: ""
                    BookingSuccessScreen(navController, appointmentId, referral, snackbarController)
                }

                composable("doctors/{speciality}") { backStackEntry ->
                    val speciality = backStackEntry.arguments?.getString("speciality") ?: ""
                    DoctorsListScreen(navController, speciality, snackbarController)
                }

                composable("confirmation/{doctorId}/{timestampSeconds}/{timestampNanoseconds}/{appointmentType}") { backStackEntry ->
                    val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
                    val timestampSeconds =
                        backStackEntry.arguments?.getString("timestampSeconds")?.toLongOrNull()
                            ?: 0L
                    val timestampNanoseconds =
                        backStackEntry.arguments?.getString("timestampNanoseconds")?.toIntOrNull()
                            ?: 0
                    val appointmentType = backStackEntry.arguments?.getString("appointmentType")

                    val timestamp = Timestamp(timestampSeconds, timestampNanoseconds)

                    ConfirmationScreen(
                        navController,
                        snackbarController,
                        doctorId,
                        timestamp,
                        AppointmentType.fromDisplayName(appointmentType!!)
                    )
                }

                composable("confirmation/{doctorId}/{timestampSeconds}/{timestampNanoseconds}/{appointmentType}/{referral}") { backStackEntry ->
                    val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
                    val timestampSeconds =
                        backStackEntry.arguments?.getString("timestampSeconds")?.toLongOrNull()
                            ?: 0L
                    val timestampNanoseconds =
                        backStackEntry.arguments?.getString("timestampNanoseconds")?.toIntOrNull()
                            ?: 0
                    val appointmentType = backStackEntry.arguments?.getString("appointmentType")
                    val referral = backStackEntry.arguments?.getString("referral")

                    val timestamp = Timestamp(timestampSeconds, timestampNanoseconds)

                    ConfirmationScreen(
                        navController,
                        snackbarController,
                        doctorId,
                        timestamp,
                        AppointmentType.fromDisplayName(appointmentType!!),
                        referral
                    )
                }

                composable("services/{city}/{speciality}") { backStackEntry ->
                    val city = backStackEntry.arguments?.getString("city") ?: ""
                    val speciality = backStackEntry.arguments?.getString("speciality")
                    SelectServiceScreen(
                        navController,
                        snackbarController,
                        null,
                        city,
                        Speciality.fromDisplayName(speciality!!)
                    )
                }

                composable("services/{doctor}") { backStackEntry ->
                    val doctor = backStackEntry.arguments?.getString("doctor") ?: ""
                    SelectServiceScreen(navController, snackbarController, doctor, null, null)
                }

                composable("book_appointment_dir/{doctor}/{service}") { backStackEntry ->
                    val doctor = backStackEntry.arguments?.getString("doctor") ?: ""
                    val service = backStackEntry.arguments?.getString("service") ?: ""
                    BookAppointmentDirectlyScreen(
                        navController,
                        snackbarController,
                        doctor,
                        AppointmentType.fromDisplayName(service)
                    )
                }
                composable("book_appointment/{city}/{service}") { backStackEntry ->
                    val city = backStackEntry.arguments?.getString("city") ?: ""
                    val service = backStackEntry.arguments?.getString("service") ?: ""
                    BookAppointmentScreen(
                        navController,
                        snackbarController,
                        city,
                        AppointmentType.fromDisplayName(service)
                    )
                }

                composable("book_appointment_dir/{doctor}/{service}/{referral}") { backStackEntry ->
                    val doctor = backStackEntry.arguments?.getString("doctor") ?: ""
                    val service = backStackEntry.arguments?.getString("service") ?: ""
                    val referral = backStackEntry.arguments?.getString("referral") ?: ""
                    BookAppointmentDirectlyScreen(
                        navController,
                        snackbarController,
                        doctor,
                        AppointmentType.fromDisplayName(service),
                        referral
                    )
                }
                composable("book_appointment/{city}/{service}/{referral}") { backStackEntry ->
                    val city = backStackEntry.arguments?.getString("city") ?: ""
                    val service = backStackEntry.arguments?.getString("service") ?: ""
                    val referral = backStackEntry.arguments?.getString("referral") ?: ""
                    BookAppointmentScreen(
                        navController,
                        snackbarController,
                        city,
                        AppointmentType.fromDisplayName(service),
                        referral
                    )
                }

                composable("reschedule/{appointmentId}") { backStackEntry ->
                    val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
                    RescheduleScreen(
                        appointmentId,
                        navController,
                        snackbarController
                    )
                }

                composable("appointments") { AppointmentsScreen(navController, snackbarController) }


                composable(
                    route = "chat/{otherUserId}/{otherUserName}", // Trasa z dwoma argumentami
                    arguments = listOf(
                        navArgument("otherUserId") { type = NavType.StringType },
                        navArgument("otherUserName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val otherUserId = backStackEntry.arguments?.getString("otherUserId")
                    val otherUserName = backStackEntry.arguments?.getString("otherUserName")
                    if (otherUserId != null && otherUserName != null) {
                        DoctorChatScreen(
                            navController = navController,
                            otherUserId = otherUserId,
                            otherUserName = otherUserName
                        )
                    } else {
                        Log.e("NavHost", "Failed to get chat arguments!")
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}


@Composable
fun PermissionDialog(onRequestPermission: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Permission Needed") },
        text = { Text("We need notification permission to keep you updated! 💌") },
        confirmButton = {
            TextButton(onClick = onRequestPermission) {
                Text("Allow")
            }
        }
    )
}

@Composable
fun RationaleDialog() {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Why We Need This?") },
        text = { Text("To send you reminders, health tips, and doctor's messages! ") },
        confirmButton = {
            TextButton(onClick = {
                val intent =
                    android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .apply {
                            putExtra(
                                android.provider.Settings.EXTRA_APP_PACKAGE,
                                context.packageName
                            )
                        }
                context.startActivity(intent)
            }) {
                Text("OK")
            }
        }
    )
}


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestNotificationPermissionDialog() {
    val permissionState =
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

    if (!permissionState.status.isGranted) {
        if (permissionState.status.shouldShowRationale) RationaleDialog()
        else PermissionDialog { permissionState.launchPermissionRequest() }
    }
}



