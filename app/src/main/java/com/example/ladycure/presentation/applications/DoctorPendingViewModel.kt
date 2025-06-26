package com.example.ladycure.presentation.applications

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.ApplicationRepository
import com.example.ladycure.data.repository.AuthRepository
import com.example.ladycure.domain.model.DoctorApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Doctor Pending Application screen.
 * Handles fetching doctor application data, logout, and file downloads.
 * @param applicationRepo The repository for application-related operations.
 * @param authRepo The repository for authentication-related operations.
 */
class DoctorPendingViewModel(
    private val applicationRepo: ApplicationRepository = ApplicationRepository(),
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {
    /**
     * Holds the doctor application data.
     */
    private val _applicationData = MutableStateFlow<DoctorApplication?>(null)

    /**
     * Publicly exposed [StateFlow] for observing doctor application data.
     */
    val applicationData: StateFlow<DoctorApplication?> = _applicationData.asStateFlow()

    /**
     * Holds error messages.
     */
    private val _error = MutableStateFlow<String?>(null)

    /**
     * Publicly exposed [StateFlow] for observing error messages.
     */
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Controls the visibility of the full application dialog.
     */
    private val _showFullApplicationDialog = MutableStateFlow(false)

    /**
     * Publicly exposed [StateFlow] for observing the visibility of the full application dialog.
     */
    val showFullApplicationDialog: StateFlow<Boolean> = _showFullApplicationDialog.asStateFlow()

    /**
     * Controls the visibility of the logout confirmation dialog.
     */
    private val _showLogoutConfirmation = MutableStateFlow(false)

    /**
     * Publicly exposed [StateFlow] for observing the visibility of the logout confirmation dialog.
     */
    val showLogoutConfirmation: StateFlow<Boolean> = _showLogoutConfirmation.asStateFlow()

    init {
        loadApplicationData()
    }

    /**
     * Initiates the logout process.
     */
    fun logout() {
        viewModelScope.launch {
            try {
                authRepo.signOut()
            } catch (e: Exception) {
                _error.value = e.message ?: "Logout failed"
            }
        }
    }

    /**
     * Loads the doctor application data from the repository.
     */
    fun loadApplicationData() {
        viewModelScope.launch {
            val result = applicationRepo.getDoctorApplication()
            if (result.isSuccess) {
                _applicationData.value = result.getOrNull()
            } else {
                _error.value =
                    result.exceptionOrNull()?.message ?: "Failed to load application data"
            }
        }
    }

    /**
     * Shows the full application dialog.
     */
    fun showDialog() {
        _showFullApplicationDialog.value = true
    }

    /**
     * Shows the logout confirmation dialog.
     */
    fun showLogoutConfirmation() {
        _showLogoutConfirmation.value = true
    }

    /**
     * Dismisses the logout confirmation dialog.
     */
    fun dismissLogoutConfirmation() {
        _showLogoutConfirmation.value = false
    }

    /**
     * Dismisses the full application dialog.
     */
    fun dismissDialog() {
        _showFullApplicationDialog.value = false
    }

    /**
     * Clears any current error messages.
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Downloads a file using the DownloadManager.
     * @param context The application context.
     * @param url The URL of the file to download.
     * @param fileName The name to save the file as.
     * @param title The title for the download notification.
     * @param description The description for the download notification.
     */
    fun downloadFile(
        context: Context,
        url: String,
        fileName: String,
        title: String,
        description: String
    ) {
        val request = DownloadManager.Request(url.toUri())
            .apply {
                setTitle(title)
                setDescription(description)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName
                )
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
    }
}