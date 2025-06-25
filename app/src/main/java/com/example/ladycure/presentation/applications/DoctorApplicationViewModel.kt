package com.example.ladycure.presentation.applications


import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.ApplicationRepository
import com.example.ladycure.data.repository.AuthRepository
import com.example.ladycure.data.repository.StorageRepository
import com.example.ladycure.domain.model.ApplicationStatus
import com.example.ladycure.domain.model.DoctorApplication
import com.example.ladycure.domain.model.Role
import com.example.ladycure.domain.model.Speciality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class DoctorApplicationViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val appRepo: ApplicationRepository = ApplicationRepository(),
    private val storageRepo: StorageRepository = StorageRepository()
) : ViewModel() {
    // Personal Information
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var dateOfBirth by mutableStateOf(LocalDate.of(2000, 1, 1))
    var selectedDate by mutableStateOf(LocalDate.of(2000, 1, 1))

    // Professional Information
    var speciality by mutableStateOf(Speciality.OTHER)
    var licenseNumber by mutableStateOf("")
    var yearsOfExperience by mutableStateOf("")
    var currentWorkplace by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var address by mutableStateOf("")
    var city by mutableStateOf("")

    // Document Uploads
    var licensePhotoUri by mutableStateOf<Uri?>(null)
    var diplomaPhotoUri by mutableStateOf<Uri?>(null)

    // UI State
    var isLoading by mutableStateOf(false)
    var progress by mutableStateOf(0f)
    var progressText by mutableStateOf("0%")
    var currentStep by mutableStateOf(1)
    val totalSteps = 4
    var errorMessage by mutableStateOf<String?>(null)
    var hasSubmitted by mutableStateOf(false)
    var tooLarge by mutableStateOf(false)

    fun submitApplication(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        isLoading = true
        progress = 0f
        progressText = "0%"
        currentStep = 1

        viewModelScope.launch {
            try {
                // 1. Create user account (step 1)
                currentStep = 1
                val authResult = authRepository.register(
                    email = email,
                    password = password,
                    name = firstName,
                    surname = lastName,
                    dateOfBirth = dateOfBirth.toString(),
                    role = Role.DOCTOR_PENDING.value
                )

                if (authResult.isSuccess) {
                    val userId = authResult.getOrNull() ?: return@launch

                    // Update progress (25%)
                    progress = 0.25f
                    progressText = "25%"

                    // 2. Upload license (step 2)
                    currentStep = 2
                    val licensePhotoUrl = licensePhotoUri?.let { uri ->
                        storageRepo.uploadFile(
                            uri = uri,
                            path = "doctor_verification/$userId/license.jpg",
                            onProgress = { uploaded, total ->
                                // Calculate progress for this step (25-50% range)
                                val stepProgress = uploaded.toFloat() / total.toFloat()
                                progress = 0.25f + (0.25f * stepProgress)
                                progressText = "${(progress * 100).toInt()}%"
                            }
                        ).getOrNull()
                    } ?: ""

                    // Update progress (50%)
                    progress = 0.5f
                    progressText = "50%"

                    // 3. Upload diploma (step 3)
                    currentStep = 3
                    val diplomaPhotoUrl = diplomaPhotoUri?.let { uri ->
                        storageRepo.uploadFile(
                            uri = uri,
                            path = "doctor_verification/$userId/diploma.jpg",
                            onProgress = { uploaded, total ->
                                // Calculate progress for this step (50-75% range)
                                val stepProgress = uploaded.toFloat() / total.toFloat()
                                progress = 0.5f + (0.25f * stepProgress)
                                progressText = "${(progress * 100).toInt()}%"
                            }
                        ).getOrNull()
                    } ?: ""

                    // Update progress (75%)
                    progress = 0.75f
                    progressText = "75%"

                    // 4. Submit application (step 4)
                    currentStep = 4
                    val application = DoctorApplication(
                        userId = userId,
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        dateOfBirth = dateOfBirth,
                        licenseNumber = licenseNumber,
                        licensePhotoUrl = licensePhotoUrl,
                        diplomaPhotoUrl = diplomaPhotoUrl,
                        speciality = speciality,
                        yearsOfExperience = yearsOfExperience.toIntOrNull() ?: 0,
                        currentWorkplace = currentWorkplace,
                        phoneNumber = phoneNumber,
                        address = address,
                        city = city,
                        status = ApplicationStatus.PENDING,
                    )
                    appRepo.submitApplication(application)

                    // Complete progress
                    progress = 1f
                    progressText = "100%"

                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }

                    authRepository.signOut()
                } else {
                    isLoading = false
                    errorMessage = "Registration failed: ${authResult.exceptionOrNull()?.message}"
                    withContext(Dispatchers.Main) {
                        onError(errorMessage!!)
                    }
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Error: ${e.message}"
                withContext(Dispatchers.Main) {
                    onError(errorMessage!!)
                }
            }
        }
    }

    fun validateApplication(): Boolean {
        hasSubmitted = true
        return isFormValid()
    }

    fun isFormValid(): Boolean {
        return firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                email.isNotBlank() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                password.isNotBlank() &&
                password.length >= 8 &&
                password.matches(Regex(".*[A-Z].*")) && // One upper case
                password.matches(Regex(".*[0-9].*")) && // One number
                password.matches(Regex(".*[!@#$%^&*].*")) && // One special character
                confirmPassword.isNotBlank() &&
                password == confirmPassword &&
                licenseNumber.isNotBlank() &&
                Regex("[A-Za-z]{2,3}\\d{4,8}").matches(licenseNumber) &&
                yearsOfExperience.isNotBlank() &&
                yearsOfExperience.toIntOrNull() != null &&
                currentWorkplace.isNotBlank() &&
                phoneNumber.isNotBlank() &&
                phoneNumber.length >= 8 &&
                address.isNotBlank() &&
                city.isNotBlank() &&
                licensePhotoUri != null &&
                diplomaPhotoUri != null
    }
}