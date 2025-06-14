package com.example.ladycure.repository

import android.util.Log
import com.example.ladycure.data.Appointment
import com.example.ladycure.data.doctor.Doctor
import com.example.ladycure.data.doctor.DoctorAvailability
import com.example.ladycure.screens.TimePeriod
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

class DoctorRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance("telecure")


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
                    DoctorAvailability(
                        doctorId = doctor.id,
                        date = LocalDate.parse(doc.id, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        startTime = LocalTime.parse(
                            doc.getString("startTime"),
                            DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)
                        ),
                        endTime = LocalTime.parse(
                            doc.getString("endTime"),
                            DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)
                        ),
                        availableSlots = (doc.get("availableSlots") as? List<String>)
                            ?.map { slot ->
                                LocalTime.parse(
                                    slot,
                                    DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)
                                )
                            }
                            ?.toMutableList() ?: mutableListOf()
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
                    DoctorAvailability(
                        doctorId = doctorId,
                        date = LocalDate.parse(doc.id, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        startTime = doc.getString("startTime")?.let {
                            LocalTime.parse(
                                it,
                                DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)
                            )
                        },
                        endTime = doc.getString("endTime")?.let {
                            LocalTime.parse(
                                it,
                                DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)
                            )
                        },
                        availableSlots = (doc.get("availableSlots") as? List<String>)
                            ?.map { slot ->
                                LocalTime.parse(
                                    slot,
                                    DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)
                                )
                            }
                            ?.toMutableList() ?: mutableListOf()
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

        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)

        return try {
            dates.forEach { date ->
                val docRef = firestore.collection("users")
                    .document(doctorId)
                    .collection("availability")
                    .document(date.toString())

                // Get existing document
                val existingDoc = docRef.get().await()

                if (existingDoc.exists()) {
                    // Get existing available slots
                    val existingAvailableSlots =
                        (existingDoc.get("availableSlots") as? List<String>)
                            ?.mapNotNull { timeString ->
                                try {
                                    LocalTime.parse(timeString, timeFormatter)
                                } catch (e: Exception) {
                                    null
                                }
                            }?.toSet() ?: emptySet()

                    // Get existing time range
                    val existingStartTime = (existingDoc.getString("startTime")?.let {
                        LocalTime.parse(it, timeFormatter)
                    }) ?: LocalTime.MIN

                    val existingEndTime = (existingDoc.getString("endTime")?.let {
                        LocalTime.parse(it, timeFormatter)
                    }) ?: LocalTime.MAX

                    // Generate all possible slots from existing time range
                    val allExistingSlots = mutableSetOf<LocalTime>()
                    var existingCurrentTime = existingStartTime
                    while (existingCurrentTime.isBefore(existingEndTime)) {
                        allExistingSlots.add(existingCurrentTime)
                        existingCurrentTime = existingCurrentTime.plus(15, ChronoUnit.MINUTES)
                    }

                    // Calculate booked slots (slots that were in the full range but not available)
                    val bookedSlots = allExistingSlots - existingAvailableSlots

                    // Merge slots: new slots + existing available slots, minus any that overlap with booked slots
                    val mergedSlots = (newSlots + existingAvailableSlots).toMutableSet()
                    mergedSlots.removeAll(bookedSlots)

                    // Update the document
                    val availabilityData = hashMapOf(
                        "doctorId" to doctorId,
                        "startTime" to startTime.format(timeFormatter),
                        "endTime" to endTime.format(timeFormatter),
                        "availableSlots" to mergedSlots.map { it.format(timeFormatter) }
                    )

                    batch.set(docRef, availabilityData, SetOptions.merge())
                } else {
                    // No existing document - just create new availability
                    val availabilityData = hashMapOf(
                        "doctorId" to doctorId,
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


    suspend fun deleteDoctorAvailability(
        doctorId: String,
        date: LocalDate
    ): Result<Unit> {
        return try {
            val docRef = firestore.collection("users")
                .document(doctorId)
                .collection("availability")
                .document(date.toString())

            docRef.delete().await()
            Result.success(Unit)
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
                .whereEqualTo("status", Appointment.Status.CONFIRMED.displayName)
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
                    // Parse date string (format: "yyyy-MM-dd")
                    val dateStr = appointment.getString("date") ?: "1970-01-01"
                    val date = try {
                        LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    } catch (e: Exception) {
                        LocalDate.of(1970, 1, 1)
                    }

                    // Group by time period
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
                .whereEqualTo("status", Appointment.Status.CONFIRMED.displayName)
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
                .whereEqualTo("status", Appointment.Status.CONFIRMED.displayName)
                .get()
                .await()

            val totalEarnings = appointmentsSnapshot.documents
                .sumOf { it.getDouble("price") ?: 0.0 }

            val thisMonth = LocalDate.now().withDayOfMonth(1)
            val thisMonthEarnings = appointmentsSnapshot.documents
                .filter { doc ->
                    val dateStr = doc.getString("date") ?: "1970-01-01"
                    val date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
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
                .whereEqualTo("status", Appointment.Status.CONFIRMED.displayName)
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
}