package com.example.ladycure.repository

import android.util.Log
import androidx.navigation.NavController
import com.example.ladycure.data.Appointment
import com.example.ladycure.data.doctor.DoctorAvailability
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance("telecure")

    suspend fun updateProfilePicture(imageUrl: String): Result<Unit> = try {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
        firestore.collection("users").document(currentUser.uid)
            .update("profilePictureUrl", imageUrl)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getUserRole(): Result<String?> = try {
        val user = auth.currentUser
        if (user != null) {
            val document = firestore.collection("users").document(user.uid).get().await()
            if (document.exists()) {
                Result.success(document.getString("role"))
            } else {
                Result.failure(Exception("User document does not exist"))
            }
        } else {
            Result.failure(Exception("User not logged in"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Failed to fetch user data: ${e.message}"))
    }

    fun authenticate(
        email: String,
        password: String,
        navController: NavController,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        firestore.collection("users").document(it.uid).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val role = document.getString("role")
                                    when (role) {
                                        "admin" -> navController.navigate("admin")
                                        "doctor" -> navController.navigate("doctor_main")
                                        else -> navController.navigate("home")
                                    }
                                    onSuccess()
                                } else {
                                    onFailure(Exception("User document does not exist"))
                                }
                            }
                            .addOnFailureListener { e ->
                                onFailure(Exception("Failed to fetch user data: ${e.message}"))
                            }
                    }
                } else {
                    onFailure(Exception("Authentication failed: ${task.exception?.message}"))
                }
            }
            .addOnFailureListener { e ->
                onFailure(Exception("Authentication failed: ${e.message}"))
            }
    }


    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun register(
        email: String,
        name: String,
        surname: String,
        dateOfBirth: String,
        password: String
    ): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User registration failed")

            val userData = hashMapOf(
                "email" to email,
                "name" to name,
                "surname" to surname,
                "dob" to dateOfBirth,
                "role" to "user"
            )
            firestore.collection("users").document(user.uid).set(userData)
                .addOnSuccessListener { }
                .addOnFailureListener { throw Exception("User registration failed") }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getDoctors(): Result<List<Map<String, Any>>> {
        return try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("role", "doctor")
                .get()
                .await()
            val doctors = querySnapshot.documents.map { it.data ?: emptyMap() }
            Result.success(doctors)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error fetching doctors", e)
            Result.failure(e)
        }
    }


    suspend fun getCurrentUserData(): Result<Map<String, Any>?> {
        val user = auth.currentUser
        user?.let {
            try {
                val documentSnapshot = firestore.collection("users").document(it.uid).get().await()
                val data = documentSnapshot.data
                if (data != null) {
                    return Result.success(data)
                } else {
                    return Result.failure(Exception("User document does not exist"))
                }
            } catch (e: Exception) {
                return Result.failure(Exception("Failed to fetch user data: ${e.message}"))
            }
        } ?: return Result.failure(Exception("User not logged in"))
    }

    suspend fun getCurrentUser(): Result<Map<String, Any>?> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
            val document = firestore.collection("users").document(user.uid).get().await()
            if (document.exists()) {
                Result.success(document.data)
            } else {
                Result.failure(Exception("User document does not exist"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    }


    suspend fun getDoctorsBySpecification(specification: String): Result<List<Map<String, Any>>> {
        return try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("role", "doctor")
                .whereEqualTo("specification", specification)
                .get()
                .await()
            val doctors =
                querySnapshot.documents.map { it.data?.plus("id" to it.id) ?: mapOf("id" to it.id) }
            Result.success(doctors)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserData(updatedData: Map<String, String>): Result<Map<String, Any>?> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("User not logged in"))

            // Create a map with only the fields we want to update
            val updateMap = mutableMapOf<String, Any>()
            updatedData["name"]?.let { updateMap["name"] = it }
            updatedData["surname"]?.let { updateMap["surname"] = it }
            updatedData["dob"]?.let { updateMap["dob"] = it }

            // Update Firestore document
            try {
                firestore.collection("users").document(user.uid)
                    .update(updateMap)
                    .await()
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error updating user data", e)
                return Result.failure(e)
            }
            // Fetch the updated document
            val document = firestore.collection("users").document(user.uid).get().await()
            if (document.exists()) {
                Result.success(document.data)
            } else {
                Result.failure(Exception("User document does not exist"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

     fun signOut() {
        Firebase.auth.signOut()
    }


    suspend fun getUserField(fieldName: String): Result<String?> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
            val document = firestore.collection("users").document(currentUser.uid).get().await()
            Result.success(document.getString(fieldName))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getAllDoctorAvailabilitiesBySpeciality(speciality: String, city: String): List<DoctorAvailability> {
        val doctors = firestore.collection("users")
            .whereEqualTo("role", "doctor")
            .whereEqualTo("city", city)
            .whereEqualTo("specification", speciality)
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
                        date = LocalDate.parse(doc.id,DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        startTime = LocalTime.parse(doc.getString("startTime"), DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)),
                        endTime = LocalTime.parse(doc.getString("endTime"), DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)),
                        availableSlots = (doc.get("availableSlots") as? List<String>)
                            ?.map { slot -> LocalTime.parse(slot, DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)) }
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
                        date = LocalDate.parse(doc.id,DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        startTime = doc.getString("startTime")?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)) },
                        endTime = doc.getString("endTime")?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)) },
                        availableSlots = (doc.get("availableSlots") as? List<String>)
                            ?.map { slot -> LocalTime.parse(slot, DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)) }
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
        val doctorId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

        val availableSlots = mutableSetOf<LocalTime>()
        var currentTime = startTime
        while (currentTime.isBefore(endTime)) {
            availableSlots.add(currentTime)
            currentTime = currentTime.plus(15, ChronoUnit.MINUTES)
        }

        dates.forEach { date ->
            val docRef = firestore.collection("users")
                .document(doctorId)
                .collection("availability")
                .document(date.toString())

            val availabilityData = hashMapOf(
                "doctorId" to doctorId,
                "startTime" to startTime.format(DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)),
                "endTime" to endTime.format(DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)),
                "availableSlots" to availableSlots.map { it.format(DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)) }
            )

            batch.set(docRef, availabilityData)
        }

        try {
            batch.commit().await()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    suspend fun getDoctorById(doctorId: String): Result<Map<String, Any>?> {
        return try {
            val document = firestore.collection("users").document(doctorId).get().await()
            if (document.exists()) {
                Result.success(document.data)
            } else {
                Result.failure(Exception("Doctor not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun bookAppointment(
        appointment: Appointment): Result<String> {
        try {
            val appointmentData = appointment.toMap(appointment).filterKeys { it != "appointmentId" }

            val documentReference = firestore.collection("appointments").add(appointmentData).await()
            val appointmentId = documentReference.id

            // make the timeslot unavailable

            val doctorId = appointment.doctorId
            val date = appointment.date
            val startTime = appointment.time
            val endTime = startTime.plus(appointment.type.durationInMinutes.toLong(), ChronoUnit.MINUTES)

            // Fetch the current available slots
            val docRef = firestore.collection("users")
                .document(doctorId)
                .collection("availability")
                .document(date.toString())

            val availabilitySnapshot = docRef.get().await()
            if (availabilitySnapshot.exists()) {
                val availableSlots = availabilitySnapshot.get("availableSlots") as? List<String> ?: emptyList()
                val updatedAvailableSlots = availableSlots.filterNot { slot ->
                    val slotTime = LocalTime.parse(slot, DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US))
                    (slotTime.isAfter(startTime) && slotTime.isBefore(endTime)) || slotTime == startTime
                }
                docRef.update("availableSlots", updatedAvailableSlots).await()
            } else {
                return Result.failure(Exception("Availability document does not exist"))
            }

            return Result.success(appointmentId)
        } catch (e: Exception) {
           return Result.failure(e)
        }
    }

    suspend fun getAppointmentById(id: String): Result<Appointment> {
        return try {
            val document = firestore.collection("appointments").document(id).get().await()
            if (document.exists()) {
                val appointment = Appointment.fromMap(document.data?.plus("appointmentId" to id) ?: mapOf("appointmentId" to id))
                Result.success(appointment)
            } else {
                Result.failure(Exception("Appointment not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAppointments(role: String): Result<List<Appointment>> {
        return try {
            val id = if (role == "doctor") { "doctorId"} else {"patientId"}
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val querySnapshot = firestore.collection("appointments")
                .whereEqualTo(id, userId)
                .get()
                .await()

            val appointments = querySnapshot.documents.map { doc ->
                Appointment.fromMap(doc.data?.plus("appointmentId" to doc.id) ?: mapOf("appointmentId" to doc.id))
            }
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAppointmentStatus(appointmentId: String, status: String): Result<Unit> {
        return try {
            val appointmentRef = firestore.collection("appointments").document(appointmentId)
            appointmentRef.update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAppointmentComment(appointmentId: String, comment: String): Result<Unit> {
        return try {
            val appointmentRef = firestore.collection("appointments").document(appointmentId)
            appointmentRef.update("comments", comment).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}