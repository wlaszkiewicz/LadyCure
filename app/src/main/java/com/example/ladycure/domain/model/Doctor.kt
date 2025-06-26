package com.example.ladycure.domain.model

import com.example.ladycure.R
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Represents a Doctor with detailed information including speciality, availability, reviews,
 * contact info, and personal data.
 *
 * @property speciality The medical speciality of the doctor.
 * @property availability List of availability schedules for the doctor.
 * @property reviews List of patient reviews for the doctor.
 * @property address The doctor's office or clinic address.
 * @property consultationPrice The consultation fee charged by the doctor.
 * @property rating Average rating score for the doctor.
 * @property experience Years of professional experience.
 * @property languages List of languages spoken by the doctor.
 * @property city The city where the doctor practices.
 * @property phone The contact phone number of the doctor.
 * @property bio A short biography or description of the doctor.
 * @property id Unique identifier for the doctor.
 * @property email Contact email of the doctor.
 * @property name Doctor's first name.
 * @property surname Doctor's last name.
 * @property dateOfBirth Doctor's birth date as a String.
 * @property profilePictureUrl URL to the doctor's profile picture.
 * @property role The role of the user, default is [Role.DOCTOR].
 * @property joinedAt Timestamp of when the doctor joined the platform.
 */
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

    /**
     * Converts this [Doctor] instance to a generic [User] instance with role set to USER.
     *
     * @return A [User] instance with details copied from this doctor.
     */
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

    /**
     * Converts the [Doctor] instance to a map representation for database storage.
     *
     * @return A map containing doctor fields and their values.
     */
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

    /**
     * Creates a copy of this doctor with optional modified fields.
     *
     * @return A new [Doctor] instance with updated values where specified.
     */
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
        /**
         * Creates a [Doctor] instance from a map representation.
         *
         * @param doctor A map containing doctor data.
         * @return A [Doctor] instance.
         */
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

        /**
         * Converts a [DoctorApplication] to a [Doctor] instance.
         *
         * @param application The doctor's application data.
         * @return A new [Doctor] instance.
         */
        fun fromApplication(application: DoctorApplication): Doctor {
            return Doctor(
                speciality = application.speciality,
                address = application.address,
                availability = emptyList(),
                reviews = emptyList(),
                consultationPrice = 100,
                rating = 0.0,
                experience = application.yearsOfExperience,
                languages = listOf("English"),
                city = application.city,
                phone = application.phoneNumber,
                bio = "New doctor",
                email = application.email,
                name = application.firstName,
                surname = application.lastName,
                dateOfBirth = application.dateOfBirth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                profilePictureUrl = "",
                id = application.userId,
                joinedAt = Timestamp.now()
            )
        }

        /**
         * Converts a [Doctor] instance into a map for storage or transmission.
         *
         * @param doctor The doctor instance.
         * @return A map containing the doctor's data.
         */
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

/**
 * Enum representing medical specialities with display names and icons.
 *
 * @property displayName Human-readable name of the speciality.
 * @property icon Resource ID for the speciality icon.
 * @property doctorCount Optional count of doctors in this speciality.
 */
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

/**
 * Represents the availability of a doctor on a specific date and times.
 *
 * @property doctorId The ID of the doctor.
 * @property date The date of availability.
 * @property startTime The start time of availability.
 * @property endTime The end time of availability.
 * @property availableSlots The list of available time slots.
 */
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