package com.example.ladycure.repository

import android.net.Uri
import android.util.Log
import androidx.navigation.NavController
import com.example.ladycure.data.Appointment
import com.example.ladycure.data.AppointmentType
import com.example.ladycure.data.doctor.Doctor
import com.example.ladycure.data.doctor.DoctorAvailability
import com.example.ladycure.data.doctor.Referral
import com.example.ladycure.utility.PdfUploader
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


    fun signOut() {
        Firebase.auth.signOut()
    }
}

