package com.example.ladycure.domain.model

data class AdminStats(
    val totalUsers: Int = 0,
    val activeDoctors: Int = 0,
    val pendingApplications: Int = 0
) {
    companion object {
        fun fromMap(map: Map<String, Any>): AdminStats = AdminStats(
            totalUsers = (map["totalUsers"] as? Number)?.toInt() ?: 0,
            activeDoctors = (map["activeDoctors"] as? Number)?.toInt() ?: 0,
            pendingApplications = (map["pendingApplications"] as? Number)?.toInt() ?: 0
        )
    }

    fun toMap(): Map<String, Any> = mapOf(
        "totalUsers" to totalUsers,
        "activeDoctors" to activeDoctors,
        "pendingApplications" to pendingApplications
    )
}
