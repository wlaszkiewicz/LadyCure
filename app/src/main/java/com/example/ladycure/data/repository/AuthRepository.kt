package com.example.ladycure.data.repository

import android.util.Log
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

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

                                    FirebaseMessaging.getInstance().token
                                        .addOnCompleteListener { tokenTask ->
                                            if (tokenTask.isSuccessful) {
                                                val token = tokenTask.result
                                                val uid =
                                                    FirebaseAuth.getInstance().currentUser?.uid
                                                if (uid != null) {
                                                    firestore
                                                        .collection("users")
                                                        .document(uid)
                                                        .update("fcmToken", token)
                                                        .addOnSuccessListener {
                                                            Log.d("FCM", "Token updated for $uid")
                                                            onSuccess()
                                                        }
                                                        .addOnFailureListener { e ->
                                                            Log.e(
                                                                "FCM",
                                                                "Failed to update token",
                                                                e
                                                            )
                                                            onFailure(
                                                                Exception("Failed to update FCM token: ${e.message}")
                                                            )
                                                        }
                                                }
                                            } else {
                                                Log.e(
                                                    "FCM",
                                                    "Failed to get FCM token",
                                                    tokenTask.exception
                                                )
                                                onFailure(
                                                    Exception("Failed to get FCM token: ${tokenTask.exception?.message}")
                                                )
                                            }
                                        }

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
                "role" to role,
                "joinedAt" to Timestamp.now()
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
        FirebaseMessaging.getInstance().deleteToken()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "FCM token deleted successfully")
                } else {
                    Log.e("FCM", "Failed to delete FCM token", task.exception)
                }
            }
        val currentUserId = getCurrentUserId() ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUserId)
            .update("fcmToken", FieldValue.delete())

        Firebase.auth.signOut()
    }

}

