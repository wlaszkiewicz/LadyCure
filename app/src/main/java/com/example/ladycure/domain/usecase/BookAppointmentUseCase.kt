package com.example.ladycure.domain.usecase

import com.example.ladycure.data.repository.AppointmentRepository
import com.example.ladycure.data.repository.AuthRepository
import com.example.ladycure.data.repository.UserRepository
import com.example.ladycure.domain.model.Appointment
import com.example.ladycure.domain.model.AppointmentType
import com.google.firebase.Timestamp
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookAppointmentUseCase @Inject constructor(
    private val appointmentRepo: AppointmentRepository,
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(
        doctorId: String,
        timestamp: Timestamp,
        appointmentType: AppointmentType,
        doctorInfo: Map<String, Any>
    ): Result<String> {
        return try {
            val patientId = authRepo.getCurrentUserId()
                ?: return Result.failure(Exception("User not logged in"))

            val firstName = userRepo.getUserField("name").getOrNull().orEmpty()
            val lastName = userRepo.getUserField("surname").getOrNull().orEmpty()
            val patientName = "$firstName $lastName".trim()

            val doctorFirstName = doctorInfo["name"] as? String ?: ""
            val doctorLastName = doctorInfo["surname"] as? String ?: ""

            val appointment = Appointment(
                appointmentId = "",
                doctorId = doctorId,
                dateTime = timestamp,
                patientId = patientId,
                status = Appointment.Status.PENDING,
                type = appointmentType,
                price = appointmentType.price,
                address = doctorInfo["address"] as? String ?: "Address unavailable",
                doctorName = "$doctorFirstName $doctorLastName".trim(),
                patientName = patientName,
                comments = ""
            )

            appointmentRepo.bookAppointment(appointment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
