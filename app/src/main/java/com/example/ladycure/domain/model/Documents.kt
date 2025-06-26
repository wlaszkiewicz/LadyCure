package com.example.ladycure.domain.model

/**
 * Represents a medical referral document related to a patient.
 *
 * @property id The unique identifier for the referral.
 * @property url The URL pointing to the referral document.
 * @property service The medical service or department that issued the referral.
 * @property uploadedAt The timestamp (in milliseconds since epoch) when the referral was uploaded.
 * @property patientId The unique identifier of the patient to whom this referral belongs.
 */
data class Referral(
    val id: String = "",
    val url: String = "",
    val service: String = "",
    val uploadedAt: Long = 0L,
    val patientId: String = "",
) {
    companion object {
        /**
         * Creates a [Referral] instance from a map of key-value pairs.
         *
         * Expects the map to contain the keys "url", "service", "uploadedAt", and "patientId"
         * with appropriate types.
         *
         * @param map A map containing referral data.
         * @return A [Referral] object populated with the data from the map.
         * @throws ClassCastException if the map contains incorrect types.
         */
        fun fromMap(map: Map<String, Any?>): Referral {
            return Referral(
                url = map["url"] as String,
                service = map["service"] as String,
                uploadedAt = (map["uploadedAt"] as Number).toLong(),
                patientId = map["patientId"] as String
            )
        }
    }
}