package com.example.ladycure.domain.model

import com.example.ladycure.R
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    override val phone: String,
    val bio: String,
    override val id: String,
    override val email: String,
    override val name: String,
    override val surname: String,
    override val dateOfBirth: String,
    override val profilePictureUrl: String,
    override val role: Role = Role.DOCTOR,
    override val joinedAt: Timestamp,
) : User(
    id = id,
    email = email,
    name = name,
    surname = surname,
    dateOfBirth = dateOfBirth,
    role = Role.DOCTOR,
    profilePictureUrl = profilePictureUrl,
    joinedAt = joinedAt,
    phone = phone
) {


    // In Doctor class
    fun toUser(): User {
        return User(
            id = this.id,
            name = this.name,
            surname = this.surname,
            email = this.email,
            dateOfBirth = this.dateOfBirth,
            profilePictureUrl = this.profilePictureUrl,
            role = Role.USER,
            joinedAt = this.joinedAt,
            phone = this.phone
        )
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "speciality" to speciality.displayName,
            "availability" to availability,
            "reviews" to reviews,
            "address" to address,
            "consultationPrice" to consultationPrice,
            "rating" to rating,
            "experience" to experience,
            "languages" to languages,
            "city" to city,
            "phone" to phone,
            "bio" to bio,
            "email" to email,
            "name" to name,
            "surname" to surname,
            "dob" to dateOfBirth,
            "profilePictureUrl" to profilePictureUrl,
            "role" to Role.DOCTOR.value,
            "joinedAt" to joinedAt,
        )
    }

    fun copyDoc(
        id: String = this.id,
        email: String = this.email,
        name: String = this.name,
        surname: String = this.surname,
        dateOfBirth: String = this.dateOfBirth,
        role: Role = this.role,
        profilePictureUrl: String = this.profilePictureUrl,
        speciality: Speciality = this.speciality,
        availability: List<DoctorAvailability> = this.availability,
        reviews: List<Review> = this.reviews,
        address: String = this.address,
        consultationPrice: Int = this.consultationPrice,
        rating: Double = this.rating,
        experience: Int = this.experience,
        languages: List<String> = this.languages,
        city: String = this.city,
        phone: String = this.phone,
        bio: String = this.bio,
        joinedAt: Timestamp = this.joinedAt
    ): Doctor {
        return Doctor(
            speciality = speciality,
            availability = availability,
            reviews = reviews,
            address = address,
            consultationPrice = consultationPrice,
            rating = rating,
            experience = experience,
            languages = languages,
            city = city,
            phone = phone,
            bio = bio,
            id = id,
            email = email,
            name = name,
            surname = surname,
            dateOfBirth = dateOfBirth,
            profilePictureUrl = profilePictureUrl,
            role = role,
            joinedAt = joinedAt
        )
    }

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
                    doctor["speciality"] as? String ?: "Other"
                ),
                address = doctor["address"] as? String ?: "No address provided",
                availability = doctor["availability"] as? List<DoctorAvailability> ?: emptyList(),
                reviews = doctor["reviews"] as? List<Review> ?: emptyList(),
                consultationPrice = price,
                rating = rating,
                experience = experience,
                languages = doctor["languages"] as? List<String> ?: listOf("English"),
                city = doctor["city"] as? String ?: "Unknown city",
                phone = doctor["phone"] as? String ?: "No phone number",
                bio = doctor["bio"] as? String ?: "No bio available",
                email = doctor["email"] as? String ?: "No email provided",
                name = doctor["name"] as? String ?: "No name provided",
                surname = doctor["surname"] as? String ?: "No surname provided",
                dateOfBirth = doctor["dob"] as? String ?: "Unknown date",
                profilePictureUrl = doctor["profilePictureUrl"] as? String ?: "No profile picture",
                id = doctor["id"] as? String ?: "No ID provided",
                joinedAt = doctor["joinedAt"] as? Timestamp ?: Timestamp.now()
            )
        }

        fun fromApplication(application: DoctorApplication): Doctor {
            return Doctor(
                speciality = application.speciality,
                address = application.address,
                availability = emptyList(),
                reviews = emptyList(),
                consultationPrice = 100,
                rating = 0.0,
                experience = application.yearsOfExperience,
                languages = listOf("English"), // Default language, can be adjusted later
                city = application.city,
                phone = application.phoneNumber,
                bio = "New doctor", // Default bio, can be adjusted later
                email = application.email,
                name = application.firstName,
                surname = application.lastName,
                dateOfBirth = application.dateOfBirth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                profilePictureUrl = "", // No profile picture in application
                id = application.userId,
                joinedAt = Timestamp.now()
            )
        }

        fun toMap(doctor: Doctor): Map<String, Any> {
            return mapOf(
                "speciality" to doctor.speciality.displayName,
                "availability" to doctor.availability,
                "reviews" to doctor.reviews,
                "address" to doctor.address,
                "consultationPrice" to doctor.consultationPrice,
                "rating" to doctor.rating,
                "experience" to doctor.experience,
                "languages" to doctor.languages,
                "city" to doctor.city,
                "phone" to doctor.phone,
                "bio" to doctor.bio,
                "email" to doctor.email,
                "name" to doctor.name,
                "surname" to doctor.surname,
                "dateOfBirth" to doctor.dateOfBirth,
                "profilePictureUrl" to doctor.profilePictureUrl,
                "role" to doctor.role.value,
                "id" to doctor.id,
                "joinedAt" to doctor.joinedAt
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
) {
    companion object {
        fun fromMap(
            map: Map<String, Any>,
            doctorId: String,
            documentId: String
        ): DoctorAvailability {
            val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
            return DoctorAvailability(
                doctorId = doctorId,
                date = LocalDate.parse(documentId, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                startTime = (map["startTime"] as? String)?.let { LocalTime.parse(it, formatter) },
                endTime = (map["endTime"] as? String)?.let { LocalTime.parse(it, formatter) },
                availableSlots = (map["availableSlots"] as? List<String>)?.map {
                    LocalTime.parse(it, formatter)
                }?.toMutableList() ?: mutableListOf()
            )
        }
    }
}