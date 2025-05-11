package com.example.ladycure.utility

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

object PdfUploader {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    data class UploadProgress(val bytesTransferred: Long, val totalBytes: Long) {
        val progress: Float get() =
            if (totalBytes > 0) bytesTransferred.toFloat() / totalBytes.toFloat() else 0f
    }

    suspend fun uploadReferral(
        uri: Uri,
        userId: String,
        onProgress: (suspend (UploadProgress) -> Unit)? = null
    ): String {
        return suspendCoroutine { continuation ->
            val pdfRef = storageRef.child("referrals/$userId/${UUID.randomUUID()}.pdf")

            val uploadTask = pdfRef.putFile(uri)

            uploadTask.addOnProgressListener { snapshot ->
                CoroutineScope(Dispatchers.IO).launch {
                    onProgress?.invoke(
                        UploadProgress(
                            bytesTransferred = snapshot.bytesTransferred,
                            totalBytes = snapshot.totalByteCount
                        )
                    )
                }
            }

            uploadTask.addOnSuccessListener {
                pdfRef.downloadUrl.addOnSuccessListener { uri ->
                    continuation.resumeWith(Result.success(uri.toString()))
                }.addOnFailureListener { e ->
                    continuation.resumeWith(Result.failure(e))
                }
            }.addOnFailureListener { e ->
                continuation.resumeWith(Result.failure(e))
            }
        }
    }

    suspend fun replaceReferral(
        uri: Uri,
        oldUri: String,
        userId: String,
        onProgress: (suspend (UploadProgress) -> Unit)? = null
    ): String {
        return suspendCoroutine { continuation ->
            // First delete the old file if the oldUri is not empty
            if (oldUri.isNotEmpty()) {
                try {
                    val oldRef = storage.getReferenceFromUrl(oldUri)
                    oldRef.delete().addOnSuccessListener {
                        // After successful deletion, upload the new file
                        uploadNewFile(uri, userId, onProgress, continuation)
                    }.addOnFailureListener { e ->
                        continuation.resumeWith(Result.failure(e))
                    }
                } catch (e: Exception) {
                    continuation.resumeWith(Result.failure(e))
                }
            } else {
                // If there's no old file, just upload the new one
                uploadNewFile(uri, userId, onProgress, continuation)
            }
        }
    }

    private fun uploadNewFile(
        uri: Uri,
        userId: String,
        onProgress: (suspend (UploadProgress) -> Unit)?,
        continuation: Continuation<String>
    ) {
        val pdfRef = storageRef.child("referrals/$userId/${UUID.randomUUID()}.pdf")
        val uploadTask = pdfRef.putFile(uri)

        uploadTask.addOnProgressListener { snapshot ->
            CoroutineScope(Dispatchers.IO).launch {
                onProgress?.invoke(
                    UploadProgress(
                        bytesTransferred = snapshot.bytesTransferred,
                        totalBytes = snapshot.totalByteCount
                    )
                )
            }
        }

        uploadTask.addOnSuccessListener {
            pdfRef.downloadUrl.addOnSuccessListener { uri ->
                continuation.resumeWith(Result.success(uri.toString()))
            }.addOnFailureListener { e ->
                continuation.resumeWith(Result.failure(e))
            }
        }.addOnFailureListener { e ->
            continuation.resumeWith(Result.failure(e))
        }
    }
}