package com.example.ladycure.presentation.home

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.AppointmentRepository
import com.example.ladycure.data.repository.DoctorRepository
import com.example.ladycure.data.repository.UserRepository
import com.example.ladycure.domain.model.Appointment
import com.example.ladycure.utility.SharedPreferencesHelper
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// A data class to hold all the UI state in one place
data class HomeUiState(
    val userData: Map<String, Any>? = null,
    var appointments: List<Appointment>? = null,
    val availableCities: List<String> = listOf("Warszawa"),
    val locationFetched: Boolean = false,
    val initialCity: String? = null,
    val selectedCity: String? = null,
    val error: String? = null
)


class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // Repositories
    private val userRepo = UserRepository()
    private val appointmentRepo = AppointmentRepository()
    private val doctorRepo = DoctorRepository()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchInitialData()
    }

    private fun fetchInitialData() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            if (SharedPreferencesHelper.shouldRememberChoice(context)) {
                SharedPreferencesHelper.getCity(context)?.let { savedCity ->
                    _uiState.update {
                        it.copy(
                            selectedCity = savedCity,
                            locationFetched = true
                        )
                    }
                }
            }

            userRepo.getCurrentUserData().onSuccess { data ->
                _uiState.update { it.copy(userData = data) }
            }
            appointmentRepo.getAppointments("user").onSuccess { apps ->
                _uiState.update { it.copy(appointments = apps) }
            }
            doctorRepo.getAvailableCities().onSuccess { cities ->
                if (cities.isNotEmpty()) {
                    _uiState.update { it.copy(availableCities = cities) }
                }
            }
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        if (_uiState.value.locationFetched) return
        if (isGranted) {
            fetchDeviceLocation()
        } else {
            handlePermissionDenied()
        }
    }


    private fun fetchDeviceLocation() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // This should not happen if isGranted is true, but it's a good safeguard
                handlePermissionDenied()
                return@launch
            }

            val locationResult: Location? = try {
                fusedLocationClient.lastLocation.await() ?: run {
                    val request = CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
                    fusedLocationClient.getCurrentLocation(request, null).await()
                }
            } catch (e: Exception) {
                null
            }

            if (locationResult != null) {
                val nearestCity = findNearestCity(locationResult.latitude, locationResult.longitude)
                _uiState.update { it.copy(initialCity = nearestCity, locationFetched = true) }
            } else {
                _uiState.update {
                    it.copy(
                        initialCity = "Detecting City...",
                        locationFetched = true
                    )
                }
            }
        }
    }

    fun handlePermissionDenied() {
        if (!_uiState.value.locationFetched) {
            val defaultCity = "Warszawa"
            _uiState.update {
                it.copy(initialCity = defaultCity, locationFetched = true)
            }
            val context = getApplication<Application>().applicationContext
            SharedPreferencesHelper.saveRememberChoice(context, true)
            SharedPreferencesHelper.saveCity(context, defaultCity)
        }
    }

    fun onCitySelected(city: String) {
        _uiState.update { it.copy(selectedCity = city) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}