package com.example.ladycure.data.doctor

import com.example.ladycure.data.Role
import com.example.ladycure.data.User

data class Doctor(
    val specialization: Specialization,
    val availability: List<Availability>,
    val reviews: List<Review>,
    val address: String,
    val consultationPrice: String,
    override val email: String,
    override val name: String,
    override val surname: String,
    override val dateOfBirth: String,
    override val profilePictureUrl: String,
    override val role: Role = Role.DOCTOR
) : User(
    email = email,
    name = name,
    surname = surname,
    dateOfBirth = dateOfBirth,
    role = Role.DOCTOR,
    profilePictureUrl = profilePictureUrl
)


enum class Specialization(val displayName: String) {
    CARDIOLOGY("Cardiology"),
    DENTISTRY("Dentistry"),
    DERMATOLOGY("Dermatology"),
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
}