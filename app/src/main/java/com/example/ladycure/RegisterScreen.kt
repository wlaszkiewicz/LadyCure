package com.example.ladycure

import LadyCureTheme
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ladycure.domain.RegisterUseCase
import com.example.ladycure.presentation.register.RegisterViewModel
import com.example.ladycure.presentation.register.components.RegisterForm
import com.example.ladycure.repository.AuthRepository
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import DefaultOnPrimary
import DefaultPrimary

@Composable

fun RegisterScreen(navController: NavController) {
    val viewModel: RegisterViewModel = viewModel(factory = RegisterViewModelFactory())
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Show snackbar when error occurs
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Short
                )
                // Clear error after showing
                viewModel.clearError()
            }
        }
    }

    LadyCureTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.logo), // Add your logo
//                        contentDescription = "LadyCure Logo",
//                        tint = MaterialTheme.colorScheme.primary,
//                        modifier = Modifier.size(80.dp)
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Create Your Account",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Join our all female community made just for you",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:ladycure_admin@gmail.com".toUri()
                        }
                        navController.context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Are you a doctor? Contact us",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.5f),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Registration Form
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White,
                        contentColor = DefaultOnPrimary
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RegisterForm(
                            state = uiState,
                            onEmailChange = viewModel::updateEmail,
                            onNameChange = viewModel::updateName,
                            onSurnameChange = viewModel::updateSurname,
                            onDateSelected = { viewModel.updateDateOfBirth(it) },
                            onPasswordChange = viewModel::updatePassword,
                            onConfirmPasswordChange = viewModel::updateConfirmPassword,
                            onRegisterClick = { viewModel.register(navController) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer Links
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextButton(
                        onClick = { navController.navigate("login") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Already have an account? Sign in",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DefaultPrimary,
                        )
                    }

                }
            }
        }
    }
}

class RegisterViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(
                registerUseCase = RegisterUseCase(
                    authRepository = AuthRepository()
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}