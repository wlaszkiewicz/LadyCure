package com.example.ladycure

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.ladycure.presentation.home.components.BottomNavBar
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.utility.ImageUploader
import com.example.ladycure.utility.rememberImagePickerLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.let

@Composable
fun ProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val repository = AuthRepository()
    val imageUploader = remember { ImageUploader(context) }
    val userData = remember { mutableStateOf<Map<String, Any>?>(null) }
    var showAccountSettingsDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var currentImageUrl by remember { mutableStateOf(userData.value?.get("profilePictureUrl") ?: "") }

    var imageUri: Uri? by remember { mutableStateOf(null) }
    val imagePickerLauncher = rememberImagePickerLauncher { uri ->
        imageUri = uri
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = repository.getCurrentUserId() ?: return@launch
                imageUploader.uploadImage(uri, userId).fold(
                    onSuccess = { downloadUrl ->
                        repository.updateProfilePicture(downloadUrl)
                        currentImageUrl = downloadUrl
                        userData.value = repository.getCurrentUser()
                        errorMessage = ""
                    },
                    onFailure = { e ->
                        errorMessage = "Failed to update profile picture: ${e.message}"
                    }
                )
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            }
        }
    }

    LaunchedEffect(Unit) {
        userData.value = repository.getCurrentUser()
    }


    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(top = 40.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                currentImageUrl = userData.value?.get("profilePictureUrl") ?: ""
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .border(4.dp, DefaultPrimary, CircleShape)
                        .background(DefaultPrimary.copy(alpha = 0.2f))
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        userData.value == null -> {
                            CircularProgressIndicator(color = DefaultPrimary)
                        }

                        currentImageUrl != "" -> {
                            SubcomposeAsyncImage(
                                model = currentImageUrl,
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                loading = {
                                    CircularProgressIndicator(color = DefaultPrimary)
                                },
                                error = {
                                    Image(
                                        painter = painterResource(R.drawable.profile_pic),
                                        contentDescription = "Error loading image"
                                    )
                                }
                            )
                        }

                        else -> {
                            Image(
                                painter = painterResource(R.drawable.profile_pic),
                                contentDescription = "Default Profile Picture"
                            )
                        }
                    }

                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            userData.value?.let { user ->
                Text(
                    text = "${user["name"]} ${user["surname"]}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DefaultPrimary,
                    fontWeight = FontWeight.Bold
                )
            } ?: Text(
                text = "Loading user data...",
                style = MaterialTheme.typography.bodyLarge,
                color = DefaultOnPrimary
            )

            // Settings options
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleMedium,
                    color = DefaultPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ProfileOption(
                    text = "Account Settings",
                    icon = Icons.Default.AccountCircle,
                    onClick = { showAccountSettingsDialog = true }
                )
                ProfileOption("Notifications", Icons.Default.Notifications)
                ProfileOption("Privacy", Icons.Default.Lock)
                ProfileOption(
                    text = "Help & Support",
                    icon = painterResource(id = R.drawable.baseline_contact_support),
                    isVector = true,
                    onClick = { showSupportDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    logOut(navController)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultPrimary.copy(alpha = 0.5f),
                    contentColor = DefaultOnPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sign Out")
            }

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }

    if (showAccountSettingsDialog) {
        AccountSettingsDialog(
            userData = userData.value,
            onDismiss = { showAccountSettingsDialog = false },
            onSave = { updatedData ->
                CoroutineScope(Dispatchers.IO).launch {
                    val result = repository.updateUserData(updatedData)
                    if (result.isSuccess) {
                        userData.value = result.getOrNull() ?: emptyMap()
                    } else {
                        errorMessage = "Failed to update user data: ${result.exceptionOrNull()?.message}"
                    }
                    showAccountSettingsDialog = false
                }
            }
        )
    }

    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            title = { Text("Need Help?", color = DefaultPrimary) },
            text = {
                Text("If you need assistance, please contact us via email.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Open email intent
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:ladycure_admin@gmail.com".toUri()
                        }
                        navController.context.startActivity(intent)
                        showSupportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DefaultPrimary,
                        contentColor = DefaultOnPrimary
                    )
                ) {
                    Text("Contact Us")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showSupportDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DefaultOnPrimary.copy(alpha = 0.1f),
                        contentColor = DefaultPrimary
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = DefaultBackground
        )
    }
}

@Composable
fun ProfileOption(
    text: String,
    icon: Any,
    isVector: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DefaultPrimary.copy(alpha = 0.1f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isVector) {
                Icon(
                    painter = icon as Painter,
                    contentDescription = text,
                    tint = DefaultPrimary
                )
            } else {
                Icon(
                    imageVector = icon as ImageVector,
                    contentDescription = text,
                    tint = DefaultPrimary
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = DefaultOnPrimary
            )
        }
    }
}

@Composable
fun AccountSettingsDialog(
    userData: Map<String, Any>?,
    onDismiss: () -> Unit,
    onSave: (Map<String, String>) -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue((userData?.get("name") as? String) ?: "")) }
    var surname by remember { mutableStateOf(TextFieldValue((userData?.get("surname") as? String) ?: "")) }
    var dob by remember { mutableStateOf(TextFieldValue((userData?.get("dob") as? String) ?: "")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Account Settings", color = DefaultPrimary) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = surname,
                    onValueChange = { surname = it },
                    label = { Text("Surname") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text("Date of Birth") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("YYYY-MM-DD") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedData = mapOf(
                        "name" to name.text,
                        "surname" to surname.text,
                        "dob" to dob.text
                    )
                    onSave(updatedData)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultPrimary,
                    contentColor = DefaultOnPrimary
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultOnPrimary.copy(alpha = 0.1f),
                    contentColor = DefaultPrimary
                )
            ) {
                Text("Cancel")
            }
        },
        containerColor = DefaultBackground
    )
}

fun logOut(navController: NavHostController) {
    val authRepo = AuthRepository()
    CoroutineScope(Dispatchers.IO).launch {
        authRepo.signOut()
        CoroutineScope(Dispatchers.Main).launch {
            navController.navigate("welcome")
        }
    }
}


