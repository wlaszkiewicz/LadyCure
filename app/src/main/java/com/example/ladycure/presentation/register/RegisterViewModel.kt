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

/**
 * ViewModel for the registration screen.
 *
 * Handles user input, validates data, and interacts with the [RegisterUseCase] to perform registration.
 * @param registerUseCase The use case for handling registration logic.
 */
class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {
    /**
     * The mutable state of the registration UI.
     *
     * This state holds all the data displayed on the registration screen and its current status.
     */
    var uiState by mutableStateOf(RegisterUiState())
        private set

    /**
     * Updates the email in the [RegisterUiState].
     * @param email The new email address.
     */
    fun updateEmail(email: String) {
        uiState = uiState.copy(email = email)
    }

    /**
     * Updates the name in the [RegisterUiState].
     * @param name The new name.
     */
    fun updateName(name: String) {
        uiState = uiState.copy(name = name)
    }

    /**
     * Updates the surname in the [RegisterUiState].
     * @param surname The new surname.
     */
    fun updateSurname(surname: String) {
        uiState = uiState.copy(surname = surname)
    }

    /**
     * Updates the password in the [RegisterUiState].
     * @param password The new password.
     */
    fun updatePassword(password: String) {
        uiState = uiState.copy(password = password)
    }

    /**
     * Updates the confirm password in the [RegisterUiState].
     * @param confirmPassword The new confirm password.
     */
    fun updateConfirmPassword(confirmPassword: String) {
        uiState = uiState.copy(confirmPassword = confirmPassword)
    }

    /**
     * Updates the selected date of birth in the [RegisterUiState].
     * @param date The new date of birth.
     */
    fun updateDateOfBirth(date: LocalDate) {
        uiState = uiState.copy(
            selectedDate = date,
        )
    }

    /**
     * Initiates the registration process.
     *
     * Performs validation and calls the [RegisterUseCase] to register the user.
     * Navigates to the "login" screen on success or sets an error message on failure.
     * @param navController The NavController used for navigation.
     */
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

    /**
     * Clears any error messages in the [RegisterUiState].
     */
    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }
}

/**
 * Represents the UI state for the registration screen.
 *
 * @property email The user's email address.
 * @property name The user's first name.
 * @property surname The user's surname.
 * @property selectedDate The user's selected date of birth.
 * @property password The user's password.
 * @property confirmPassword The confirmed password.
 * @property isLoading Indicates if a registration operation is in progress.
 * @property isSuccess Indicates if the registration was successful.
 * @property errorMessage An error message to display, if any.
 */
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
    /**
     * Checks if all registration fields are valid.
     * @return true if all fields are valid, false otherwise.
     */
    fun isValid(): Boolean {
        return email.isNotBlank() &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                name.isNotBlank() &&
                surname.isNotBlank() &&
                password.isNotBlank() &&
                password.length >= 8 &&
                password.matches(Regex(".*[A-Z].*")) &&
                password.matches(Regex(".*[0-9].*")) &&
                password.matches(Regex(".*[!@#$%^&*].*")) &&
                confirmPassword.isNotBlank() &&
                password == confirmPassword &&
                !selectedDate.isAfter(LocalDate.now().minusYears(18))
    }

    /**
     * Returns the name of the first invalid field.
     *
     * This is useful for guiding the user to correct their input.
     * @return A string representing the first invalid field, or "valid" if all fields are valid.
     */
    fun getFirstInvalidField(): String {
        return when {
            email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "email"
            name.isBlank() -> "firstName"
            surname.isBlank() -> "lastName"
            password.isBlank() || password.length < 8
                    || !password.matches(Regex(".*[A-Z].*"))
                    || !password.matches(Regex(".*[0-9].*"))
                    || !password.matches(Regex(".*[!@#$%^&*].*")) -> "password"

            confirmPassword.isBlank() || password != confirmPassword -> "confirmPassword"
            else -> "valid"
        }
    }
}