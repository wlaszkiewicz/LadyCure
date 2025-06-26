package com.example.ladycure.presentation.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.AdminRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * ViewModel for the Admin Analytics screen.
 *
 * Handles loading and managing analytics data including user growth, patient growth,
 * doctor growth, application statuses, age distributions, and overall stats.
 * Supports switching between different time periods for the data.
 *
 * @property adminRepo Repository providing analytics data from the backend or local source.
 */
class AdminAnalyticsViewModel(
    private val adminRepo: AdminRepository = AdminRepository(),
) : ViewModel() {

    var isLoading by mutableStateOf(true)
        private set

    var userGrowthData by mutableStateOf<List<Pair<String, Int>>>(emptyList())
        private set
    var patientGrowthData by mutableStateOf<List<Pair<String, Int>>>(emptyList())
        private set
    var doctorGrowthData by mutableStateOf<List<Pair<String, Int>>>(emptyList())
        private set
    var usersAgeData by mutableStateOf<List<Pair<String, Int>>>(emptyList())
        private set
    var applicationStats by mutableStateOf<Map<String, Int>>(emptyMap())
        private set
    var totalStats by mutableStateOf<Map<String, Any>>(emptyMap())
        private set

    var selectedTimePeriod by mutableStateOf(TimePeriod.MONTHLY)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        internal set

    init {
        loadAnalyticsData()
    }

    /**
     * Updates the selected time period and reloads analytics data.
     *
     * @param period The new time period to apply for analytics.
     */
    fun updateTimePeriod(period: TimePeriod) {
        selectedTimePeriod = period
        loadAnalyticsData()
    }

    /**
     * Loads all relevant analytics data asynchronously.
     *
     * Uses Kotlin coroutines to concurrently fetch data from the repository.
     * Sets error messages if any fetch fails and updates loading state accordingly.
     */
    fun loadAnalyticsData() {
        viewModelScope.launch {
            isLoading = true
            try {
                val userGrowthDeferred = async { adminRepo.getUserGrowthData(selectedTimePeriod) }
                val patientGrowthDeferred =
                    async { adminRepo.getPatientGrowthData(selectedTimePeriod) }
                val doctorGrowthDeferred =
                    async { adminRepo.getDoctorGrowthData(selectedTimePeriod) }
                val applicationStatsDeferred = async { adminRepo.getApplicationStats() }
                val totalStatsDeferred = async { adminRepo.getAdminStats() }
                val usersAgeDeferred = async { adminRepo.getUsersAgeData() }

                userGrowthData = userGrowthDeferred.await().getOrElse {
                    errorMessage = "Failed to load user growth data: ${it.message}"
                    emptyList()
                }

                patientGrowthData = patientGrowthDeferred.await().getOrElse {
                    errorMessage = "Failed to load patient growth data: ${it.message}"
                    emptyList()
                }

                doctorGrowthData = doctorGrowthDeferred.await().getOrElse {
                    errorMessage = "Failed to load doctor growth data: ${it.message}"
                    emptyList()
                }

                applicationStats = applicationStatsDeferred.await().getOrElse {
                    errorMessage = "Failed to load application stats: ${it.message}"
                    emptyMap()
                }

                totalStats = totalStatsDeferred.await().getOrElse {
                    errorMessage = "Failed to load total stats: ${it.message}"
                    emptyMap()
                }

                usersAgeData = usersAgeDeferred.await().getOrElse {
                    errorMessage = "Failed to load users age data: ${it.message}"
                    emptyList()
                }

            } catch (e: Exception) {
                errorMessage = "An error occurred while loading analytics data: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}

