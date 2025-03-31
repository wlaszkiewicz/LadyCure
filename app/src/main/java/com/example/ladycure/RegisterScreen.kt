package com.example.ladycure

import LadyCureTheme
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ladycure.domain.RegisterUseCase
import com.example.ladycure.presentation.register.RegisterViewModel
import com.example.ladycure.presentation.register.components.RegisterForm
import com.example.ladycure.repository.AuthRepository
import androidx.core.net.toUri
@Composable
fun RegisterScreen(navController: NavController) {
    val viewModel: RegisterViewModel = viewModel(factory = RegisterViewModelFactory())
    val uiState = viewModel.uiState
    LadyCureTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),  // Add scrolling if content is long

                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Welcome to LadyCure!",
                        fontSize = 24.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Fill in the details below to get started! ✨",
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }


                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

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
                            "Are you a doctor? Contact us here.",
                            fontSize = 14.sp,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                RegisterForm(
                    state = uiState,
                    onEmailChange = viewModel::updateEmail,
                    onNameChange = viewModel::updateName,
                    onSurnameChange = viewModel::updateSurname,
                    onDaySelected = {
                        viewModel.updateDateOfBirth(
                            it,
                            uiState.selectedMonth,
                            uiState.selectedYear
                        )
                    },
                    onMonthSelected = {
                        viewModel.updateDateOfBirth(
                            uiState.selectedDay,
                            it,
                            uiState.selectedYear
                        )
                    },
                    onYearSelected = {
                        viewModel.updateDateOfBirth(
                            uiState.selectedDay,
                            uiState.selectedMonth,
                            it
                        )
                    },
                    onPasswordChange = viewModel::updatePassword,
                    onConfirmPasswordChange = viewModel::updateConfirmPassword,
                    onRegisterClick = { viewModel.register(navController) }
                )

                TextButton(
                    onClick = { navController.navigate("login") },

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        "Already have an account?\nLogin",
                        fontSize = 14.sp,
                        style = TextStyle(
                            fontSize = 16.sp
                        ),
                        textAlign = TextAlign.Center,

                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))  // Bottom padding
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