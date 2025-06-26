package com.example.ladycure.domain.model

import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Represents a summary of an appointment with essential details.
 *
 * @property appointmentId The unique identifier of the appointment.
 * @property doctorName The name of the doctor for the appointment.
 * @property patientName The name of the patient for the appointment.
 * @property dateTime The date and time of the appointment as a Firebase [Timestamp].
 * @property status The current status of the appointment.
 * @property type The type of the appointment as a display name [String].
 * @property price The price of the appointment.
 */
data class AppointmentSummary(
    val appointmentId: String = "",
    val doctorName: String = "",
    val patientName: String = "",
    val dateTime: Timestamp = Timestamp.now(),
    val status: Appointment.Status = Appointment.Status.PENDING,
    val type: String = "",
    val price: Double = 0.0
) {
    /**
     * Returns the [LocalDate] part of the appointment's dateTime.
     */
    val date: LocalDate
        get() = dateTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    /**
     * Returns the [LocalTime] part of the appointment's dateTime.
     */
    val time: LocalTime
        get() = dateTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalTime()

    /**
     * Returns the [AppointmentType] corresponding to the appointment's type display name.
     */
    val enumType: AppointmentType
        get() = AppointmentType.fromDisplayName(type)


    /**
     * Creates an instance of [AppointmentSummary] from a map of values, typically
     * retrieved from a database document.
     *
     * @param map The map containing appointment data.
     * @param docId The document ID to use as the appointmentId.
     * @return An instance of [AppointmentSummary].
     */
    companion object {
        fun fromMap(map: Map<String, Any?>, docId: String): AppointmentSummary {
            return AppointmentSummary(
                appointmentId = docId,
                doctorName = map["doctorName"] as? String ?: "",
                dateTime = map["dateTime"] as? Timestamp ?: Timestamp.now(),
                status = Appointment.Status.fromDisplayName(map["status"] as? String ?: ""),
                type = map["type"] as? String ?: "",
                price = (map["price"] as? Number)?.toDouble() ?: 0.0
            )
        }
    }

    /**
     * Converts this [AppointmentSummary] into a map suitable for database storage.
     *
     * @return A [Map] containing the appointment data.
     */
    fun toMap(): Map<String, Any> = mapOf(
        "doctorName" to doctorName,
        "dateTime" to dateTime,
        "status" to status.displayName,
        "type" to type,
        "price" to price
    )

}
