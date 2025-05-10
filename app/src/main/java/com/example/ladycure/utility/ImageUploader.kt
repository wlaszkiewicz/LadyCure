package com.example.ladycure.utility

import android.content.ContentResolver
import android.content.Context
import android.util.Log
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import com.example.ladycure.repository.AuthRepository
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ImageUploader(private val context: Context) {
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference
    private val authRepo: AuthRepository = AuthRepository()

    suspend fun uploadImage(uri: Uri, userId: String): Result<String> {
        return try {
            val newImageRef = storageRef.child("profile_images/$userId/${UUID.randomUUID()}")
            newImageRef.putFile(uri).await()
            val downloadUrl = newImageRef.downloadUrl.await().toString()

//            // 3. Delete the old image if it exists
//            oldUrl?.let { url ->
//                try {
//                    // Validate the URL is a Firebase Storage URL
//                    if (url.startsWith("gs://") || url.contains("firebasestorage.googleapis.com")) {
//                        val oldRef = storage.getReferenceFromUrl(url)
//                        oldRef.delete().await()
//                    }
//                } catch (e: Exception) {
//                    // Log the error but don't fail the whole operation
//                    Log.e("ImageUploader", "Failed to delete old image: ${e.message}")
//                }
//            }

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


@Composable
fun rememberImagePickerLauncher(
    onImageSelected: (Uri) -> Unit
): ActivityResultLauncher<String> {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let(onImageSelected)
    }

    LaunchedEffect(Unit) {
        val permission = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                android.Manifest.permission.READ_MEDIA_IMAGES
            }
            else -> {
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            }
        }

        val status = ContextCompat.checkSelfPermission(context, permission)
        hasPermission = status == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            permissionLauncher.launch(permission)
        }
    }

    return remember {
        object : ActivityResultLauncher<String>() {
            override val contract: ActivityResultContract<String, *>
                get() = TODO("Not yet implemented")

            override fun launch(input: String, options: ActivityOptionsCompat?) {
                if (hasPermission) {
                    imagePickerLauncher.launch(input)
                }
            }

            override fun unregister() {
                imagePickerLauncher.unregister()
            }
        }
    }
}