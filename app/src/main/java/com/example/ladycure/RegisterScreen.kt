package com.example.ladycure

import LadyCureTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = " Welcome to LadyCure! ",
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
                Spacer(modifier = Modifier.height(16.dp))

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

                TextButton (
                    onClick = { navController.navigate("login") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text("Already have an account? Login",
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary)
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
