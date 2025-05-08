package com.example.ladycure.repository

import android.util.Log
import androidx.navigation.NavController
import com.example.ladycure.data.Appointment
import com.example.ladycure.data.doctor.Doctor
import com.example.ladycure.data.doctor.DoctorAvailability
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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

//    suspend fun deleteUser(userId: String): Result<Unit> {
//        return try {
//            firestore.collection("users").document(userId).delete().await()
//            // delete from auth
//            val user = auth.getUser(userId)
//            user?.delete()?.await()
//                ?: return Result.failure(Exception("User not logged in"))
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Log.e("AuthRepository", "Error deleting user", e)
//            Result.failure(e)
//        }
//    }

    suspend fun updateUser(userId: String, updatedData: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection("users").document(userId).update(updatedData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error updating user data", e)
            Result.failure(e)
        }
    }

    suspend fun docToUserUpdate(
        userId: String,
        updatedData: Map<String, Any>
    ): Result<Unit> {
        return try {

            val documentRef = firestore.collection("users").document(userId)

            val fieldsToDelete = hashMapOf<String, Any>(
                "address" to FieldValue.delete(),
                "consultationPrice" to FieldValue.delete(),
                "experience" to FieldValue.delete(),
                "languages" to FieldValue.delete(),
                "phoneNumber" to FieldValue.delete(),
                "speciality" to FieldValue.delete(),
                "city" to FieldValue.delete(),
                "bio" to FieldValue.delete(),
                "rating" to FieldValue.delete(),
                "speciality" to FieldValue.delete(),
            )

            firestore.runTransaction { transaction ->
                transaction.get(documentRef)

                transaction.update(documentRef, fieldsToDelete)

                transaction.update(documentRef, updatedData)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error updating user data", e)
            Result.failure(e)
        }
    }

    suspend fun getUsers(): Result<List<Map<String, Any>>> {
        return try {
            val querySnapshot = firestore.collection("users").get().await()
            val users = querySnapshot.documents.map { it.data?.plus("id" to it.id) ?: emptyMap() }
            Result.success(users)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error fetching users", e)
            Result.failure(e)
        }
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
            return try {
                val documentSnapshot = firestore.collection("users").document(it.uid).get().await()
                val data = documentSnapshot.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("User document does not exist"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Failed to fetch user data: ${e.message}"))
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
            val currentUser =
                auth.currentUser ?: return Result.failure(Exception("User not logged in"))
            val document = firestore.collection("users").document(currentUser.uid).get().await()
            Result.success(document.getString(fieldName))
        } catch (e: Exception) {
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

    suspend fun bookAppointment(
        appointment: Appointment
    ): Result<String> {
        try {
            val appointmentData =
                appointment.toMap(appointment).filterKeys { it != "appointmentId" }

            val documentReference =
                firestore.collection("appointments").add(appointmentData).await()
            val appointmentId = documentReference.id

            // make the timeslot unavailable

            val doctorId = appointment.doctorId
            val date = appointment.date
            val startTime = appointment.time
            val endTime =
                startTime.plus(appointment.type.durationInMinutes.toLong(), ChronoUnit.MINUTES)

            // Fetch the current available slots
            val docRef = firestore.collection("users")
                .document(doctorId)
                .collection("availability")
                .document(date.toString())

            val availabilitySnapshot = docRef.get().await()
            if (availabilitySnapshot.exists()) {
                val availableSlots =
                    availabilitySnapshot.get("availableSlots") as? List<String> ?: emptyList()
                val updatedAvailableSlots = availableSlots.filterNot { slot ->
                    val slotTime = LocalTime.parse(
                        slot,
                        DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)
                    )
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
                val appointment = Appointment.fromMap(
                    document.data?.plus("appointmentId" to id) ?: mapOf("appointmentId" to id)
                )
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
            val id = if (role == "doctor") {
                "doctorId"
            } else {
                "patientId"
            }
            val userId =
                auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val querySnapshot = firestore.collection("appointments")
                .whereEqualTo(id, userId)
                .get()
                .await()

            val appointments = querySnapshot.documents.map { doc ->
                Appointment.fromMap(
                    doc.data?.plus("appointmentId" to doc.id) ?: mapOf("appointmentId" to doc.id)
                )
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


    suspend fun cancelAppointment(appointmentId: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val appointmentRef = firestore.collection("appointments").document(appointmentId)
                val appointmentSnapshot = transaction.get(appointmentRef)

                if (!appointmentSnapshot.exists()) {
                    throw Exception("Appointment not found")
                }

                val appointment = Appointment.fromMap(
                    appointmentSnapshot.data?.plus("appointmentId" to appointmentId)
                        ?: throw Exception("Invalid appointment data")
                )

                val doctorId = appointment.doctorId
                val date = appointment.date
                val startTime = appointment.time
                val endTime =
                    startTime.plus(appointment.type.durationInMinutes.toLong(), ChronoUnit.MINUTES)

                val availableSlots = mutableSetOf<LocalTime>()
                var currentTime = startTime
                while (currentTime.isBefore(endTime)) {
                    availableSlots.add(currentTime)
                    currentTime = currentTime.plus(15, ChronoUnit.MINUTES)
                }

                val docRef = firestore.collection("users")
                    .document(doctorId)
                    .collection("availability")
                    .document(date.toString())

                val availabilitySnapshot = transaction.get(docRef)
                if (availabilitySnapshot.exists()) {
                    val currentAvailableSlots =
                        availabilitySnapshot.get("availableSlots") as? List<String> ?: emptyList()
                    val updatedAvailableSlots = (currentAvailableSlots + availableSlots.map {
                        it.format(
                            DateTimeFormatter.ofPattern(
                                "h:mm a",
                                java.util.Locale.US
                            )
                        )
                    }).distinct().sortedBy {
                        LocalTime.parse(
                            it,
                            DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)
                        )
                    }
                    transaction.update(docRef, "availableSlots", updatedAvailableSlots)
                } else {
                    throw Exception("Availability document does not exist")
                }

                transaction.delete(appointmentRef)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rescheduleAppointment(
        appointmentId: String,
        newTime: LocalTime,
        newDate: LocalDate
    ): Result<Unit> {
        return try {
            val appointmentRef = firestore.collection("appointments").document(appointmentId)
            val appointmentSnapshot = appointmentRef.get().await()

            if (!appointmentSnapshot.exists()) {
                return Result.failure(Exception("Appointment not found"))
            }

            val appointment = Appointment.fromMap(
                appointmentSnapshot.data?.plus("appointmentId" to appointmentId)
                    ?: throw Exception("Invalid appointment data")
            )

            val doctorId = appointment.doctorId
            val oldDate = appointment.date
            val oldStartTime = appointment.time
            val oldEndTime =
                oldStartTime.plus(appointment.type.durationInMinutes.toLong(), ChronoUnit.MINUTES)

            val newEndTime =
                newTime.plus(appointment.type.durationInMinutes.toLong(), ChronoUnit.MINUTES)

            // Update the appointment with the new date and time
            val updatedAppointmentData = mapOf(
                "date" to newDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", java.util.Locale.US)),
                "time" to newTime.format(DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US))
            )

            appointmentRef.update(updatedAppointmentData).await()

            // get 15 minute slots that we can add back to the doctor's availability
            val againAvailableSlots = mutableSetOf<LocalTime>()
            var currentTime = oldStartTime
            while (currentTime.isBefore(oldEndTime)) {
                againAvailableSlots.add(currentTime)
                currentTime = currentTime.plus(15, ChronoUnit.MINUTES)
            }

            firestore.runTransaction { transaction ->
                // Add the old time slot back to the doctor's availability
                val docRef = firestore.collection("users")
                    .document(doctorId)
                    .collection("availability")
                    .document(oldDate.toString())

                val newDocRef = firestore.collection("users")
                    .document(doctorId)
                    .collection("availability")
                    .document(newDate.toString())

                // Read all required documents first
                val availabilitySnapshot = transaction.get(docRef)
                val newAvailabilitySnapshot = transaction.get(newDocRef)

                // Process the old availability
                if (availabilitySnapshot.exists()) {
                    val doctorStartTime =
                        availabilitySnapshot.getString("startTime")?.let {
                            LocalTime.parse(it, DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US))
                        } ?: LocalTime.MIN
                    val doctorEndTime =
                        availabilitySnapshot.getString("endTime")?.let {
                            LocalTime.parse(it, DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US))
                        } ?: LocalTime.MAX

                    // Check if the old time is within the doctor's availability (could have been changed)
                    val filteredSlots = againAvailableSlots.filter { slot ->
                        (slot.isAfter(doctorStartTime) && slot.isBefore(doctorEndTime)) || slot == doctorStartTime
                    }

                    val currentAvailableSlots =
                        availabilitySnapshot.get("availableSlots") as? List<String> ?: emptyList()
                    val updatedAvailableSlots = (currentAvailableSlots + filteredSlots.map {
                        it.format(
                            DateTimeFormatter.ofPattern(
                                "h:mm a",
                                java.util.Locale.US
                            )
                        )
                    }).distinct().sortedBy {
                        LocalTime.parse(
                            it,
                            DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)
                        )
                    }
                    transaction.update(docRef, "availableSlots", updatedAvailableSlots)
                } else {
                    throw Exception("Availability document does not exist")
                }

                // Process the new availability
                if (newAvailabilitySnapshot.exists()) {
                    val newAvailableSlots =
                        newAvailabilitySnapshot.get("availableSlots") as? List<String> ?: emptyList()
                    val updatedNewAvailableSlots = newAvailableSlots.filterNot { slot ->
                        val slotTime = LocalTime.parse(
                            slot,
                            DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)
                        )
                        (slotTime.isAfter(newTime) && slotTime.isBefore(newEndTime)) || slotTime == newTime
                    }
                    transaction.update(newDocRef, "availableSlots", updatedNewAvailableSlots)
                } else {
                    throw Exception("Availability document does not exist")
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}