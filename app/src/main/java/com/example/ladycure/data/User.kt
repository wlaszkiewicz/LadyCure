package com.example.ladycure.data

import com.example.ladycure.data.doctor.Availability
import com.example.ladycure.data.doctor.Review

data class User(
    val email: String,
    val name: String,
    val surname: String,
    val dateOfBirth: String,
    val role : Role,
    val profilePictureUrl: String,
    //for doctors only
    val specialization: Specialization,
    val availability: List<Availability>,
    val reviews: List<Review>,
    val address: String,
    val consultationPrice: String,
)

enum class Role(val value: String) {
    DOCTOR("Doctor"),
    PATIENT("Patient"),
    ADMIN("Admin");

    companion object {
        fun fromValue(value: String): Role {
            return values().firstOrNull { it.value == value } ?: PATIENT
        }
    }
}


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


