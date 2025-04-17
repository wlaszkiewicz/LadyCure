package com.example.ladycure.data

open class User(
   open val email: String,
    open val name: String,
    open val surname: String,
  open  val dateOfBirth: String,
  open  val role : Role,
  open  val profilePictureUrl: String,
    //for doctors only
//    val specialization: Specialization,
//    val availability: List<Availability>,
//    val reviews: List<Review>,
//    val address: String,
//    val consultationPrice: String,
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




