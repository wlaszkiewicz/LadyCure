package com.example.ladycure.domain.model

import java.security.Timestamp

/**
 * Represents a review left by a patient for a doctor or service.
 *
 * @property description The text content of the review.
 * @property patientID The unique identifier of the patient who wrote the review.
 * @property rating The rating score given by the patient, typically on a scale (e.g., 1.0 to 5.0).
 * @property timestamp The time when the review was created.
 */
data class Review(
    val description: String,
    val patientID: String,
    val rating: Double,
    val timestamp: Timestamp,
)
