package com.example.ladycure.data.doctor

import com.example.ladycure.R
import com.example.ladycure.data.Role
import com.example.ladycure.data.User
import java.time.LocalDate
import java.time.LocalTime

data class Doctor(
    val speciality: Speciality,
    val availability: List<DoctorAvailability>,
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


enum class Speciality(val displayName: String, val icon: Int, val doctorCount: Int? = 2 ) {
    FAMILY_MEDICINE("Family Medicine", icon = (R.drawable.ic_family_medicine)),
    DERMATOLOGY("Dermatology", icon = (R.drawable.ic_dermatology)),
    CARDIOLOGY("Cardiology", icon = (R.drawable.ic_cardiology)),
    DENTISTRY("Dentistry", icon = (R.drawable.ic_dentistry)),
    ENDOCRINOLOGY("Endocrinology", icon = (R.drawable.ic_endocrinology)),
    GYNECOLOGY("Gynecology", icon = (R.drawable.ic_gynecology)),
    NEUROLOGY("Neurology", icon = (R.drawable.ic_neurology)),
    ONCOLOGY("Oncology", icon = (R.drawable.ic_oncology)),
    OPHTHALMOLOGY("Ophthalmology", icon = (R.drawable.ic_ophthalmology)),
    ORTHOPEDICS("Orthopedics", icon = (R.drawable.ic_orthopedics)),
    PEDIATRICS("Pediatrics", icon = (R.drawable.ic_pediatrics)),
    PSYCHIATRY("Psychiatry", icon = (R.drawable.ic_psychology)),
    PHYSIOTHERAPY("Physiotherapy", icon = (R.drawable.ic_physiotherapy)),
    RADIOLOGY("Radiology", icon = (R.drawable.ic_radiology)),
    OTHER("Other", icon = (R.drawable.ic_medical_services));

    companion object {
        fun fromDisplayName(displayName: String): Speciality {
            return values().firstOrNull { it.displayName == displayName } ?: OTHER
        }

        val popularCategories: List<Speciality>
            get() = listOf(FAMILY_MEDICINE, GYNECOLOGY, DERMATOLOGY, DENTISTRY, PSYCHIATRY)

    }
}

data class DoctorAvailability(
    val doctorId: String,
    val date: LocalDate?,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val availableSlots: MutableList<LocalTime>
)