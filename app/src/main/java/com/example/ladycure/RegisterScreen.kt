package com.example.ladycure

import android.util.Patterns
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ladycure.domain.RegisterUseCase
import com.example.ladycure.presentation.register.RegisterViewModel
import com.example.ladycure.presentation.register.components.RegisterForm
import com.example.ladycure.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

@Composable
fun RegisterScreen(navController: NavController) {
    val viewModel: RegisterViewModel = viewModel(factory = RegisterViewModelFactory())
    val uiState = viewModel.uiState

    Surface {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome to LadyCure", style = MaterialTheme.typography.headlineSmall)
            Text("Please fill in the form to register", style = MaterialTheme.typography.bodyMedium)

            RegisterForm(
                state = uiState,
                onEmailChange = viewModel::updateEmail,
                onNameChange = viewModel::updateName,
                onSurnameChange = viewModel::updateSurname,
                onDaySelected = { viewModel.updateDateOfBirth(it, uiState.selectedMonth, uiState.selectedYear) },
                onMonthSelected = { viewModel.updateDateOfBirth(uiState.selectedDay, it, uiState.selectedYear) },
                onYearSelected = { viewModel.updateDateOfBirth(uiState.selectedDay, uiState.selectedMonth, it) },
                onPasswordChange = viewModel::updatePassword,
                onConfirmPasswordChange = viewModel::updateConfirmPassword,
                onRegisterClick = { viewModel.register(navController) }
            )
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