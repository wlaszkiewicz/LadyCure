package com.example.ladycure.data.repository

import android.util.Log
import com.example.ladycure.domain.model.Role
import com.example.ladycure.presentation.admin.TimePeriod
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

/**
 * Repository class for fetching admin-related data from Firestore.
 */
class AdminRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()


    /**
     * Retrieves user registration data grouped by the given [timePeriod].
     *
     * @param timePeriod The time interval to group data (e.g., daily, weekly).
     * @return A [Result] containing a list of pairs (timeLabel, userCount).
     */
    suspend fun getUserGrowthData(timePeriod: TimePeriod): Result<List<Pair<String, Int>>> {
        return try {
            val snapshot = firestore
                .collection("users")
                .get()
                .await()

            val formatter = when (timePeriod) {
                TimePeriod.DAILY -> DateTimeFormatter.ofPattern("MMM dd")
                TimePeriod.WEEKLY -> DateTimeFormatter.ofPattern("yy 'W'ww")
                TimePeriod.MONTHLY -> DateTimeFormatter.ofPattern("MMM yyyy")
                TimePeriod.YEARLY -> DateTimeFormatter.ofPattern("yyyy")
            }

            val grouped = snapshot.documents
                .groupBy { user ->
                    val timestamp = user.getTimestamp("joinedAt")?.toDate()?.toInstant()
                        ?: Instant.ofEpochMilli(0)

                    Instant.ofEpochMilli(timestamp.toEpochMilli())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .let { date ->
                            when (timePeriod) {
                                TimePeriod.DAILY -> date
                                TimePeriod.WEEKLY -> date.with(
                                    TemporalAdjusters.previousOrSame(
                                        DayOfWeek.MONDAY
                                    )
                                )

                                TimePeriod.MONTHLY -> date.withDayOfMonth(1)
                                TimePeriod.YEARLY -> date.withDayOfYear(1)
                            }.format(formatter)
                        }
                }
                .mapValues { it.value.size }
                .toList()
                .sortedBy { it.first }

            Result.success(grouped)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves patient registration data grouped by the given [timePeriod].
     * Only includes users with the role "USER".
     *
     * @param timePeriod The time interval to group data.
     * @return A [Result] with a list of (timeLabel, patientCount) pairs.
     */
    suspend fun getPatientGrowthData(timePeriod: TimePeriod): Result<List<Pair<String, Int>>> {
        return try {
            val snapshot = firestore
                .collection("users")
                .whereEqualTo("role", Role.USER.value)
                .get()
                .await()

            val formatter = when (timePeriod) {
                TimePeriod.DAILY -> DateTimeFormatter.ofPattern("MMM dd")
                TimePeriod.WEEKLY -> DateTimeFormatter.ofPattern("yy 'W'ww")
                TimePeriod.MONTHLY -> DateTimeFormatter.ofPattern("MMM yyyy")
                TimePeriod.YEARLY -> DateTimeFormatter.ofPattern("yyyy")
            }

            val grouped = snapshot.documents
                .groupBy { doc ->
                    val timestamp = doc.getTimestamp("joinedAt")?.toDate()?.toInstant()
                        ?: Instant.ofEpochMilli(0)

                    Instant.ofEpochMilli(timestamp.toEpochMilli())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .let { date ->
                            when (timePeriod) {
                                TimePeriod.DAILY -> date
                                TimePeriod.WEEKLY -> date.with(
                                    TemporalAdjusters.previousOrSame(
                                        DayOfWeek.MONDAY
                                    )
                                )

                                TimePeriod.MONTHLY -> date.withDayOfMonth(1)
                                TimePeriod.YEARLY -> date.withDayOfYear(1)
                            }.format(formatter)
                        }
                }
                .mapValues { it.value.size }
                .toList()
                .sortedBy { it.first }

            Result.success(grouped)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /**
     * Retrieves doctor registration data grouped by the specified [timePeriod].
     * Only includes users with the role "DOCTOR".
     *
     * @param timePeriod The time interval to group data.
     * @return A [Result] containing a list of (timeLabel, doctorCount) pairs.
     */
    suspend fun getDoctorGrowthData(timePeriod: TimePeriod): Result<List<Pair<String, Int>>> {
        return try {
            val snapshot = firestore
                .collection("users")
                .whereEqualTo("role", Role.DOCTOR.value)
                .get()
                .await()

            val formatter = when (timePeriod) {
                TimePeriod.DAILY -> DateTimeFormatter.ofPattern("MMM dd")
                TimePeriod.WEEKLY -> DateTimeFormatter.ofPattern("yy 'W'ww")
                TimePeriod.MONTHLY -> DateTimeFormatter.ofPattern("MMM yyyy")
                TimePeriod.YEARLY -> DateTimeFormatter.ofPattern("yyyy")
            }

            val grouped = snapshot.documents
                .groupBy { doc ->
                    val timestamp = doc.getTimestamp("joinedAt")?.toDate()?.toInstant()
                        ?: Instant.ofEpochMilli(0)

                    Instant.ofEpochMilli(timestamp.toEpochMilli())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .let { date ->
                            when (timePeriod) {
                                TimePeriod.DAILY -> date
                                TimePeriod.WEEKLY -> date.with(
                                    TemporalAdjusters.previousOrSame(
                                        DayOfWeek.MONDAY
                                    )
                                )

                                TimePeriod.MONTHLY -> date.withDayOfMonth(1)
                                TimePeriod.YEARLY -> date.withDayOfYear(1)
                            }.format(formatter)
                        }
                }
                .mapValues { it.value.size }
                .toList()
                .sortedBy { it.first }

            Result.success(grouped)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves the number of applications grouped by their status (e.g., PENDING, APPROVED).
     *
     * @return A [Result] containing a map of (status -> count).
     */
    suspend fun getApplicationStats(): Result<Map<String, Int>> {
        return try {
            val snapshot = firestore
                .collection("applications")
                .get()
                .await()

            val stats = snapshot.documents
                .groupingBy { doc -> doc.getString("status") ?: "PENDING" }
                .eachCount()

            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves high-level admin statistics such as total users, active doctors, and pending applications.
     *
     * @return A [Result] with a map of stat names to their corresponding values.
     */
    suspend fun getAdminStats(): Result<Map<String, Any>> {
        return try {
            val stats = mutableMapOf<String, Any>()

            val usersQuery = firestore.collection("users").get().await()
            stats["totalUsers"] = usersQuery.size()

            val doctorsQuery = firestore.collection("users")
                .whereEqualTo("role", "doctor")
                .get()
                .await()
            stats["activeDoctors"] = doctorsQuery.size()

            val pendingAppsQuery = firestore.collection("applications")
                .whereEqualTo("status", "pending")
                .get()
                .await()
            stats["pendingApplications"] = pendingAppsQuery.size()

            Result.success(stats)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error fetching admin stats: ${e.message}")
            Result.failure(e)
        }
    }


    /**
     * Retrieves users' age distribution grouped into age ranges (e.g., "21-30").
     *
     * @return A [Result] containing a list of (ageRange, userCount) pairs.
     */
    suspend fun getUsersAgeData(): Result<List<Pair<String, Int>>> {
        return try {
            val snapshot = firestore
                .collection("users")
                .get()
                .await()

            val currentDate = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()

            val ageGroups = snapshot.documents
                .mapNotNull { doc ->
                    val dobString = doc.getString("dob")
                    if (dobString != null) {
                        val dob =
                            LocalDate.parse(dobString)
                        val age = currentDate.year - dob.year
                        if (currentDate.monthValue < dob.monthValue ||
                            (currentDate.monthValue == dob.monthValue && currentDate.dayOfMonth < dob.dayOfMonth)
                        ) {
                            age - 1
                        } else {
                            age
                        }
                    } else {
                        null
                    }
                }
                .groupBy { age ->
                    when (age) {
                        in 18..20 -> "18-20"
                        in 21..30 -> "21-30"
                        in 31..40 -> "31-40"
                        in 41..50 -> "41-50"
                        in 51..60 -> "51-60"
                        in 61..70 -> "61-70"
                        in 71..80 -> "71-80"
                        in 81..90 -> "81-90"
                        else -> "90+"
                    }
                }
                .mapValues { it.value.size }
                .toList()
                .sortedBy { it.first }

            Result.success(ageGroups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}