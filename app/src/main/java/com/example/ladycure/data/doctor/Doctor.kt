package com.example.ladycure.data.doctor

import com.example.ladycure.R
import com.example.ladycure.data.Role
import com.example.ladycure.data.User
import java.time.LocalDate
import java.time.LocalTime

data class Doctor(
    val speciality: Speciality,
    val availability: List<DoctorAvailability> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val address: String = "No address provided",
    val consultationPrice: Int = 100,
    val rating: Double = 4.5,
    val experience: Int = 5,
    val languages: List<String> = listOf("English"),
    val city: String,
    val phoneNumber: String,
    val bio: String,
    override val id: String,
    override val email: String,
    override val name: String,
    override val surname: String,
    override val dateOfBirth: String,
    override val profilePictureUrl: String,
    override val role: Role = Role.DOCTOR
) : User(
    id = id,
    email = email,
    name = name,
    surname = surname,
    dateOfBirth = dateOfBirth,
    role = Role.DOCTOR,
    profilePictureUrl = profilePictureUrl
) {
    companion object {
        fun fromMap(doctor: Map<String, Any>): Doctor {
            val rating = when (val rat = doctor["rating"]) {
                is Int -> rat.toDouble()
                is Long -> rat.toDouble()
                is Double -> rat
                is String -> rat.toDouble()
                else -> 4.5
            }

            val experience = when (val exp = doctor["experience"]) {
                is Int -> exp
                is Long -> exp.toInt()
                is Double -> exp.toInt()
                is String -> exp.toIntOrNull() ?: 5
                else -> 5
            }

            val price = when (val exp = doctor["consultationPrice"]) {
                is Int -> exp
                is Long -> exp.toInt()
                is Double -> exp.toInt()
                is String -> exp.toIntOrNull() ?: 100
                else -> 100
            }

            return Doctor(
                speciality = Speciality.fromDisplayName(
                    doctor["specification"] as? String ?: "Other"
                ),
                address = doctor["address"] as? String ?: "No address provided",
                availability = doctor["availability"] as? List<DoctorAvailability> ?: emptyList(),
                reviews = doctor["reviews"] as? List<Review> ?: emptyList(),
                consultationPrice = price,
                rating = rating,
                experience = experience,
                languages = doctor["languages"] as? List<String> ?: listOf("English"),
                city = doctor["city"] as? String ?: "Unknown city",
                phoneNumber = doctor["phone"] as? String ?: "No phone number",
                bio = doctor["bio"] as? String ?: "No bio available",
                email = doctor["email"] as? String ?: "No email provided",
                name = doctor["name"] as? String ?: "No name provided",
                surname = doctor["surname"] as? String ?: "No surname provided",
                dateOfBirth = doctor["dob"] as? String ?: "Unknown date",
                profilePictureUrl = doctor["profilePictureUrl"] as? String ?: "No profile picture",
                id = doctor["id"] as? String ?: "No ID provided",
            )
        }

        fun toMap(doctor: Doctor): Map<String, Any?> {
            return mapOf(
                "specification" to doctor.speciality.displayName,
                "availability" to doctor.availability,
                "reviews" to doctor.reviews,
                "address" to doctor.address,
                "consultationPrice" to doctor.consultationPrice,
                "rating" to doctor.rating,
                "experience" to doctor.experience,
                "languages" to doctor.languages,
                "city" to doctor.city,
                "phoneNumber" to doctor.phoneNumber,
                "bio" to doctor.bio,
                "email" to doctor.email,
                "name" to doctor.name,
                "surname" to doctor.surname,
                "dateOfBirth" to doctor.dateOfBirth,
                "profilePictureUrl" to doctor.profilePictureUrl,
                "role" to doctor.role.value,
            )
        }
    }
}


enum class Speciality(val displayName: String, val icon: Int, val doctorCount: Int? = 2) {
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