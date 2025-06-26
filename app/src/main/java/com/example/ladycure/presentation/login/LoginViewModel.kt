package com.example.ladycure.presentation.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.ladycure.data.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the login screen, handling user authentication logic.
 *
 * @param authRepository The repository for authentication operations.
 */
class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    /** The email input by the user. */
    var email by mutableStateOf("")

    /** The password input by the user. */
    var password by mutableStateOf("")

    /** Indicates whether a login operation is currently in progress. */
    var isLoading by mutableStateOf(false)

    /** Stores any error message that occurs during the login process. */
    var error by mutableStateOf("")

    /**
     * Initiates the login process.
     *
     * Validates input fields and then attempts to authenticate the user using the [AuthRepository].
     * Updates [isLoading] and [error] states based on the authentication result.
     *
     * @param navController The [NavController] used for navigation after successful login.
     */
    fun login(navController: NavController) {
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

    /**
     * Checks if the email and password input fields are not empty.
     *
     * @return `true` if both email and password are not empty, `false` otherwise.
     */
    fun isValidInput(): Boolean {
        return email.isNotEmpty() && password.isNotEmpty()
    }
}