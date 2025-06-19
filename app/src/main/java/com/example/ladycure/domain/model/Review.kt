package com.example.ladycure.domain.model

import java.security.Timestamp

data class Review(
    val description: String,
    val patientID: String,
    val rating: Double,
    val timestamp: Timestamp,
)
