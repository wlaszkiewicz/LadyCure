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
import com.example.ladycure.data.repository.NotificationRepository
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

/**
 * A data class to hold all the UI state in one place for the Home screen.
 * @property userData User-specific data, nullable.
 * @property unreadNotificationCount The number of unread notifications.
 * @property appointments A list of appointments, nullable.
 * @property availableCities A list of cities where services are available, defaults to "Warszawa".
 * @property locationFetched Indicates if the device location has been fetched.
 * @property initialCity The city determined initially, often based on location or default.
 * @property selectedCity The city currently selected by the user.
 * @property error An error message, if any.
 */
data class HomeUiState(
    val userData: Map<String, Any>? = null,
    val unreadNotificationCount: Int = 0,
    var appointments: List<Appointment>? = null,
    val availableCities: List<String> = listOf("Warszawa"),
    val locationFetched: Boolean = false,
    val initialCity: String? = null,
    val selectedCity: String? = null,
    val error: String? = null
)


/**
 * ViewModel for the Home screen, responsible for managing UI-related data and logic.
 * @param application The application instance.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepo = UserRepository()
    private val appointmentRepo = AppointmentRepository()
    private val notificationRepo = NotificationRepository()
    private val doctorRepo = DoctorRepository()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val _uiState = MutableStateFlow(HomeUiState())

    /**
     * Exposes the UI state as a [StateFlow] for observing in the UI.
     */
    val uiState = _uiState.asStateFlow()

    init {
        fetchInitialData()
    }

    /**
     * Fetches the initial data required for the Home screen, including user data, appointments,
     * available cities, and unread notification count.
     * It also checks for a previously saved city choice.
     */
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

            notificationRepo.getUnreadNotificationsCount(
                onResult = { count ->
                    _uiState.update { it.copy(unreadNotificationCount = count) }
                },
                onError = { error ->
                    _uiState.update { it.copy(error = error) }
                }
            )
        }
    }

    /**
     * Handles the result of a location permission request.
     * If permission is granted, fetches the device location. Otherwise, handles the permission denial.
     * @param isGranted True if the permission was granted, false otherwise.
     */
    fun onPermissionResult(isGranted: Boolean) {
        if (_uiState.value.locationFetched) return
        if (isGranted) {
            fetchDeviceLocation()
        } else {
            handlePermissionDenied()
        }
    }

    /**
     * Fetches the current device location. If location access is not granted, it calls [handlePermissionDenied].
     * It attempts to get the last known location first, then requests the current location.
     */
    private fun fetchDeviceLocation() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
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

    /**
     * Handles the scenario where location permission is denied. It sets a default city ("Warszawa")
     * and saves this choice for future use.
     */
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

    /**
     * Updates the selected city in the UI state.
     * @param city The city selected by the user.
     */
    fun onCitySelected(city: String) {
        _uiState.update { it.copy(selectedCity = city) }
    }

    /**
     * Clears any error messages currently held in the UI state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}