package com.example.ladycure.presentation.doctor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.DoctorRepository
import com.example.ladycure.presentation.admin.TimePeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DoctorEarningsViewModel @Inject constructor(
    private val doctorRepository: DoctorRepository
) : ViewModel() {

    var isLoading by mutableStateOf(true)
        private set

    var earningsData by mutableStateOf<List<Pair<String, Double>>>(emptyList())
        private set

    var earningsByType by mutableStateOf<Map<String, Double>>(emptyMap())
        private set

    var totalEarnings by mutableStateOf(0.0)
        private set

    var totalAppointments by mutableStateOf(0)
        private set

    var thisMonthEarnings by mutableStateOf(0.0)
        private set

    var mostPopularType by mutableStateOf<Pair<String, Int>?>(null)
        private set

    var selectedTimePeriod by mutableStateOf(TimePeriod.MONTHLY)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    init {
        loadEarnings(TimePeriod.MONTHLY)
    }

    fun selectTimePeriod(period: TimePeriod) {
        selectedTimePeriod = period
        loadEarnings(period)
    }

    fun clearError() {
        error = null
    }

    private fun loadEarnings(period: TimePeriod) {
        viewModelScope.launch {
            isLoading = true
            try {
                val earningsDeferred = async { doctorRepository.getEarningsData(period) }
                val earningsByTypeDeferred = async { doctorRepository.getEarningsByAppointmentType() }
                val statsDeferred = async { doctorRepository.getEarningsStats() }
                val popularTypeDeferred = async { doctorRepository.getMostPopularAppointmentType() }

                earningsData = earningsDeferred.await().getOrElse { emptyList() }
                earningsByType = earningsByTypeDeferred.await().getOrElse { emptyMap() }

                statsDeferred.await().getOrNull()?.let { stats ->
                    totalEarnings = stats["totalEarnings"] as? Double ?: 0.0
                    totalAppointments = stats["totalAppointments"] as? Int ?: 0
                    thisMonthEarnings = stats["thisMonthEarnings"] as? Double ?: 0.0
                }

                mostPopularType = popularTypeDeferred.await().getOrNull()
            } catch (e: Exception) {
                error = "Failed to load earnings: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
