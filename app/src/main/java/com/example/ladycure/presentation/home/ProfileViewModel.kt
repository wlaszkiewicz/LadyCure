package com.example.ladycure.presentation.home

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.AuthRepository
import com.example.ladycure.data.repository.DoctorRepository
import com.example.ladycure.data.repository.UserRepository
import com.example.ladycure.utility.ImageUploader
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val doctorRepository: DoctorRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    var userData by mutableStateOf<Map<String, Any>?>(null)
        private set

    var errorMessage by mutableStateOf("")
        private set

    var currentImageUrl by mutableStateOf("")
        private set

    init {
        loadUserData()
    }

    fun loadUserData() {
        viewModelScope.launch {
            val userResult = userRepository.getCurrentUserData()
            if (userResult.isFailure) {
                errorMessage = "Failed to load user data: ${userResult.exceptionOrNull()?.message}"
                return@launch
            }
            val user = userResult.getOrNull() ?: return@launch
            val role = user["role"] as? String
            if (role == "doctor") {
                val doctorResult = doctorRepository.getCurrentDoctorData()
                if (doctorResult.isFailure) {
                    errorMessage = "Failed to load doctor data: ${doctorResult.exceptionOrNull()?.message}"
                } else {
                    userData = doctorResult.getOrNull()
                }
            } else {
                userData = user
            }
            currentImageUrl = userData?.get("profilePictureUrl") as? String ?: ""
        }
    }

    fun uploadProfilePicture(uri: Uri) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val imageUploader = ImageUploader(appContext)
            imageUploader.uploadImage(uri, userId).fold(
                onSuccess = { downloadUrl ->
                    userRepository.updateProfilePicture(downloadUrl)
                    currentImageUrl = downloadUrl
                    val role = userData?.get("role") as? String
                    userData = if (role == "doctor") {
                        doctorRepository.getCurrentDoctorData().getOrNull()
                    } else {
                        userRepository.getCurrentUserData().getOrNull()
                    }
                    errorMessage = ""
                },
                onFailure = { e ->
                    errorMessage = "Failed to update profile picture: ${e.message}"
                }
            )
        }
    }

    fun updateUserData(updatedData: Map<String, String>) {
        viewModelScope.launch {
            val role = userData?.get("role") as? String
            if (role == "doctor") {
                userData = doctorRepository.getCurrentDoctorData().getOrNull() ?: emptyMap()
            } else {
                val result = userRepository.updateUserData(updatedData)
                if (result.isSuccess) {
                    userData = result.getOrNull() ?: emptyMap()
                } else {
                    errorMessage = "Failed to update user data: ${result.exceptionOrNull()?.message}"
                }
            }
        }
    }

    fun saveDoctorProfile(updatedData: Map<String, Any>) {
        viewModelScope.launch {
            doctorRepository.updateDoctorProfile(updatedData)
            userData = doctorRepository.getCurrentDoctorData().getOrNull()
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun clearError() { errorMessage = "" }
}
