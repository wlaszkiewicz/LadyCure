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


@Composable
fun WelcomeScreen(navController: NavController) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(true) }
    var shouldShowUninstall by remember { mutableStateOf(false) }
    var ITSAMAN by remember { mutableStateOf(false) }

    val uninstallLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // When the user returns from the uninstall screen
        if (isAppInstalled(context)) {
            // If app is still installed, show the prompt again
            shouldShowUninstall = true
        }
    }

    // Trigger uninstall prompt when shouldShowUninstall changes
    LaunchedEffect(shouldShowUninstall) {
        if (shouldShowUninstall) {
            val packageName = context.packageName
            val intent = Intent(Intent.ACTION_DELETE, Uri.fromParts("package", packageName, null))
            uninstallLauncher.launch(intent)
            shouldShowUninstall = false // Reset flag, will be set again if needed
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
                            shouldShowUninstall = true // Start uninstall process
                            showDialog = false // Close dialog
                            ITSAMAN = true // Set the flag to true
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
                        contentScale = ContentScale.Crop
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
        true  // App is still installed
    } catch (e: Exception) {
        false // App has been uninstalled
    }
}