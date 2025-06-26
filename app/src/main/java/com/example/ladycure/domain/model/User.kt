package com.example.ladycure.domain.model

import com.google.firebase.Timestamp

/**
 * Represents a user in the system with common user attributes.
 *
 * @property id Unique identifier for the user.
 * @property email The user's email address.
 * @property name The user's first name.
 * @property surname The user's last name.
 * @property dateOfBirth The user's date of birth, stored as a string.
 * @property role The role assigned to the user.
 * @property profilePictureUrl URL to the user's profile picture.
 * @property joinedAt Timestamp when the user joined the system.
 * @property phone The user's phone number.
 */
open class User(
    open val id: String,
    open val email: String,
    open val name: String,
    open val surname: String,
    open val dateOfBirth: String,
    open val role: Role,
    open val profilePictureUrl: String,
    open val joinedAt: Timestamp,
    open val phone: String,
) {

    /**
     * Creates a copy of this [User] with the option to override properties.
     *
     * @return A new [User] instance with updated values.
     */
    fun copy(
        id: String = this.id,
        email: String = this.email,
        name: String = this.name,
        surname: String = this.surname,
        dateOfBirth: String = this.dateOfBirth,
        role: Role = this.role,
        profilePictureUrl: String = this.profilePictureUrl,
        joinedAt: Timestamp = this.joinedAt,
        phone: String = this.phone
    ): User {
        return User(id, email, name, surname, dateOfBirth, role, profilePictureUrl, joinedAt, phone)
    }

    /**
     * Converts this [User] to a [Doctor] with additional doctor-specific information.
     *
     * @param speciality The doctor's speciality.
     * @param address The doctor's address.
     * @param consultationPrice The consultation fee.
     * @param rating The doctor's rating.
     * @param experience Years of experience.
     * @param languages List of languages the doctor speaks.
     * @param city The city where the doctor is located.
     * @param phone The doctor's phone number.
     * @param bio Biography or description of the doctor.
     * @return A [Doctor] object created from this user.
     */
    fun toDoctor(
        speciality: Speciality = Speciality.OTHER,
        address: String = "No address provided",
        consultationPrice: Int = 100,
        rating: Double = 4.5,
        experience: Int = 5,
        languages: List<String> = listOf("English"),
        city: String = "Unknown",
        phone: String = "Unknown",
        bio: String = "No bio provided"
    ): Doctor {
        return Doctor(
            id = this.id,
            email = this.email,
            name = this.name,
            surname = this.surname,
            dateOfBirth = this.dateOfBirth,
            role = Role.DOCTOR,
            profilePictureUrl = profilePictureUrl,
            speciality = speciality,
            address = address,
            consultationPrice = consultationPrice,
            rating = rating,
            experience = experience,
            languages = languages,
            city = city,
            phone = phone,
            bio = bio,
            joinedAt = this.joinedAt
        )
    }

    companion object {
        /**
         * Creates a [User] from a map of values, typically from a data source like Firestore.
         *
         * @param user A map containing user properties.
         * @return A [User] instance.
         */
        fun fromMap(user: Map<String, Any>): User {
            val id = user["id"] as? String? ?: ""
            val email = user["email"] as? String? ?: "Error"
            val name = user["name"] as? String? ?: "Error"
            val surname = user["surname"] as? String? ?: "Error"
            val dateOfBirth = user["dob"] as? String ?: ""
            val role = Role.fromValue(user["role"] as? String? ?: "user")
            val profilePictureUrl = user["profilePictureUrl"] as? String? ?: ""
            val joinedAt = user["joinedAt"] as? Timestamp ?: Timestamp.now()
            val phone = user["phone"] as? String? ?: "Unknown"

            return User(
                id,
                email,
                name,
                surname,
                dateOfBirth,
                role,
                profilePictureUrl,
                joinedAt,
                phone
            )
        }

        /**
         * Returns an empty default [User] instance.
         *
         * @return A [User] with default empty or placeholder values.
         */
        fun empty(): User {
            return User(
                id = "",
                email = "",
                name = "",
                surname = "",
                dateOfBirth = "",
                role = Role.USER,
                profilePictureUrl = "",
                joinedAt = Timestamp.now(),
                phone = ""
            )
        }
    }
}

/**
 * Represents the possible roles a user can have in the system.
 *
 * @property value The string representation of the role.
 */
enum class Role(val value: String) {
    DOCTOR("doctor"),
    USER("user"),
    DOCTOR_PENDING("doctor_pending"),
    ADMIN("admin");

    companion object {
        /**
         * Parses a string [value] to return the corresponding [Role].
         *
         * @param value The string value representing a role.
         * @return The matching [Role], or [USER] if none matches.
         */
        fun fromValue(value: String): Role {
            return Role.entries.firstOrNull { it.value == value } ?: USER
        }

    }
}




