package com.example.ladycure.presentation.home

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.example.ladycure.R
import com.example.ladycure.data.repository.AuthRepository
import com.example.ladycure.data.repository.UserRepository
import com.example.ladycure.presentation.register.components.DatePickerButton
import com.example.ladycure.utility.ImageUploader
import com.example.ladycure.utility.rememberImagePickerLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val userRepo = UserRepository()
    val authRepo = AuthRepository()
    val imageUploader = remember { ImageUploader(context) }
    val userData = remember { mutableStateOf<Map<String, Any>?>(null) }
    var showAccountSettingsDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var currentImageUrl by remember {
        mutableStateOf(
            userData.value?.get("profilePictureUrl") ?: ""
        )
    }

    var imageUri: Uri? by remember { mutableStateOf(null) }
    val imagePickerLauncher = rememberImagePickerLauncher { uri ->
        imageUri = uri
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = authRepo.getCurrentUserId() ?: return@launch
                imageUploader.uploadImage(uri, userId).fold(
                    onSuccess = { downloadUrl ->
                        userRepo.updateProfilePicture(downloadUrl)
                        currentImageUrl = downloadUrl
                        userData.value = userRepo.getCurrentUserData().getOrNull()
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
        val result = userRepo.getCurrentUserData()
        if (result.isFailure) {
            errorMessage = "Failed to load user data: ${result.exceptionOrNull()?.message}"
        } else {
            userData.value = result.getOrNull()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultBackground)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DefaultBackground,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "My Profile",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DefaultPrimary,
                        fontSize = 28.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (userData.value == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = DefaultPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading user data...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = DefaultOnPrimary
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            currentImageUrl != "" -> {
                                SubcomposeAsyncImage(
                                    model = currentImageUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(DefaultPrimary.copy(alpha = 0.2f)),
                                    contentScale = ContentScale.Crop,
                                    loading = {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(70.dp),
                                            color = DefaultPrimary
                                        )
                                    },
                                    error = {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = "Error loading image",
                                            tint = DefaultPrimary,
                                        )
                                    }
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Default Profile Picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    tint = DefaultPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    userData.value?.let { user ->
                        Text(
                            text = "${user["name"]} ${user["surname"]}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = DefaultPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Settings
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
                    ProfileOption(
                        text = "Notifications",
                        icon = Icons.Default.Notifications
                    )
                    ProfileOption(
                        text = "Help & Support",
                        icon = painterResource(id = R.drawable.baseline_contact_support),
                        isVector = true,
                        onClick = { showSupportDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { logOut(navController) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DefaultPrimary.copy(alpha = 0.8f),
                        contentColor = DefaultOnPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sign Out")
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }

    if (showAccountSettingsDialog) {
        AccountSettingsDialog(
            userData = userData.value,
            onDismiss = { showAccountSettingsDialog = false },
            onSave = { updatedData ->
                CoroutineScope(Dispatchers.IO).launch {
                    val result = userRepo.updateUserData(updatedData)
                    if (result.isSuccess) {
                        userData.value = result.getOrNull() ?: emptyMap()
                    } else {
                        errorMessage =
                            "Failed to update user data: ${result.exceptionOrNull()?.message}"
                    }
                    showAccountSettingsDialog = false
                }
            }
        )
    }

    if (showSupportDialog) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showSupportDialog = false }
            )

            Card(
                modifier = Modifier
                    .width(300.dp)
                    .zIndex(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                    contentColor = DefaultOnPrimary
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Help,
                        contentDescription = "Help Icon",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(80.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Need Help?",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = DefaultPrimary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "If you need assistance, please contact us via email.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { showSupportDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = DefaultPrimary
                            ),
                            border = BorderStroke(1.dp, DefaultPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    setData("mailto:ladycure_admin@gmail.com".toUri())
                                }
                                navController.context.startActivity(intent)
                                showSupportDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DefaultPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Contact Us")
                        }
                    }
                }
            }
        }
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
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp), // Increased height
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = DefaultOnPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(0.5.dp, DefaultPrimary.copy(alpha = 0.1f)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isVector) {
                Icon(
                    painter = icon as Painter,
                    contentDescription = text,
                    tint = DefaultPrimary,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Icon(
                    imageVector = icon as ImageVector,
                    contentDescription = text,
                    tint = DefaultPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = DefaultOnPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = DefaultPrimary.copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp)
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
    var name by remember {
        mutableStateOf(
            TextFieldValue(
                (userData?.get("name") as? String) ?: ""
            )
        )
    }
    var surname by remember {
        mutableStateOf(
            TextFieldValue(
                (userData?.get("surname") as? String) ?: ""
            )
        )
    }

    val initialDob = remember {
        try {
            (userData?.get("dob") as? String)?.let {
                LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
            } ?: LocalDate.now().minusYears(18)
        } catch (e: Exception) {
            LocalDate.now().minusYears(18)
        }
    }
    var dob by remember { mutableStateOf(initialDob) }
    var dobText by remember { mutableStateOf(initialDob.format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var isAdult by remember { mutableStateOf(!dob.isAfter(LocalDate.now().minusYears(18))) }

    var phone by remember {
        mutableStateOf(
            TextFieldValue(
                (userData?.get("phone") as? String) ?: ""
            )
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(onClick = onDismiss)
        )

        Box(
            modifier = Modifier
                .width(360.dp)
                .height(720.dp)
                .padding(top = 70.dp)
                .background(
                    color = DefaultBackground,
                    shape = RoundedCornerShape(20.dp)
                )
        )

        Image(
            painter = painterResource(id = R.drawable.login_kapi),
            contentDescription = "Capybara",
            modifier = Modifier
                .width(240.dp)
                .height(210.dp)
                .offset(y = 80.dp)
                .zIndex(3f),
            contentScale = ContentScale.Crop
        )

        Card(
            modifier = Modifier
                .width(340.dp)
                .padding(top = 260.dp)
                .zIndex(2f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
                contentColor = DefaultOnPrimary
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Account Settings",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = DefaultPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = surname,
                    onValueChange = { surname = it },
                    label = { Text("Surname") },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = "Surname") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))


                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("+48 123 456 789") }
                )

                Spacer(modifier = Modifier.height(8.dp))


                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Date of Birth",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    DatePickerButton(
                        selectedDate = dob,
                        onDateSelected = { date ->
                            dob = date
                            dobText = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            isAdult = !date.isAfter(LocalDate.now().minusYears(18))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!isAdult) {
                        Text(
                            text = "We are sorry, you must be at least 18 years old",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DefaultOnPrimary.copy(alpha = 0.1f),
                            contentColor = DefaultPrimary
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val updatedData = mapOf(
                                "name" to name.text,
                                "surname" to surname.text,
                                "dob" to dobText,
                                "phone" to phone.text
                            )
                            onSave(updatedData)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DefaultPrimary,
                            contentColor = DefaultOnPrimary
                        ),
                        enabled = isAdult
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

fun logOut(navController: NavHostController) {
    val authRepo = AuthRepository()
    authRepo.signOut()
    navController.navigate("welcome") { popUpTo(0) }
}