package com.example.ladycure.utility

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.UUID
import kotlin.coroutines.Continuation

object PdfUploader {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    private const val MAX_FILE_SIZE_BYTES: Long = 5 * 1024 * 1024 // 5MB

    data class UploadProgress(val bytesTransferred: Long, val totalBytes: Long) {
        val progress: Float
            get() =
                if (totalBytes > 0) bytesTransferred.toFloat() / totalBytes.toFloat() else 0f
    }

    // Copies URI content to a temp file to work around Waydroid/emulator content provider issues
    private fun uriToTempFile(context: Context, uri: Uri): File {
        val tempFile = File.createTempFile("upload_pdf", ".pdf", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    suspend fun uploadReferral(
        context: Context,
        uri: Uri,
        userId: String,
        onProgress: (suspend (UploadProgress) -> Unit)? = null
    ): String {
        return suspendCancellableCoroutine { continuation ->
            val pdfRef = storageRef.child("referrals/$userId/${UUID.randomUUID()}.pdf")

            val tempFile = uriToTempFile(context, uri)
            val uploadTask = pdfRef.putFile(android.net.Uri.fromFile(tempFile))

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
                tempFile.delete()
                pdfRef.downloadUrl.addOnSuccessListener { uri ->
                    continuation.resumeWith(Result.success(uri.toString()))
                }.addOnFailureListener { e ->
                    continuation.resumeWith(Result.failure(e))
                }
            }.addOnFailureListener { e ->
                tempFile.delete()
                continuation.resumeWith(Result.failure(e))
            }
        }
    }

    suspend fun replaceReferral(
        context: Context,
        uri: Uri,
        oldUri: String,
        userId: String,
        onProgress: (suspend (UploadProgress) -> Unit)? = null
    ): String {
        return suspendCancellableCoroutine { continuation ->
            if (oldUri.isNotEmpty()) {
                try {
                    val oldRef = storage.getReferenceFromUrl(oldUri)
                    oldRef.delete().addOnSuccessListener {
                        uploadNewFile(context, uri, userId, onProgress, continuation)
                    }.addOnFailureListener { e ->
                        continuation.resumeWith(Result.failure(e))
                    }
                } catch (e: Exception) {
                    continuation.resumeWith(Result.failure(e))
                }
            } else {
                uploadNewFile(context, uri, userId, onProgress, continuation)
            }
        }
    }

    private fun uploadNewFile(
        context: Context,
        uri: Uri,
        userId: String,
        onProgress: (suspend (UploadProgress) -> Unit)?,
        continuation: Continuation<String>
    ) {
        val pdfRef = storageRef.child("referrals/$userId/${UUID.randomUUID()}.pdf")
        val tempFile = uriToTempFile(context, uri)
        val uploadTask = pdfRef.putFile(android.net.Uri.fromFile(tempFile))

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
            tempFile.delete()
            pdfRef.downloadUrl.addOnSuccessListener { uri ->
                continuation.resumeWith(Result.success(uri.toString()))
            }.addOnFailureListener { e ->
                continuation.resumeWith(Result.failure(e))
            }
        }.addOnFailureListener { e ->
            tempFile.delete()
            continuation.resumeWith(Result.failure(e))
        }
    }

    private fun getFileSize(context: Context, uri: Uri): Long {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            if (it.moveToFirst()) it.getLong(sizeIndex) else -1
        } ?: -1
    }

    fun isFileTooLarge(
        context: Context,
        uri: Uri,
        maxSizeBytes: Long = MAX_FILE_SIZE_BYTES
    ): Boolean {
        return getFileSize(context, uri) > maxSizeBytes
    }
}