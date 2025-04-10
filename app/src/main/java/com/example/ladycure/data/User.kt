package com.example.ladycure.data

import com.example.ladycure.data.doctor.Availability
import com.example.ladycure.data.doctor.Review

data class User(
    val email: String,
    val name: String,
    val surname: String,
    val dateOfBirth: String,
    val role : String,
    val profilePictureUrl: String,
    //for doctors only
    val specification: String,
    val availability: List<Availability>,
    val reviews: List<Review>,
    val address: String,
    val consultationPrice: String,
)
