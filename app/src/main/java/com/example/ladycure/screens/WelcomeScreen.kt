package com.example.ladycure.screens

import DefaultPrimary
import LadyCureTheme
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ladycure.R
import com.example.ladycure.repository.AuthRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun WelcomeScreen(navController: NavController) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var isLoggedIn by remember { mutableStateOf(false) }
    val authRepo = AuthRepository()

    LaunchedEffect(Unit) {
        // Check auth state asynchronously
        Firebase.auth.addAuthStateListener { auth ->
            isLoggedIn = auth.currentUser != null
            isLoading = false
        }
    }

    LoadingScreen(isLoading = isLoading)

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            val result = try {
                authRepo.getUserRole()
            } catch (e: Exception) {
                Result.failure(e)
            }

            when {
                result.isSuccess -> {
                    val role = result.getOrNull()
                    when (role) {
                        "admin" -> navController.navigate("admin") { popUpTo(navController.graph.startDestinationId) { inclusive = true } }
                        "doctor" -> navController.navigate("doctor_main") { popUpTo(navController.graph.startDestinationId) { inclusive = true } }
                        else -> navController.navigate("home") { popUpTo(navController.graph.startDestinationId) { inclusive = true } }
                    }
                }
                else -> {
                    Toast.makeText(context, result.exceptionOrNull()?.message ?: "Error fetching user role", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Only show the gender selection/login screen if user is not logged in
    if (!isLoading && !isLoggedIn) {
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
            shouldShowUninstall = false
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
                    text = "Welcome to LadyCure!",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    fontSize = 26.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.akczuali_kapi),
                            contentDescription = "Welcome illustration for women",
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .zIndex(1f)
                                .height(170.dp)
                                .offset(y = (0).dp),
                            contentScale = ContentScale.Fit
                        )

                        // White speech bubble with text
                        Card(
                            modifier = Modifier
                                .width(280.dp)
                                .padding(top = 170.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Text(
                                text = " Our app is designed primarily for women's health needs. " +
                                        "However, if you still find our services helpful, you're more than welcome to continue using the app.",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Button(
                        onClick = { showDialog = false },
                        modifier = Modifier.width(200.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DefaultPrimary
                        )
                    ) {
                        Text("Continue to App")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            shouldShowUninstall = true
                            showDialog = false
                        },
                        modifier = Modifier.width(200.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DefaultPrimary
                        )
                    ) {
                        Text("Not for me")
                    }
                }
            },
            confirmButton = {},
            modifier = Modifier.wrapContentSize()
        )
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