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

/**
 * ViewModel for the Doctor Application screen.
 * Handles the logic for doctor application submission, including data validation,
 * user registration, file uploads, and application submission to repositories.
 *
 * @param authRepository Repository for authentication operations.
 * @param appRepo Repository for application data operations.
 * @param storageRepo Repository for file storage operations.
 */
class DoctorApplicationViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val appRepo: ApplicationRepository = ApplicationRepository(),
    private val storageRepo: StorageRepository = StorageRepository()
) : ViewModel() {
    /** First name of the applicant. */
    var firstName by mutableStateOf("")
    /** Last name of the applicant. */
    var lastName by mutableStateOf("")
    /** Email of the applicant. */
    var email by mutableStateOf("")
    /** Password for the applicant's account. */
    var password by mutableStateOf("")
    /** Confirmation of the password. */
    var confirmPassword by mutableStateOf("")
    /** Date of birth of the applicant. */
    var dateOfBirth by mutableStateOf(LocalDate.of(2000, 1, 1))
    /** Selected date for date of birth in the UI. */
    var selectedDate by mutableStateOf(LocalDate.of(2000, 1, 1))

    /** Selected medical speciality. */
    var speciality by mutableStateOf(Speciality.OTHER)
    /** Medical license number. */
    var licenseNumber by mutableStateOf("")
    /** Years of professional experience. */
    var yearsOfExperience by mutableStateOf("")
    /** Current workplace or hospital. */
    var currentWorkplace by mutableStateOf("")
    /** Phone number of the applicant. */
    var phoneNumber by mutableStateOf("")
    /** Address of the applicant. */
    var address by mutableStateOf("")
    /** City of residence. */
    var city by mutableStateOf("")

    /** URI for the uploaded license photo. */
    var licensePhotoUri by mutableStateOf<Uri?>(null)
    /** URI for the uploaded diploma photo. */
    var diplomaPhotoUri by mutableStateOf<Uri?>(null)

    /** Indicates if the application is currently loading/submitting. */
    var isLoading by mutableStateOf(false)
    /** Current progress of the application submission (0.0 to 1.0). */
    var progress by mutableStateOf(0f)
    /** Text representation of the progress (e.g., "50%"). */
    var progressText by mutableStateOf("0%")
    /** Current step in the multi-step submission process. */
    var currentStep by mutableStateOf(1)
    /** Total number of steps in the submission process. */
    val totalSteps = 4
    /** Error message to be displayed, if any. */
    var errorMessage by mutableStateOf<String?>(null)
    /** Indicates if the form has been submitted at least once. */
    var hasSubmitted by mutableStateOf(false)
    /** Indicates if an uploaded file is too large. */
    var tooLarge by mutableStateOf(false)

    /**
     * Submits the doctor application.
     * This function orchestrates the steps of creating a user account, uploading required documents,
     * and submitting the application details to the backend.
     *
     * @param onSuccess Callback function to be invoked upon successful application submission.
     * @param onError Callback function to be invoked if an error occurs during submission, with an error message.
     */
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

                    progress = 0.25f
                    progressText = "25%"

                    currentStep = 2
                    val licensePhotoUrl = licensePhotoUri?.let { uri ->
                        storageRepo.uploadFile(
                            uri = uri,
                            path = "doctor_verification/$userId/license.jpg",
                            onProgress = { uploaded, total ->
                                val stepProgress = uploaded.toFloat() / total.toFloat()
                                progress = 0.25f + (0.25f * stepProgress)
                                progressText = "${(progress * 100).toInt()}%"
                            }
                        ).getOrNull()
                    } ?: ""

                    progress = 0.5f
                    progressText = "50%"

                    currentStep = 3
                    val diplomaPhotoUrl = diplomaPhotoUri?.let { uri ->
                        storageRepo.uploadFile(
                            uri = uri,
                            path = "doctor_verification/$userId/diploma.jpg",
                            onProgress = { uploaded, total ->
                                val stepProgress = uploaded.toFloat() / total.toFloat()
                                progress = 0.5f + (0.25f * stepProgress)
                                progressText = "${(progress * 100).toInt()}%"
                            }
                        ).getOrNull()
                    } ?: ""

                    progress = 0.75f
                    progressText = "75%"

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

    /**
     * Validates the entire application form.
     * Sets [hasSubmitted] to true to trigger validation messages in the UI.
     *
     * @return True if the form is valid, false otherwise.
     */
    fun validateApplication(): Boolean {
        hasSubmitted = true
        return isFormValid()
    }

    /**
     * Checks if all fields in the application form are valid.
     *
     * @return True if all fields meet the validation criteria, false otherwise.
     */
    fun isFormValid(): Boolean {
        return firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                email.isNotBlank() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                password.isNotBlank() &&
                password.length >= 8 &&
                password.matches(Regex(".*[A-Z].*")) &&
                password.matches(Regex(".*[0-9].*")) &&
                password.matches(Regex(".*[!@#$%^&*].*")) &&
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