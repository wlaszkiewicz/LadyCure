package com.example.ladycure.screens

import BabyBlue
import DefaultOnPrimary
import DefaultPrimary
import LadyCureTheme
import YellowOrange
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.ladycure.R
import com.example.ladycure.repository.AuthRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.material3.TextField
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ladycure.presentation.welcome.WelcomeViewModel
import com.example.ladycure.repository.UserRepository
import java.util.concurrent.Executor

@Composable
fun WelcomeScreen(navController: NavController) {
    val context = LocalContext.current
    val authRepo = AuthRepository()
    val userRepo = UserRepository()
    val viewModel = viewModel { WelcomeViewModel(authRepo,userRepo) }

    // Initialize biometric authentication
    LaunchedEffect(Unit) {
        viewModel.initializeBiometric(context)
        viewModel.setupAuthListener()
    }

    // Handle successful authentication
    LaunchedEffect(viewModel.authenticationSuccess) {
        if (viewModel.authenticationSuccess && viewModel.currentUser != null) {
            // Navigate based on role
            when (viewModel.userRole) {
                "admin" -> navController.navigate("admin") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
                "doctor" -> navController.navigate("doctor_main") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
                else -> navController.navigate("home") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            viewModel.resetAuthState()
        }
    }

    LoadingScreen(isLoading = viewModel.isLoading)

    if (!viewModel.isLoading) {
        if (viewModel.currentUser != null) {
            LoggedInWelcomeScreen(
                user = viewModel.currentUser!!,
                onContinue = { viewModel.authenticateWithBiometrics(context) },
                onUseDifferentAccount = { Firebase.auth.signOut() }
            )
        } else {
            // Navigate to login if no user
            LaunchedEffect(Unit) {
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }
    }

    if (viewModel.showBiometricError) {
        AlertDialog(
            onDismissRequest = { viewModel.showBiometricError = false },
            title = { Text("Authentication Failed") },
            text = { Text("Could not authenticate with biometrics. Please try again or use password.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.showBiometricError = false
                        viewModel.showPasswordDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DefaultPrimary)
                ) {
                    Text("Use Password")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.showBiometricError = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (viewModel.showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showPasswordDialog = false },
            title = { Text("Enter Password") },
            text = {
                Column {
                    Text("Please enter your password to continue:")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = viewModel.password,
                        onValueChange = { viewModel.password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.authenticateWithPassword(navController, context) },
                    colors = ButtonDefaults.buttonColors(containerColor = DefaultPrimary)
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.showPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
@Composable
fun LoggedInWelcomeScreen(
    user: FirebaseUser,
    onContinue: () -> Unit,
    onUseDifferentAccount: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.icon),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 24.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "Welcome back!",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Logged in as ${user.email?.takeWhile { it != '@' } ?: "User"}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DefaultPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Continue",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continue with this account")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onUseDifferentAccount,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Different account",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Use different account")
            }
        }
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

