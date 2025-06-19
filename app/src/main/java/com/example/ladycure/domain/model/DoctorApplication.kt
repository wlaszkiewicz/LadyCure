package com.example.ladycure.domain.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DoctorApplication(
    val userId: String, // ID of the user applying
    val firstName: String,
    val lastName: String,
    val email: String,
    val dateOfBirth: LocalDate,
    val licenseNumber: String,
    val licensePhotoUrl: String,  // URL to uploaded license image
    val diplomaPhotoUrl: String,  // URL to uploaded diploma
    val speciality: Speciality,
    val yearsOfExperience: Int,
    val currentWorkplace: String,
    val phoneNumber: String,
    val address: String,
    val city: String,
    val status: ApplicationStatus = ApplicationStatus.PENDING,
    val submissionDate: LocalDate = LocalDate.now(),
    val reviewNotes: String? = null
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "dateOfBirth" to dateOfBirth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            "licenseNumber" to licenseNumber,
            "licensePhotoUrl" to licensePhotoUrl,
            "diplomaPhotoUrl" to diplomaPhotoUrl,
            "speciality" to speciality.displayName,
            "yearsOfExperience" to yearsOfExperience,
            "currentWorkplace" to currentWorkplace,
            "phoneNumber" to phoneNumber,
            "address" to address,
            "city" to city,
            "status" to status.displayName,
            "submissionDate" to submissionDate.toString(),
            "reviewNotes" to (reviewNotes ?: "")
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): DoctorApplication {
            return DoctorApplication(
                userId = map["userId"] as String,
                firstName = map["firstName"] as String,
                lastName = map["lastName"] as String,
                email = map["email"] as String,
                dateOfBirth = map["dateOfBirth"].let { date ->
                    if (date is String) LocalDate.parse(
                        date, DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    )
                    else throw IllegalArgumentException("Invalid date format")
                },
                licenseNumber = map["licenseNumber"] as String,
                licensePhotoUrl = map["licensePhotoUrl"] as String,
                diplomaPhotoUrl = map["diplomaPhotoUrl"] as String,
                speciality = Speciality.fromDisplayName(map["speciality"] as String),
                yearsOfExperience = (map["yearsOfExperience"] as Number).toInt(),
                currentWorkplace = map["currentWorkplace"] as String,
                phoneNumber = map["phoneNumber"] as String,
                address = map["address"] as String,
                city = map["city"] as String,
                status = ApplicationStatus.fromDisplayName(map["status"] as String),
                submissionDate = LocalDate.parse(map["submissionDate"] as String),
                reviewNotes = map["reviewNotes"] as? String
            )
        }
    }
}

enum class ApplicationStatus(val displayName: String) {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected"),
    NEEDS_MORE_INFO("needs_more_info");

    companion object {
        fun fromDisplayName(displayName: String): ApplicationStatus {
            return ApplicationStatus.entries.firstOrNull { it.displayName == displayName }
                ?: throw IllegalArgumentException("Unknown status: $displayName")
        }
    }
}