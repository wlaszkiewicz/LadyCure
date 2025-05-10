package com.example.ladycure.utility

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.suspendCoroutine

object PdfUploader {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    data class UploadProgress(val bytesTransferred: Long, val totalBytes: Long) {
        val progress: Float get() =
            if (totalBytes > 0) bytesTransferred.toFloat() / totalBytes.toFloat() else 0f
    }

    suspend fun uploadPdf(
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
}