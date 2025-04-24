package com.example.ladycure.repository

import android.util.Log
import com.example.ladycure.data.doctor.DoctorAvailability
import com.example.ladycure.data.doctor.Specialization
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

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


    suspend fun getCurrentUser(): Map<String, String>? {
        val user = auth.currentUser
        return user?.let {
            try {
                val document = firestore.collection("users").document(it.uid).get().await()
                if (document.exists()) {
                    mapOf(
                        "name" to (document.getString("name") ?: ""),
                        "surname" to (document.getString("surname") ?: ""),
                        "email" to (document.getString("email") ?: ""),
                        "dob" to (document.getString("dob") ?: ""),
                        "profilePictureUrl" to (document.getString("profilePictureUrl") ?: "")
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
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

    suspend fun signOut() {
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


    suspend fun getAllDoctorAvailabilitiesBySpecialization(specialization: String, city: String): List<DoctorAvailability> {
        val doctors = firestore.collection("users")
            .whereEqualTo("role", "doctor")
            .whereEqualTo("city", city)
            .whereEqualTo("specification", specialization)
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
                        date = doc.id,
                        startTime = doc.getString("startTime") ?: "",
                        endTime = doc.getString("endTime") ?: "",
                    )
                }
            allAvailabilities.addAll(availabilities)
        }
        return allAvailabilities
    }


    suspend fun getDoctorAvailability(doctorId: String): Result<List<DoctorAvailability>> {
        val doctor = firestore.collection("users").document(doctorId).get().await()
        return try {
            val availabilities = doctor.reference.collection("availability")
                .get()
                .await()
                .documents
                .map { doc ->
                    DoctorAvailability(
                        doctorId = doctor.id,
                        date = doc.id,
                        startTime = doc.getString("startTime") ?: "",
                        endTime = doc.getString("endTime") ?: "",
                    )
                }
            Result.success(availabilities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDoctorById(doctorId: String): Result<Map<String, Any>?> {
        return try {
            val document = firestore.collection("users").document(doctorId).get().await()
            if (document.exists()) {
                Result.success(document.data)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}