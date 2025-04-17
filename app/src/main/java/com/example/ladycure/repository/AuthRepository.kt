package com.example.ladycure.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance("telecure")

    suspend fun register(email: String, name: String, surname: String, dateOfBirth: String, password: String): Result<Unit> {
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

    suspend fun getUserData(userId: String): Result<Map<String, Any>> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            if (document.exists()) {
                Result.success(document.data ?: emptyMap())
            } else {
                Result.failure(Exception("User data not found"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error fetching user data", e)
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
                        "dob" to (document.getString("dob") ?: "")
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

//    suspend fun getDoctorsBySpecification(specification: String): Result<List<Map<String, Any>>> {
//        return try {
//            val querySnapshot = firestore.collection("Doctors")
//                .whereEqualTo("specification", specification)
//                .get()
//                .await()
//            val doctors = querySnapshot.documents.map { it.data ?: emptyMap() }
//            Result.success(doctors)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }

    suspend fun getDoctorsBySpecification(specification: String): Result<List<Map<String, Any>>> {
        return try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("role", "doctor")
                .whereEqualTo("specification", specification)
                .get()
                .await()
            val doctors = querySnapshot.documents.map { it.data ?: emptyMap() }
            Result.success(doctors)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserData(updatedData: Map<String, String>): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("User not logged in"))

            // Create a map with only the fields we want to update
            val updateMap = mutableMapOf<String, Any>()
            updatedData["name"]?.let { updateMap["name"] = it }
            updatedData["surname"]?.let { updateMap["surname"] = it }
            updatedData["dob"]?.let { updateMap["dob"] = it }

            // Update Firestore document
            firestore.collection("users").document(user.uid)
                .update(updateMap)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error updating user data", e)
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        Firebase.auth.signOut()
    }

}