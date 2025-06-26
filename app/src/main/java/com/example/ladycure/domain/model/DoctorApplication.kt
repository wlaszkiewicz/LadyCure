package com.example.ladycure.domain.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Represents an application submitted by a user to become a doctor in the system.
 *
 * @property userId The unique identifier of the user submitting the application.
 * @property firstName The applicant's first name.
 * @property lastName The applicant's last name.
 * @property email The applicant's email address.
 * @property dateOfBirth The applicant's date of birth.
 * @property licenseNumber The professional license number of the applicant.
 * @property licensePhotoUrl URL pointing to the photo of the applicant's professional license.
 * @property diplomaPhotoUrl URL pointing to the photo of the applicant's diploma.
 * @property speciality The medical speciality the applicant practices.
 * @property yearsOfExperience The number of years the applicant has worked in their speciality.
 * @property currentWorkplace The name of the applicant's current workplace or hospital.
 * @property phoneNumber The applicant's phone number.
 * @property address The applicant's address.
 * @property city The city where the applicant resides or works.
 * @property status The current status of the application (default is [ApplicationStatus.PENDING]).
 * @property submissionDate The date when the application was submitted (default is current date).
 * @property reviewNotes Optional notes or comments made during application review.
 */
data class DoctorApplication(
    val userId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val dateOfBirth: LocalDate,
    val licenseNumber: String,
    val licensePhotoUrl: String,
    val diplomaPhotoUrl: String,
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

    /**
     * Converts this [DoctorApplication] instance into a [Map] of key-value pairs.
     *
     * The date fields are formatted as strings using the pattern "yyyy-MM-dd".
     *
     * @return A map representing the application data suitable for storage or transmission.
     */
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
        /**
         * Creates a [DoctorApplication] object from a map of key-value pairs.
         *
         * The map must contain all required fields with the appropriate types.
         * Date fields must be strings formatted as "yyyy-MM-dd".
         *
         * @param map A map containing the application data.
         * @return A [DoctorApplication] instance initialized with the map data.
         * @throws IllegalArgumentException If any date strings are improperly formatted or required fields are missing.
         */
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

/**
 * Represents the status of a doctor application.
 *
 * @property displayName The string representation of the status used in storage or display.
 */
enum class ApplicationStatus(val displayName: String) {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected"),
    NEEDS_MORE_INFO("needs_more_info");

    companion object {
        /**
         * Returns the [ApplicationStatus] corresponding to the given display name.
         *
         * @param displayName The string representation of the status.
         * @return The matching [ApplicationStatus].
         * @throws IllegalArgumentException If the display name does not match any status.
         */
        fun fromDisplayName(displayName: String): ApplicationStatus {
            return ApplicationStatus.entries.firstOrNull { it.displayName == displayName }
                ?: throw IllegalArgumentException("Unknown status: $displayName")
        }
    }
}