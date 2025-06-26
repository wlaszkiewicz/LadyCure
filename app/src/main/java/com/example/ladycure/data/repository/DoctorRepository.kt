package com.example.ladycure.data.repository

import android.util.Log
import com.example.ladycure.domain.model.Appointment
import com.example.ladycure.domain.model.Doctor
import com.example.ladycure.domain.model.DoctorAvailability
import com.example.ladycure.presentation.admin.TimePeriod
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class DoctorRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()


    suspend fun getDoctors(): Result<List<Doctor>> {
        return try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("role", "doctor")
                .get()
                .await()
            val doctors =
                querySnapshot.documents.map { Doctor.fromMap(it.data!!.plus("id" to it.id)) }
            Result.success(doctors)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error fetching doctors", e)
            Result.failure(e)
        }
    }


    suspend fun getAllDoctorAvailabilitiesBySpeciality(
        speciality: String,
        city: String
    ): List<DoctorAvailability> {
        val doctors = firestore.collection("users")
            .whereEqualTo("role", "doctor")
            .whereEqualTo("city", city)
            .whereEqualTo("speciality", speciality)
            .get()
            .await()
            .documents

        val allAvailabilities = mutableListOf<DoctorAvailability>()

        for (doctor in doctors) {
            val availabilities = doctor.reference.collection("availability")
                .get()
                .await()
                .documents
                .map { doc ->
                    DoctorAvailability.fromMap(
                        doc.data ?: emptyMap(),
                        doctorId = doctor.id,
                        documentId = doc.id
                    )
                }
            allAvailabilities.addAll(availabilities)
        }
        return allAvailabilities
    }


    suspend fun getDoctorAvailability(doctorId: String): Result<List<DoctorAvailability>> {
        return try {
            val snapshot = firestore.collection("users")
                .document(doctorId)
                .collection("availability")
                .get()
                .await()

            val availabilities = snapshot.documents.mapNotNull { doc ->
                try {
                    DoctorAvailability.fromMap(
                        doc.data ?: emptyMap(),
                        doctorId = doctorId,
                        documentId = doc.id
                    )
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(availabilities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAvailabilities(
        dates: List<LocalDate>,
        startTime: LocalTime,
        endTime: LocalTime
    ): Result<Unit> {
        val batch = firestore.batch()
        val doctorId =
            auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

        // Generate all possible slots for the new time range
        val newSlots = mutableSetOf<LocalTime>()
        var currentTime = startTime
        while (currentTime.isBefore(endTime)) {
            newSlots.add(currentTime)
            currentTime = currentTime.plus(15, ChronoUnit.MINUTES)
        }

        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

        return try {
            dates.forEach { date ->
                val docRef = firestore.collection("users")
                    .document(doctorId)
                    .collection("availability")
                    .document(date.toString())

                // Get existing document
                val existingDoc = docRef.get().await()

                if (existingDoc.exists()) {
                    val existingAvailableSlots =
                        (existingDoc.get("availableSlots") as? List<String>)
                            ?.mapNotNull { timeString ->
                                try {
                                    LocalTime.parse(timeString, timeFormatter)
                                } catch (e: Exception) {
                                    null
                                }
                            }?.toSet() ?: emptySet()

                    val existingStartTime = (existingDoc.getString("startTime")?.let {
                        LocalTime.parse(it, timeFormatter)
                    }) ?: LocalTime.MIN

                    val existingEndTime = (existingDoc.getString("endTime")?.let {
                        LocalTime.parse(it, timeFormatter)
                    }) ?: LocalTime.MAX

                    val allExistingSlots = mutableSetOf<LocalTime>()
                    var existingCurrentTime = existingStartTime
                    while (existingCurrentTime.isBefore(existingEndTime)) {
                        allExistingSlots.add(existingCurrentTime)
                        existingCurrentTime = existingCurrentTime.plus(15, ChronoUnit.MINUTES)
                    }

                    val bookedSlots = allExistingSlots - existingAvailableSlots

                    val mergedSlots = (newSlots + existingAvailableSlots).toMutableSet()
                    mergedSlots.removeAll(bookedSlots)

                    val availabilityData = hashMapOf(
                        "startTime" to startTime.format(timeFormatter),
                        "endTime" to endTime.format(timeFormatter),
                        "availableSlots" to mergedSlots.map { it.format(timeFormatter) }
                    )

                    batch.set(docRef, availabilityData, SetOptions.merge())
                } else {
                    val availabilityData = hashMapOf(
                        "startTime" to startTime.format(timeFormatter),
                        "endTime" to endTime.format(timeFormatter),
                        "availableSlots" to newSlots.map { it.format(timeFormatter) }
                    )

                    batch.set(docRef, availabilityData)
                }
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDoctorById(doctorId: String): Result<Doctor> {
        return try {
            val document = firestore.collection("users").document(doctorId).get().await()
            if (document.exists()) {
                val doctor = Doctor.fromMap(
                    document.data?.plus("id" to doctorId) ?: mapOf("id" to doctorId)
                )
                Result.success(doctor)
            } else {
                Result.failure(Exception("Doctor not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getDoctorsBySpeciality(speciality: String): Result<List<Doctor>> {
        return try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("role", "doctor")
                .whereEqualTo("speciality", speciality)
                .get()
                .await()
            val doctors = querySnapshot.documents.mapNotNull { doc ->
                try {
                    val doctor = Doctor.fromMap(doc.data!!.plus("id" to doc.id))
                    doctor
                } catch (e: Exception) {
                    return Result.failure(e)
                }
            }
            Result.success(doctors)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getEarningsData(timePeriod: TimePeriod): Result<List<Pair<String, Double>>> {
        return try {
            val currentDoctorId =
                auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            val appointmentsSnapshot = firestore.collection("appointments")
                .whereEqualTo("doctorId", currentDoctorId)
                .whereIn(
                    "status",
                    listOf(
                        Appointment.Status.CONFIRMED.displayName,
                        Appointment.Status.COMPLETED.displayName
                    )
                )
                .get()
                .await()

            val formatter = when (timePeriod) {
                TimePeriod.DAILY -> DateTimeFormatter.ofPattern("MMM dd")
                TimePeriod.WEEKLY -> DateTimeFormatter.ofPattern("yy 'W'ww")
                TimePeriod.MONTHLY -> DateTimeFormatter.ofPattern("MMM yyyy")
                TimePeriod.YEARLY -> DateTimeFormatter.ofPattern("yyyy")
            }

            val grouped = appointmentsSnapshot.documents
                .groupBy { appointment ->
                    val date = appointment.getTimestamp("dateTime")?.toDate()?.toInstant()
                        ?.atZone(java.time.ZoneId.systemDefault())
                        ?.toLocalDate()
                        ?: LocalDate.now()


                    when (timePeriod) {
                        TimePeriod.DAILY -> date.format(DateTimeFormatter.ofPattern("MMM dd"))
                        TimePeriod.WEEKLY -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                            .format(DateTimeFormatter.ofPattern("yy 'W'ww"))

                        TimePeriod.MONTHLY -> date.withDayOfMonth(1)
                            .format(DateTimeFormatter.ofPattern("MMM yyyy"))

                        TimePeriod.YEARLY -> date.withDayOfYear(1)
                            .format(DateTimeFormatter.ofPattern("yyyy"))
                    }
                }
                .mapValues { entry ->
                    entry.value.sumOf { it.getDouble("price") ?: 0.0 }
                }
                .toList()
                .sortedBy { it.first }

            Result.success(grouped)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEarningsByAppointmentType(): Result<Map<String, Double>> {
        return try {
            val currentDoctorId =
                auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            val appointmentsSnapshot = firestore.collection("appointments")
                .whereEqualTo("doctorId", currentDoctorId)
                .whereIn(
                    "status",
                    listOf(
                        Appointment.Status.CONFIRMED.displayName,
                        Appointment.Status.COMPLETED.displayName
                    )
                )
                .get()
                .await()

            val earningsByType = appointmentsSnapshot.documents
                .groupingBy { it.getString("type") ?: "Other" }
                .fold(0.0) { acc, doc -> acc + (doc.getDouble("price") ?: 0.0) }

            Result.success(earningsByType)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEarningsStats(): Result<Map<String, Any>> {
        return try {
            val currentDoctorId =
                auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            val appointmentsSnapshot = firestore.collection("appointments")
                .whereEqualTo("doctorId", currentDoctorId)
                .whereIn(
                    "status",
                    listOf(
                        Appointment.Status.CONFIRMED.displayName,
                        Appointment.Status.COMPLETED.displayName
                    )
                )
                .get()
                .await()

            val totalEarnings = appointmentsSnapshot.documents
                .sumOf { it.getDouble("price") ?: 0.0 }

            val thisMonth = LocalDate.now().withDayOfMonth(1)
            val thisMonthEarnings = appointmentsSnapshot.documents
                .filter { doc ->
                    val timestamp = doc.getTimestamp("dateTime")
                    val date =
                        timestamp?.toDate()?.toInstant()?.atZone(java.time.ZoneId.systemDefault())
                            ?.toLocalDate() ?: LocalDate.MIN
                    date.isAfter(thisMonth.minusDays(1)) && date.isBefore(
                        LocalDate.now().plusDays(1)
                    )
                }
                .sumOf { it.getDouble("price") ?: 0.0 }

            val stats = mapOf(
                "totalEarnings" to totalEarnings,
                "totalAppointments" to appointmentsSnapshot.size(),
                "thisMonthEarnings" to thisMonthEarnings
            )

            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMostPopularAppointmentType(): Result<Pair<String, Int>> {
        return try {
            val currentDoctorId =
                auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            val appointmentsSnapshot = firestore.collection("appointments")
                .whereEqualTo("doctorId", currentDoctorId)
                .whereIn(
                    "status",
                    listOf(
                        Appointment.Status.CONFIRMED.displayName,
                        Appointment.Status.COMPLETED.displayName
                    )
                )
                .get()
                .await()

            val typeCounts = appointmentsSnapshot.documents
                .groupingBy { it.getString("type") ?: "Other" }
                .eachCount()

            val mostPopular = typeCounts.maxByOrNull { it.value }
                ?.let { it.key to it.value }
                ?: ("None" to 0)

            Result.success(mostPopular)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvailableCities(): Result<List<String>> {
        return try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("role", "doctor")
                .get()
                .await()

            val cities = querySnapshot.documents.mapNotNull { doc ->
                doc.getString("city")
            }.distinct()

            Result.success(cities)
        } catch (e: Exception) {
            Log.e("DoctorRepository", "Error fetching cities", e)
            Result.failure(e)
        }
    }

    suspend fun getCurrentDoctorData(): Result<Map<String, Any>?> {
        val user = auth.currentUser
        user?.let {
            return try {
                val documentSnapshot = firestore.collection("users").document(it.uid).get().await()
                val data = documentSnapshot.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Doctor document does not exist"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Failed to fetch doctor data: ${e.message}"))
            }
        } ?: return Result.failure(Exception("Doctor not logged in"))
    }

    suspend fun updateDoctorProfile(data: Map<String, Any>): Result<Unit> {
        val user = auth.currentUser
        return if (user != null) {
            try {
                val doctorRef = firestore.collection("users").document(user.uid)
                doctorRef.set(data, SetOptions.merge()).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("You are not logged in as a doctor"))
        }
    }

}