package com.example.ladycure.presentation.booking

import android.content.Context
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
import com.example.ladycure.utility.PdfUploader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectServiceViewModel @Inject constructor(
    private val doctorRepository: DoctorRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    var doctor by mutableStateOf<Doctor?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isUploading by mutableStateOf(false)
        private set

    var uploadProgress by mutableStateOf(0f)
        private set

    var referralId by mutableStateOf<String?>(null)
        private set

    var showUploadSuccessDialog by mutableStateOf(false)
        private set

    var tooLarge by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun loadDoctor(doctorId: String) {
        viewModelScope.launch {
            isLoading = true
            val result = doctorRepository.getDoctorById(doctorId)
            if (result.isSuccess) {
                doctor = result.getOrNull()
            } else {
                error = result.exceptionOrNull()?.message
            }
            isLoading = false
        }
    }

    fun uploadReferral(context: Context, uri: Uri, service: AppointmentType?) {
        viewModelScope.launch {
            if (PdfUploader.isFileTooLarge(context, uri)) {
                tooLarge = true
                return@launch
            }
            isUploading = true
            uploadProgress = 0f
            val result = storageRepository.uploadReferralToFirestore(
                context = context,
                uri = uri,
                service = service
            ) { progress ->
                uploadProgress = progress.progress
            }
            isUploading = false
            if (result.isSuccess) {
                referralId = result.getOrNull()
                showUploadSuccessDialog = true
            } else {
                error = result.exceptionOrNull()?.message ?: "Could not upload PDF"
            }
        }
    }

    fun clearTooLarge() { tooLarge = false }
    fun clearUploadSuccess() { showUploadSuccessDialog = false }
    fun clearError() { error = null }
}
