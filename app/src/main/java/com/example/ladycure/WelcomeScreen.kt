package com.example.ladycure

import LadyCureTheme
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.text.style.TextAlign
import com.example.ladycure.repository.AuthRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun WelcomeScreen(navController: NavController) {
    val context = LocalContext.current
    val isLoggedIn by remember { mutableStateOf(checkIfUserIsLoggedIn(context)) }

    LoadingScreen(isLoading = isLoggedIn)

    // If user is logged in, navigate to home screen
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate("home") {
                // Clear back stack so user can't go back to welcome screen
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
        }
    }

    // Only show the gender selection/login screen if user is not logged in
    if (!isLoggedIn) {
        WelcomeContent(navController)
    }
}

@Composable
fun LoadingScreen(isLoading: Boolean) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun WelcomeContent(navController: NavController) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(true) }
    var shouldShowUninstall by remember { mutableStateOf(false) }
    var ITSAMAN by remember { mutableStateOf(false) }

    val uninstallLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (isAppInstalled(context)) {
            shouldShowUninstall = true
        }
    }

    LaunchedEffect(shouldShowUninstall) {
        if (shouldShowUninstall) {
            val packageName = context.packageName
            val intent = Intent(Intent.ACTION_DELETE, Uri.fromParts("package", packageName, null))
            uninstallLauncher.launch(intent)
            shouldShowUninstall = false
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = "Select Your Gender",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { showDialog = false },
                        modifier = Modifier.width(150.dp)
                    ) {
                        Text("Female")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            shouldShowUninstall = true
                            showDialog = false
                            ITSAMAN = true
                        },
                        modifier = Modifier.width(150.dp)
                    ) {
                        Text("Male")
                    }
                }
            },
            confirmButton = {},
            modifier = Modifier.wrapContentSize()
        )
    }

    if (ITSAMAN) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Go away",
                style = MaterialTheme.typography.headlineLarge,
            )
            Image(
                painter = painterResource(id = R.drawable.diva),
                contentDescription = "Capybara background",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp),
                contentScale = ContentScale.Crop
            )
        }
    }

    if (!showDialog && !ITSAMAN) {
        LadyCureTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 32.dp),
                        contentScale = ContentScale.FillWidth
                    )
                    Button(
                        onClick = { navController.navigate("login") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                            .height(55.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Login",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { navController.navigate("register") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                            .height(55.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Register",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    }
}


fun checkIfUserIsLoggedIn(context: Context): Boolean {
    val user = Firebase.auth.currentUser
    if (user != null) {
        return true
    } else {
        return false
    }
}

@Preview
@Composable
fun WelcomeScreenPreview() {
    val navController = rememberNavController()
    LadyCureTheme {
        WelcomeScreen(navController)
    }
}

fun isAppInstalled(context: Context): Boolean {
    return try {
        context.packageManager.getPackageInfo(context.packageName, 0)
        true
    } catch (e: Exception) {
        false
    }
}