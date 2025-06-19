package com.example.ladycure.presentation.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.ladycure.data.repository.AuthRepository
import com.example.ladycure.utility.SnackbarController
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf("")

    fun login(navController: NavController, snackbarHostState: SnackbarController) {
        if (!isValidInput()) {
            error = "Please fill all fields"
            return
        }

        isLoading = true
        viewModelScope.launch {
            authRepository.authenticate(
                email = email,
                password = password,
                navController = navController,
                onSuccess = {
                    error = "Login Successful"
                    isLoading = false
                },
                onFailure = { exception ->
                    error = exception.message ?: "Authentication failed"
                    isLoading = false
                }
            )
        }
    }

    fun isValidInput(): Boolean {
        return email.isNotEmpty() && password.isNotEmpty()
    }
}