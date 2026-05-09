package com.example.ladycure.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.PeriodTrackerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PeriodTrackerViewModel @Inject constructor(
    private val periodTrackerRepository: PeriodTrackerRepository
) : ViewModel() {

    var periodSettings by mutableStateOf(PeriodTrackerSettings())
        private set

    var dailyDataMap by mutableStateOf<Map<LocalDate, DailyPeriodData>>(emptyMap())
        private set

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            periodTrackerRepository.getPeriodTrackerSettings().onSuccess { settings ->
                periodSettings = settings
            }
        }
    }

    fun loadMonthData(month: LocalDate) {
        viewModelScope.launch {
            periodTrackerRepository.getDailyPeriodDataForMonth(month).onSuccess { data ->
                dailyDataMap = data
            }
        }
    }

    fun saveSettings(newSettings: PeriodTrackerSettings) {
        periodSettings = newSettings
        viewModelScope.launch {
            periodTrackerRepository.savePeriodTrackerSettings(newSettings)
        }
    }

    fun saveDailyData(updatedData: DailyPeriodData) {
        dailyDataMap = dailyDataMap + (updatedData.date to updatedData)
        viewModelScope.launch {
            periodTrackerRepository.saveDailyPeriodData(updatedData)
        }
    }
}
