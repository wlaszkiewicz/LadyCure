package com.example.ladycure.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
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
            firestore.collection("user").document(user.uid).set(userData)
                .addOnSuccessListener { }
                .addOnFailureListener { throw Exception("User registration failed") }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}