package com.example.ladycure.data.repository

import com.example.ladycure.domain.model.Doctor
import com.example.ladycure.domain.model.DoctorApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository class responsible for handling doctor application logic,
 * including submission, approval, and status tracking.
 */
class ApplicationRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()


    /**
     * Submits a new doctor application to Firestore.
     *
     * @param application The [DoctorApplication] to be submitted.
     * @return [Result.success] if submission is successful, or [Result.failure] with an exception.
     */
    suspend fun submitApplication(application: DoctorApplication): Result<Unit> {
        var appMap = application.toMap()
        return try {
            firestore.collection("applications").document(application.userId).set(appMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /**
     * Retrieves the current application status for the specified user.
     *
     * @param userId The ID of the user.
     * @return The status string (e.g., "pending", "approved", "rejected"), or null if not found.
     */
    suspend fun getApplicationStatus(userId: String): String? {
        val document = firestore.collection("applications").document(userId).get().await()
        return if (document.exists()) {
            document.getString("status")
        } else {
            null
        }
    }


    /**
     * Updates the status of a specific doctor application.
     *
     * @param applicationId The ID of the application document.
     * @param status The new status to be set.
     * @param reason Optional reason or review notes (default is empty).
     * @return [Result.success] if the update is successful, or [Result.failure] on error.
     */
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

    /**
     * Retrieves the current user's doctor application.
     *
     * @return A [Result] containing the [DoctorApplication], or null if not found or user not logged in.
     */
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

    /**
     * Fetches all doctor applications from Firestore.
     *
     * @return A [Result] containing a list of [DoctorApplication] objects.
     */
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


    /**
     * Approves a doctor application and creates a corresponding doctor user in Firestore.
     * This is executed as a transaction to ensure consistency.
     *
     * @param applicationId The ID of the application to approve.
     * @return [Result.success] if approval is successful, or [Result.failure] with the encountered exception.
     */
    suspend fun approveApplication(
        applicationId: String,
    ): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val appRef = firestore.collection("applications").document(applicationId)
                val appSnapshot = transaction.get(appRef)
                if (!appSnapshot.exists()) {
                    throw Exception("Application not found")
                }
                val application = DoctorApplication.fromMap(appSnapshot.data ?: emptyMap())

                transaction.update(appRef, "status", "approved")

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