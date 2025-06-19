package com.example.ladycure.domain.model

data class Referral(
    val id: String = "",
    val url: String = "",
    val service: String = "",
    val uploadedAt: Long = 0L,
    val patientId: String = "",
) {
    companion object {
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