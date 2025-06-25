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

class DoctorPendingViewModel(
    private val applicationRepo: ApplicationRepository = ApplicationRepository(),
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _applicationData = MutableStateFlow<DoctorApplication?>(null)
    val applicationData: StateFlow<DoctorApplication?> = _applicationData.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _showFullApplicationDialog = MutableStateFlow(false)
    val showFullApplicationDialog: StateFlow<Boolean> = _showFullApplicationDialog.asStateFlow()

    private val _showLogoutConfirmation = MutableStateFlow(false)
    val showLogoutConfirmation: StateFlow<Boolean> = _showLogoutConfirmation.asStateFlow()

    init {
        loadApplicationData()
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepo.signOut()
            } catch (e: Exception) {
                _error.value = e.message ?: "Logout failed"
            }
        }
    }

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

    fun showDialog() {
        _showFullApplicationDialog.value = true
    }

    fun showLogoutConfirmation() {
        _showLogoutConfirmation.value = true
    }

    fun dismissLogoutConfirmation() {
        _showLogoutConfirmation.value = false
    }

    fun dismissDialog() {
        _showFullApplicationDialog.value = false
    }

    fun clearError() {
        _error.value = null
    }

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