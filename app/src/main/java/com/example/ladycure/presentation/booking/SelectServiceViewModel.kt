package com.example.ladycure.presentation.booking

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.DoctorRepository
import com.example.ladycure.data.repository.StorageRepository
import com.example.ladycure.domain.model.AppointmentType
import com.example.ladycure.domain.model.Doctor
import com.example.ladycure.domain.model.Speciality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectServiceViewModel(
    private val doctorRepo: DoctorRepository = DoctorRepository(),
    private val referralRepo: StorageRepository = StorageRepository()
) : ViewModel() {

    // State variables
    var doctor by mutableStateOf<Doctor?>(null)
        private set
    var speciality by mutableStateOf<Speciality?>(null)
        internal set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var selectedService by mutableStateOf<AppointmentType?>(null)
        private set

    // Referral upload state
    var isUploading by mutableStateOf(false)
        private set
    var showUploadSuccessDialog by mutableStateOf(false)
        private set
    var uploadProgress by mutableStateOf(0f)
        private set
    var referralId by mutableStateOf<String?>(null)
        private set

    // UI state
    var showReferralDialog by mutableStateOf(false)
        private set

    fun loadDoctor(doctorId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = doctorRepo.getDoctorById(doctorId)
                if (result.isSuccess) {
                    doctor = result.getOrNull()
                    speciality = doctor?.speciality
                } else {
                    errorMessage = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                errorMessage = "Error loading doctor: ${e.message}"
            }
        }
    }

    fun selectService(service: AppointmentType) {
        selectedService = service
    }

    fun showReferralRequiredDialog(show: Boolean) {
        showReferralDialog = show
    }

    fun uploadReferral(uri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Main) {
                    isUploading = true
                    uploadProgress = 0f
                }

                val result = withContext(Dispatchers.IO) {
                    referralRepo.uploadReferralToFirestore(
                        uri,
                        selectedService
                    ) { progress ->
                        uploadProgress = progress.progress
                    }
                }

                withContext(Dispatchers.Main) {
                    isUploading = false
                    if (result.isSuccess) {
                        referralId = result.getOrNull()
                        showUploadSuccessDialog = true
                        onSuccess()
                    } else {
                        onError(result.exceptionOrNull()?.message ?: "Could not upload PDF")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isUploading = false
                    onError(e.message ?: "Upload failed")
                }
            }
        }
    }

    fun dismissUploadSuccessDialog() {
        showUploadSuccessDialog = false
    }

    fun clearError() {
        errorMessage = null
    }

    // Computed property
    val services: List<AppointmentType>
        get() = speciality?.let {
            AppointmentType.entries.filter { type ->
                type.speciality.equals(it.displayName, ignoreCase = true)
            }
        } ?: emptyList()
}