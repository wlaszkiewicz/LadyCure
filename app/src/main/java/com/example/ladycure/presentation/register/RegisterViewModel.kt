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
import java.util.Calendar

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

    fun updateDateOfBirth(day: Int, month: Int, year: Int) {
        uiState = uiState.copy(
            selectedDay = day,
            selectedMonth = month,
            selectedYear = year
        )
    }

    fun register(navController: NavController) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val result = registerUseCase(
                uiState.email,
                uiState.name,
                uiState.surname,
                uiState.dateOfBirth,
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
}

data class RegisterUiState(
    val email: String = "",
    val name: String = "",
    val surname: String = "",
    val selectedDay: Int = 1,
    val selectedMonth: Int = 1,
    val selectedYear: Int = 2000,
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val dateOfBirth: String
        get() = "$selectedDay/$selectedMonth/$selectedYear"

    fun isValid(): Boolean {
        return email.isNotBlank() &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                name.isNotBlank() &&
                surname.isNotBlank() &&
                password.isNotBlank() &&
                password == confirmPassword &&
                validAge(dateOfBirth)
    }

    private fun validAge(dateOfBirth: String): Boolean {
        val parts = dateOfBirth.split("/")
        val day = parts[0].toInt()
        val month = parts[1].toInt() - 1
        val year = parts[2].toInt()

        val dob = Calendar.getInstance().apply {
            set(year, month, day)
        }
        val today = Calendar.getInstance()

        val age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            return age > 18
        }
        return age >= 18
    }
}