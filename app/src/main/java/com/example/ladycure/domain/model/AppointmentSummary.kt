package com.example.ladycure.domain.model

import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class AppointmentSummary(
    val appointmentId: String = "",
    val doctorName: String = "", // used in user ui
    val patientName: String = "", // used in doctor ui
    val dateTime: Timestamp = Timestamp.now(),
    val status: Appointment.Status = Appointment.Status.PENDING,
    val type: String = "", // displayName!!
    val price: Double = 0.0
) {
    val date: LocalDate
        get() = dateTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    val time: LocalTime
        get() = dateTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalTime()

    val enumType: AppointmentType
        get() = AppointmentType.fromDisplayName(type)


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

    fun toMap(): Map<String, Any> = mapOf(
        "doctorName" to doctorName,
        "dateTime" to dateTime,
        "status" to status.displayName,
        "type" to type,
        "price" to price
    )

}
