package com.example.ladycure.data.repository

import android.net.Uri
import com.example.ladycure.domain.model.AppointmentType
import com.example.ladycure.domain.model.Referral
import com.example.ladycure.utility.PdfUploader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File

@Singleton
class StorageRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val storageRef = storage.reference
    suspend fun uploadReferralToFirestore(
        context: android.content.Context,
        uri: Uri,
        service: AppointmentType?,
        onProgress: (PdfUploader.UploadProgress) -> Unit
    ): Result<String> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User not logged in"))

        return try {

            val pdfUrl = PdfUploader.uploadReferral(context, uri, userId, onProgress)

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
        context: android.content.Context,
        uri: Uri,
        oldUri: String,
        referralId: String,
        service: String?,
        onProgress: (PdfUploader.UploadProgress) -> Unit
    ): Result<String> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User not logged in"))

        return try {
            val pdfUrl = PdfUploader.replaceReferral(context, uri, oldUri, userId, onProgress)

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


    suspend fun uploadFile(
        context: android.content.Context,
        uri: Uri,
        path: String,
        onProgress: (uploadedBytes: Long, totalBytes: Long) -> Unit
    ): Result<String> {
        return try {
            val userId =
                auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

            val fileRef = storage.reference.child(path)

            val tempFile = File.createTempFile("upload", null, context.cacheDir)

            context.contentResolver.openInputStream(uri)?.use { it.copyTo(tempFile.outputStream()) }
            val uploadTask = fileRef.putFile(android.net.Uri.fromFile(tempFile))

            uploadTask.addOnProgressListener { taskSnapshot ->
                val bytesTransferred = taskSnapshot.bytesTransferred
                val totalBytes = taskSnapshot.totalByteCount
                onProgress(bytesTransferred, totalBytes)
            }

            val task = uploadTask.await()
            val downloadUrl = task.storage.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFile(url: String): Result<Boolean> {
        return try {
            val storageRef = storage.getReferenceFromUrl(url)
            storageRef.delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}