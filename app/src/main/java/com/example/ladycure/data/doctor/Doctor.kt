package com.example.ladycure.data.doctor

import com.example.ladycure.data.Role
import com.example.ladycure.data.User

data class Doctor(
    val specialization: Specialization,
    val availability: List<Availability>,
    val reviews: List<Review>,
    val address: String,
    val consultationPrice: String,
    val rating: Double,
    val experience: String,
    val languages: List<String>,
    val city: String,
    val phoneNumber: String,
    val bio: String,
    override val email: String,
    override val name: String,
    override val surname: String,
    override val dateOfBirth: String,
    override val profilePictureUrl: String,
    override val role: Role = Role.DOCTOR
) : User(
    id = "",
    email = email,
    name = name,
    surname = surname,
    dateOfBirth = dateOfBirth,
    role = Role.DOCTOR,
    profilePictureUrl = profilePictureUrl
)


enum class Specialization(val displayName: String) {
    FAMILY_MEDICINE("Family Medicine"),
    DERMATOLOGY("Dermatology"),
    CARDIOLOGY("Cardiology"),
    DENTISTRY("Dentistry"),
    ENDOCRINOLOGY("Endocrinology"),
    GYNECOLOGY("Gynecology"),
    NEUROLOGY("Neurology"),
    ONCOLOGY("Oncology"),
    OPHTHALMOLOGY("Ophthalmology"),
    ORTHOPEDICS("Orthopedics"),
    PEDIATRICS("Pediatrics"),
    PSYCHIATRY("Psychiatry"),
    PHYSIOTHERAPY("Physiotherapy"),
    RADIOLOGY("Radiology"),
    OTHER("Other");

    companion object {
        fun fromDisplayName(displayName: String): Specialization {
            return values().firstOrNull { it.displayName == displayName } ?: OTHER
        }

    }
}

data class DoctorAvailability(
    val doctorId: String,
    val date: String,
    val startTime: String,
    val endTime: String
)