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

/**
 * ViewModel for the confirmation screen, handling the display of appointment details,
 * referral uploads, and appointment booking.
 *
 * @param userRepo Repository for user-related operations.
 * @param authRepo Repository for authentication-related operations.
 * @param doctorRepo Repository for doctor-related operations.
 * @param appointmentRepo Repository for appointment-related operations.
 * @param referralRepo Repository for referral storage operations.
 */
class ConfirmationViewModel(
    private val userRepo: UserRepository = UserRepository(),
    private val authRepo: AuthRepository = AuthRepository(),
    private val doctorRepo: DoctorRepository = DoctorRepository(),
    private val appointmentRepo: AppointmentRepository = AppointmentRepository(),
    private val referralRepo: StorageRepository = StorageRepository()
) : ViewModel() {

    /**
     * Holds the doctor's information as a map.
     */
    var doctorInfo by mutableStateOf<Map<String, Any>?>(null)
        private set

    /**
     * Indicates whether data is currently being loaded.
     */
    var isLoading by mutableStateOf(true)
        private set

    /**
     * Holds any error message that occurs during operations.
     */
    var errorMessage by mutableStateOf<String?>(null)
        internal set

    /**
     * Holds the name of the patient.
     */
    var userName by mutableStateOf("Patient unavailable")
        private set

    /**
     * Holds the referral document information.
     */
    var referral by mutableStateOf<Referral?>(null)
        private set

    /**
     * Indicates whether a referral is currently being uploaded.
     */
    var isUploading by mutableStateOf(false)
        private set

    /**
     * Indicates whether the referral upload was successful.
     */
    var showUploadSuccess by mutableStateOf(false)
        private set

    /**
     * Current progress of the referral upload (0f to 1f).
     */
    var uploadProgress by mutableStateOf(0f)
        private set

    /**
     * The timestamp of the appointment.
     */
    var dateTime by mutableStateOf(Timestamp.now())

    /**
     * Indicates if the selected file is too large for upload.
     */
    var tooLarge by mutableStateOf(false)
        internal set

    /**
     * Loads initial data required for the confirmation screen, including user name,
     * doctor information, and optional referral details.
     *
     * @param doctorId The ID of the selected doctor.
     * @param timestamp The timestamp of the selected appointment.
     * @param referralId The optional ID of the referral document.
     */
    fun loadInitialData(
        doctorId: String,
        timestamp: Timestamp,
        referralId: String?
    ) {
        viewModelScope.launch {
            try {
                dateTime = timestamp
                userName = withContext(Dispatchers.IO) {
                    "${userRepo.getUserField("name").getOrNull()} ${
                        userRepo.getUserField("surname").getOrNull()
                    }"
                }

                val result = withContext(Dispatchers.IO) {
                    doctorRepo.getDoctorById(doctorId)
                }
                if (result.isSuccess) {
                    val doctor = result.getOrNull()
                    doctorInfo = Doctor.toMap(doctor!!)
                } else {
                    errorMessage = "Failed to load doctor details"
                }

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

    /**
     * Uploads or replaces a referral PDF document.
     *
     * @param uri The URI of the PDF file to upload.
     * @param referralId The ID of the referral document.
     * @param appointmentType The type of appointment, used for service identification.
     * @param context The Android context, used for file size checking.
     * @param onSuccess Callback function to be invoked upon successful upload.
     * @param onError Callback function to be invoked if an error occurs during upload.
     */
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

    /**
     * Books a new appointment.
     *
     * @param doctorId The ID of the doctor for the appointment.
     * @param timestamp The timestamp of the appointment.
     * @param appointmentType The type of appointment.
     * @param onSuccess Callback function to be invoked upon successful booking, providing the appointment ID.
     * @param onError Callback function to be invoked if an error occurs during booking.
     */
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

    /**
     * Creates an [Appointment] object based on the provided details.
     *
     * @param doctorId The ID of the doctor.
     * @param timestamp The timestamp of the appointment.
     * @param appointmentType The type of the appointment.
     * @return An [Appointment] object.
     */
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

    /**
     * Formatted date string for display.
     */
    val formattedDate: String
        get() = doctorInfo?.let {
            val date = dateTime.toDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
        } ?: "Date unavailable"

    /**
     * Formatted time string for display.
     */
    val formattedTime: String
        get() = doctorInfo?.let {
            val time = dateTime.toDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
            time.format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
        } ?: "Time unavailable"
}