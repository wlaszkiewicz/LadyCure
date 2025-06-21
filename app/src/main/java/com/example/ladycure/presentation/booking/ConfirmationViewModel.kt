package com.example.ladycure.presentation.booking

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.AppointmentRepository
import com.example.ladycure.data.repository.AuthRepository
import com.example.ladycure.data.repository.DoctorRepository
import com.example.ladycure.data.repository.StorageRepository
import com.example.ladycure.data.repository.UserRepository
import com.example.ladycure.domain.model.Appointment
import com.example.ladycure.domain.model.AppointmentType
import com.example.ladycure.domain.model.Doctor
import com.example.ladycure.domain.model.Referral
import com.example.ladycure.utility.PdfUploader
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class ConfirmationViewModel(
    private val userRepo: UserRepository = UserRepository(),
    private val authRepo: AuthRepository = AuthRepository(),
    private val doctorRepo: DoctorRepository = DoctorRepository(),
    private val appointmentRepo: AppointmentRepository = AppointmentRepository(),
    private val referralRepo: StorageRepository = StorageRepository()
) : ViewModel() {

    // State variables
    var doctorInfo by mutableStateOf<Map<String, Any>?>(null)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        internal set
    var userName by mutableStateOf("Patient unavailable")
        private set
    var referral by mutableStateOf<Referral?>(null)
        private set
    var isUploading by mutableStateOf(false)
        private set
    var showUploadSuccess by mutableStateOf(false)
        private set
    var uploadProgress by mutableStateOf(0f)
        private set
    var dateTime by mutableStateOf(Timestamp.now())

    var tooLarge by mutableStateOf(false)
        internal set

    fun loadInitialData(
        doctorId: String,
        timestamp: Timestamp,
        referralId: String?
    ) {
        viewModelScope.launch {
            try {

                dateTime = timestamp
                // Load user name
                userName = withContext(Dispatchers.IO) {
                    "${userRepo.getUserField("name").getOrNull()} ${
                        userRepo.getUserField("surname").getOrNull()
                    }"
                }

                // Load doctor info
                val result = withContext(Dispatchers.IO) {
                    doctorRepo.getDoctorById(doctorId)
                }
                if (result.isSuccess) {
                    val doctor = result.getOrNull()
                    doctorInfo = Doctor.toMap(doctor!!)
                } else {
                    errorMessage = "Failed to load doctor details"
                }

                // Load referral if exists
                if (referralId != null) {
                    val referralResult = withContext(Dispatchers.IO) {
                        referralRepo.getReferralById(referralId)
                    }
                    if (referralResult.isSuccess) {
                        referral = referralResult.getOrNull()
                    } else {
                        errorMessage = "Failed to load referral document"
                    }
                }

                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                isLoading = false
            }
        }
    }

    fun uploadReferral(
        uri: Uri,
        referralId: String,
        appointmentType: AppointmentType,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            if (PdfUploader.isFileTooLarge(context = context, uri = uri)) {
                tooLarge = true
                return@launch
            }
            try {
                withContext(Dispatchers.Main) {
                    isUploading = true
                    uploadProgress = 0f
                }

                val result = withContext(Dispatchers.IO) {
                    referralRepo.replaceReferralInFirestore(
                        uri = uri,
                        oldUri = referral?.url.toString(),
                        referralId = referralId,
                        service = appointmentType.displayName,
                    ) { progress ->
                        uploadProgress = progress.progress
                    }
                }

                withContext(Dispatchers.Main) {
                    isUploading = false
                    if (result.isSuccess) {
                        referral = referral?.copy(url = result.getOrNull() ?: "")
                        showUploadSuccess = true
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

    fun bookAppointment(
        doctorId: String,
        timestamp: Timestamp,
        appointmentType: AppointmentType,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val appointment = createAppointment(doctorId, timestamp, appointmentType)
                val result = withContext(Dispatchers.IO) {
                    appointmentRepo.bookAppointment(appointment)
                }
                if (result.isSuccess) {
                    onSuccess(result.getOrNull() ?: "")
                } else {
                    onError("Failed to book appointment: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            }
        }
    }

    private fun createAppointment(
        doctorId: String,
        timestamp: Timestamp,
        appointmentType: AppointmentType
    ): Appointment {
        return Appointment(
            appointmentId = "",
            doctorId = doctorId,
            dateTime = timestamp,
            patientId = authRepo.getCurrentUserId().toString(),
            status = Appointment.Status.PENDING,
            type = appointmentType,
            price = appointmentType.price,
            address = doctorInfo?.get("address") as? String ?: "Address unavailable",
            doctorName = (doctorInfo?.get("name") as? String + " " +
                    doctorInfo?.get("surname") as? String),
            patientName = userName,
            comments = "",
        )
    }

    // Helper properties for date/time display
    val formattedDate: String
        get() = doctorInfo?.let {
            val date = dateTime.toDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
        } ?: "Date unavailable"

    val formattedTime: String
        get() = doctorInfo?.let {
            val time = dateTime.toDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
            time.format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
        } ?: "Time unavailable"


}