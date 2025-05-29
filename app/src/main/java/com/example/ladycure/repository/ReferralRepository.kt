package com.example.ladycure.repository

import android.net.Uri
import com.example.ladycure.data.AppointmentType
import com.example.ladycure.data.doctor.Referral
import com.example.ladycure.utility.PdfUploader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReferralRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance("telecure")

    suspend fun uploadReferralToFirestore(
        uri: Uri,
        service: AppointmentType?,
        onProgress: (PdfUploader.UploadProgress) -> Unit
    ): Result<String> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User not logged in"))

        return try {

            val pdfUrl = PdfUploader.uploadReferral(uri, userId, onProgress)

            firestore.runTransaction { transaction ->
                val userRef = firestore.collection("users")
                    .document(userId)
                    .collection("referrals")
                    .document()

                val referralData = mapOf(
                    "url" to pdfUrl,
                    "service" to service?.displayName,
                    "uploadedAt" to System.currentTimeMillis(),
                    "patientId" to userId,
                )

                transaction.set(userRef, referralData)
            }.await()

            val referralId = firestore.collection("users")
                .document(userId)
                .collection("referrals")
                .whereEqualTo("url", pdfUrl)
                .get()
                .await()
                .documents
                .firstOrNull()?.id



            Result.success(referralId ?: throw Exception("Referral ID not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun replaceReferralInFirestore(
        uri: Uri,
        oldUri: String,
        referralId: String,
        service: String?,
        onProgress: (PdfUploader.UploadProgress) -> Unit
    ): Result<String> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User not logged in"))

        return try {
            val pdfUrl = PdfUploader.replaceReferral(uri, oldUri, userId, onProgress)

            firestore.runTransaction { transaction ->
                val userRef = firestore.collection("users")
                    .document(userId)
                    .collection("referrals")
                    .document(referralId)

                val referralData = mapOf(
                    "url" to pdfUrl,
                    "service" to service,
                    "uploadedAt" to System.currentTimeMillis(),
                    "patientId" to userId,
                )

                transaction.update(userRef, referralData)
            }.await()

            Result.success(pdfUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReferralById(referralId: String): Result<Referral> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))
            val document = firestore.collection("users")
                .document(userId)
                .collection("referrals")
                .document(referralId)
                .get()
                .await()

            if (document.exists()) {
                val referral = Referral.fromMap(
                    document.data?.plus("id" to referralId) ?: mapOf("id" to referralId)
                )
                Result.success(referral)
            } else {
                Result.failure(Exception("Referral not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}