package com.example.ladycure.repository

import android.util.Log
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance("telecure")

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
                                        "doctor_pending" -> navController.navigate("doctor_pending")
                                        "user" -> navController.navigate("home")
                                        else -> {
                                            onFailure(Exception("Unknown user role: $role"))
                                            return@addOnSuccessListener
                                        }
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
        password: String,
        role: String = "user"
    ): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User registration failed")

            val userData = hashMapOf(
                "email" to email,
                "name" to name,
                "surname" to surname,
                "dob" to dateOfBirth,
                "role" to role
            )
            firestore.collection("users").document(user.uid).set(userData)
                .addOnSuccessListener { }
                .addOnFailureListener { throw Exception("User registration failed") }

            Result.success(user.uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    fun signOut() {
        Firebase.auth.signOut()
    }

    suspend fun getAdminStats(): Result<MutableMap<String, Any>> {
        return try {
            val stats = mutableMapOf<String, Any>()
            firestore.collection("users").get().addOnSuccessListener { users ->
                stats["totalUsers"] = users.size()
            }.await()

            firestore.collection("users")
                .whereEqualTo("role", "doctor")
                .get()
                .addOnSuccessListener { doctors ->
                    stats["activeDoctors"] = doctors.size()
                }.await()

            firestore.collection("applications").whereEqualTo("status", "pending").get()
                .addOnSuccessListener { applications ->
                    stats["pendingApplications"] = applications.size()
                }.await()


            Result.success(stats)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error fetching admin stats: ${e.message}")
            Result.failure(e)
        }
    }
}

