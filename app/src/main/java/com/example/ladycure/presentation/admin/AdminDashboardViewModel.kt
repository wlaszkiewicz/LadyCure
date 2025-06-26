package com.example.ladycure.presentation.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.ladycure.data.repository.AdminRepository
import com.example.ladycure.data.repository.ApplicationRepository
import com.example.ladycure.data.repository.AuthRepository
import com.example.ladycure.domain.model.ApplicationStatus
import com.example.ladycure.domain.model.DoctorApplication
import kotlinx.coroutines.launch

/**
 * ViewModel for the Admin Dashboard screen.
 *
 * Handles loading and refreshing of doctor applications and admin statistics,
 * manages UI state such as dialogs and loading indicators,
 * and coordinates actions like logout and application status updates.
 *
 * @property applicationRepo Repository for fetching and updating doctor applications.
 * @property adminRepo Repository for fetching admin-related statistics.
 * @property authRepo Repository for handling authentication (e.g., logout).
 */
class AdminDashboardViewModel(
    private val applicationRepo: ApplicationRepository = ApplicationRepository(),
    private val adminRepo: AdminRepository = AdminRepository(),
    private val authRepo: AuthRepository = AuthRepository(),
) : ViewModel() {
    var showLogoutDialog by mutableStateOf(false)
        private set

    var isLoadingApplications by mutableStateOf(true)
        private set

    val doctorApplications = mutableStateListOf<DoctorApplication>()

    var showApplicationsDialog by mutableStateOf(false)
        private set

    var selectedApplication by mutableStateOf<DoctorApplication?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        internal set

    var stats by mutableStateOf(
        mapOf(
            "totalUsers" to 0,
            "activeDoctors" to 0,
            "pendingApplications" to 0
        )
    )
        private set

    init {
        loadInitialData()
    }

    /** Loads initial data including stats and doctor applications. */
    private fun loadInitialData() {
        viewModelScope.launch {
            loadStats()
            loadApplications()
        }
    }

    /** Loads admin statistics from the repository and updates state. */
    private suspend fun loadStats() {
        val result = adminRepo.getAdminStats()
        if (result.isSuccess) {
            stats = (result.getOrNull() ?: mapOf(
                "totalUsers" to 0,
                "activeDoctors" to 0,
                "pendingApplications" to 0
            )) as Map<String, Int>
        } else {
            errorMessage = "Failed to load stats: ${result.exceptionOrNull()?.message}"
        }
    }

    /** Loads initial data including stats and doctor applications. */
    private suspend fun loadApplications() {
        isLoadingApplications = true
        val result = applicationRepo.getAllApplications()
        if (result.isSuccess) {
            doctorApplications.clear()
            doctorApplications.addAll(result.getOrNull() as List<DoctorApplication>)
            isLoadingApplications = false
        } else {
            errorMessage = "Failed to load applications: ${result.exceptionOrNull()?.message}"
            isLoadingApplications = false
        }
    }

    fun refreshApplications() {
        viewModelScope.launch {
            isLoadingApplications = true
            val result = applicationRepo.getAllApplications()
            if (result.isSuccess) {
                doctorApplications.clear()
                doctorApplications.addAll(result.getOrNull() as List<DoctorApplication>)
                isLoadingApplications = false
            } else {
                errorMessage =
                    "Failed to refresh applications: ${result.exceptionOrNull()?.message}"
                isLoadingApplications = false
            }
        }
    }

    fun showLogoutDialog() {
        showLogoutDialog = true
    }

    fun dismissLogoutDialog() {
        showLogoutDialog = false
    }

    fun showApplicationDetails(application: DoctorApplication) {
        selectedApplication = application
        showApplicationsDialog = true
    }

    fun dismissApplicationDetails() {
        showApplicationsDialog = false
    }

    fun logout(navController: NavController) {
        viewModelScope.launch {
            authRepo.signOut()
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    fun updateApplicationStatus(
        newStatus: ApplicationStatus,
        comment: String?,
        justComment: Boolean?
    ) {
        selectedApplication?.let { application ->
            viewModelScope.launch {
                val result = applicationRepo.updateApplicationStatus(
                    application.userId,
                    newStatus.displayName,
                    comment ?: application.reviewNotes ?: ""
                )
                if (result.isSuccess) {
                    if (justComment == true) {
                        errorMessage = "Comment updated successfully."
                    } else {
                        errorMessage = "Application status updated successfully."
                        showApplicationsDialog = false
                    }
                    refreshApplications()
                } else {
                    errorMessage =
                        "Failed to update application status: ${result.exceptionOrNull()?.message}"
                }
            }
        }
    }

    fun approveApplication() {
        selectedApplication?.let { application ->
            viewModelScope.launch {
                val result = applicationRepo.approveApplication(application.userId)
                if (result.isSuccess) {
                    showApplicationsDialog = false
                    refreshApplications()
                } else {
                    errorMessage =
                        "Failed to approve application: ${result.exceptionOrNull()?.message}"
                }
            }
        }
    }
}