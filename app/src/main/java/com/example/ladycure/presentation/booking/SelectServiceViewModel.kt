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

/**
 * ViewModel for the Select Service screen, handling business logic and UI state.
 *
 * @param doctorRepo The repository for doctor-related operations.
 * @param referralRepo The repository for referral storage operations.
 */
class SelectServiceViewModel(
    private val doctorRepo: DoctorRepository = DoctorRepository(),
    private val referralRepo: StorageRepository = StorageRepository()
) : ViewModel() {

    /**
     * The currently selected doctor.
     */
    var doctor by mutableStateOf<Doctor?>(null)
        private set

    /**
     * The speciality of the selected doctor.
     */
    var speciality by mutableStateOf<Speciality?>(null)
        internal set

    /**
     * Error message to be displayed to the user.
     */
    var errorMessage by mutableStateOf<String?>(null)
        private set

    /**
     * The currently selected appointment type.
     */
    var selectedService by mutableStateOf<AppointmentType?>(null)
        private set

    /**
     * Indicates if a referral is currently being uploaded.
     */
    var isUploading by mutableStateOf(false)
        private set

    /**
     * Controls the visibility of the upload success dialog.
     */
    var showUploadSuccessDialog by mutableStateOf(false)
        private set

    /**
     * The progress of the referral upload, from 0f to 1f.
     */
    var uploadProgress by mutableStateOf(0f)
        private set

    /**
     * The ID of the uploaded referral document.
     */
    var referralId by mutableStateOf<String?>(null)
        private set

    /**
     * Controls the visibility of the referral required dialog.
     */
    var showReferralDialog by mutableStateOf(false)
        private set

    /**
     * Loads doctor details based on the provided doctor ID.
     *
     * @param doctorId The ID of the doctor to load.
     */
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

    /**
     * Selects an appointment type.
     *
     * @param service The [AppointmentType] to be selected.
     */
    fun selectService(service: AppointmentType) {
        selectedService = service
    }

    /**
     * Sets the visibility of the referral required dialog.
     *
     * @param show True to show the dialog, false to hide it.
     */
    fun showReferralRequiredDialog(show: Boolean) {
        showReferralDialog = show
    }

    /**
     * Uploads a referral document to Firestore.
     *
     * @param uri The URI of the referral document.
     * @param onSuccess A callback to be invoked upon successful upload.
     * @param onError A callback to be invoked if the upload fails, providing an error message.
     */
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

    /**
     * Dismisses the upload success dialog.
     */
    fun dismissUploadSuccessDialog() {
        showUploadSuccessDialog = false
    }

    /**
     * Clears any current error message.
     */
    fun clearError() {
        errorMessage = null
    }

    /**
     * A computed property that returns a list of [AppointmentType]s available for the selected speciality.
     * Returns an empty list if no speciality is selected.
     */
    val services: List<AppointmentType>
        get() = speciality?.let {
            AppointmentType.entries.filter { type ->
                type.speciality.equals(it.displayName, ignoreCase = true)
            }
        } ?: emptyList()
}