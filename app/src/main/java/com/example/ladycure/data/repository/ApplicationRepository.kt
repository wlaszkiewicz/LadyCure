package com.example.ladycure.data.repository

import com.example.ladycure.domain.model.Doctor
import com.example.ladycure.domain.model.DoctorApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ApplicationRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun submitApplication(application: DoctorApplication): Result<Unit> {
        var appMap = application.toMap()
        return try {
            firestore.collection("applications").document(application.userId).set(appMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getApplicationStatus(userId: String): String? {
        val document = firestore.collection("applications").document(userId).get().await()
        return if (document.exists()) {
            document.getString("status")
        } else {
            null
        }
    }

    suspend fun updateApplicationStatus(
        applicationId: String,
        status: String,
        reason: String = ""
    ): Result<Unit> {
        return try {
            firestore.collection("applications").document(applicationId).update("status", status)
                .await()
            if (reason.isNotEmpty()) {
                firestore.collection("applications").document(applicationId)
                    .update("reviewNotes", reason)
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDoctorApplication(): Result<DoctorApplication?> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
        return try {
            val document = firestore.collection("applications").document(userId).get().await()
            if (document.exists()) {
                val application = DoctorApplication.fromMap(document.data ?: emptyMap())
                Result.success(application)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllApplications(): Result<List<DoctorApplication>> {
        return try {
            val snapshot = firestore.collection("applications").get().await()
            val applications = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                DoctorApplication.fromMap(data)
            }
            Result.success(applications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveApplication(
        applicationId: String,
    ): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                // Get application data (read first)
                val appRef = firestore.collection("applications").document(applicationId)
                val appSnapshot = transaction.get(appRef)
                if (!appSnapshot.exists()) {
                    throw Exception("Application not found")
                }
                val application = DoctorApplication.fromMap(appSnapshot.data ?: emptyMap())

                // Update application status (write after all reads)
                transaction.update(appRef, "status", "approved")

                // Create doctor data
                val doctorData = Doctor.fromApplication(application)
                val userRef = firestore.collection("users").document(application.userId)
                transaction.set(userRef, doctorData.toMap())
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}