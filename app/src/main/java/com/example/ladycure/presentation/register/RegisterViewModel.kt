package com.example.ladycure.presentation.register

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.ladycure.domain.RegisterUseCase
import kotlinx.coroutines.launch
import java.time.LocalDate

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {
    // Form state
    var uiState by mutableStateOf(RegisterUiState())
        private set

    fun updateEmail(email: String) {
        uiState = uiState.copy(email = email)
    }

    fun updateName(name: String) {
        uiState = uiState.copy(name = name)
    }

    fun updateSurname(surname: String) {
        uiState = uiState.copy(surname = surname)
    }

    fun updatePassword(password: String) {
        uiState = uiState.copy(password = password)
    }

    fun updateConfirmPassword(confirmPassword: String) {
        uiState = uiState.copy(confirmPassword = confirmPassword)
    }

    fun updateDateOfBirth(date: LocalDate) {
        uiState = uiState.copy(
            selectedDate = date,
        )
    }

    fun register(navController: NavController) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val result = registerUseCase(
                uiState.email,
                uiState.name,
                uiState.surname,
                uiState.selectedDate.toString(),
                uiState.password
            )
            uiState = uiState.copy(isLoading = false)

            if (result.isSuccess) {
                navController.navigate("login")
            } else {
                uiState = uiState.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "Registration failed"
                )
            }
        }
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }
}

data class RegisterUiState(
    val email: String = "",
    val name: String = "",
    val surname: String = "",
    val selectedDate: LocalDate = LocalDate.of(2000, 1, 1),
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {

    fun isValid(): Boolean {
        return email.isNotBlank() &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                name.isNotBlank() &&
                surname.isNotBlank() &&
                password.isNotBlank() &&
                password == confirmPassword &&
                selectedDate.isAfter(LocalDate.now().minusYears(18))
    }


    fun getFirstInvalidField(): String {
        return when {
            email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "email"
            name.isBlank() -> "firstName"
            surname.isBlank() -> "lastName"
         //   selectedDate.isAfter(LocalDate.now().minusYears(18)) -> "dob"
            password.isBlank() || password.length < 8
                    || !password.matches(Regex(".*[A-Z].*"))
                    || !password.matches(Regex(".*[0-9].*"))
                    || !password.matches(Regex(".*[!@#$%^&*].*")) -> "password"
            confirmPassword.isBlank() || password != confirmPassword -> "confirmPassword"
            else -> "valid"
        }
    }


}