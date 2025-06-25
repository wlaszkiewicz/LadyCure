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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.example.ladycure.R
import com.example.ladycure.data.repository.AuthRepository
import com.example.ladycure.data.repository.DoctorRepository
import com.example.ladycure.data.repository.UserRepository
import com.example.ladycure.domain.model.Speciality
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
    val doctorRepo = remember { DoctorRepository() }
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
                        val role = userData.value?.get("role") as? String
                        if (role == "doctor") {
                            userRepo.updateProfilePicture(downloadUrl)
                            userData.value = doctorRepo.getCurrentDoctorData().getOrNull()
                        } else {
                            userData.value = userRepo.getCurrentUserData().getOrNull()
                        }
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
        val userResult = userRepo.getCurrentUserData()
        if (userResult.isFailure) {
            errorMessage = "Failed to load user data: ${userResult.exceptionOrNull()?.message}"
            return@LaunchedEffect
        }

        val user = userResult.getOrNull() ?: return@LaunchedEffect
        val role = user["role"] as? String

        if (role == "doctor") {
            val doctorResult = doctorRepo.getCurrentDoctorData()
            if (doctorResult.isFailure) {
                errorMessage = "Failed to load doctor data: ${doctorResult.exceptionOrNull()?.message}"
            } else {
                userData.value = doctorResult.getOrNull()
            }
        } else {
            userData.value = user
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
                    val role = userData.value?.get("role") as? String
                    if (role == "doctor") {
                        userData.value = doctorRepo.getCurrentDoctorData().getOrNull() ?: emptyMap()
                    } else {
                        val result = userRepo.updateUserData(updatedData)
                        if (result.isSuccess) {
                            userData.value = result.getOrNull() ?: emptyMap()
                        } else {
                            errorMessage = "Failed to update user data: ${result.exceptionOrNull()?.message}"
                        }
                    }
                    showAccountSettingsDialog = false
                }
            },
            role = userData.value?.get("role") as? String
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
    onSave: (Map<String, String>) -> Unit,
    role: String? = null
) {
    val doctorRepo = remember { DoctorRepository() }
    when (role) {
        "doctor" -> DoctorAccountSettingsDialog(
            userData = userData,
            onDismiss = onDismiss,
            onSave = { updatedData ->
                CoroutineScope(Dispatchers.IO).launch {
                    doctorRepo.updateDoctorProfile(updatedData)
                    onSave(updatedData as Map<String, String>)
                }
            }
        )
        else -> RegularAccountSettingsDialog(userData, onDismiss, onSave)
    }
}

@Composable
fun DoctorAccountSettingsDialog(
    userData: Map<String, Any>?,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit,
    role: String? = null
) {
    var name by remember { mutableStateOf(TextFieldValue((userData?.get("name") as? String) ?: "")) }
    var surname by remember { mutableStateOf(TextFieldValue((userData?.get("surname") as? String) ?: "")) }

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

    var selectedSpeciality by remember {
        mutableStateOf(
            Speciality.fromDisplayName(userData?.get("speciality") as? String ?: "") ?: Speciality.OTHER
        )
    }
    var expanded by remember { mutableStateOf(false) }

    var phone by remember { mutableStateOf(TextFieldValue((userData?.get("phone") as? String) ?: "")) }
    var address by remember { mutableStateOf(TextFieldValue((userData?.get("address") as? String) ?: "")) }
    var city by remember { mutableStateOf(TextFieldValue((userData?.get("city") as? String) ?: "")) }
    var consultationPrice by remember { mutableStateOf(TextFieldValue((userData?.get("consultationPrice") as? String) ?: "")) }
    var experience by remember { mutableStateOf(TextFieldValue((userData?.get("experience") as? String) ?: "")) }
    var languages by remember { mutableStateOf(TextFieldValue((userData?.get("languages") as? List<String>)?.joinToString(", ") ?: "")) }
    var speciality by remember { mutableStateOf(TextFieldValue((userData?.get("speciality") as? String) ?: "")) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(DefaultBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onDismiss() },
                    tint = DefaultPrimary
                )

                Text(
                    text = "Doctor Account Settings",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = DefaultPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.size(32.dp))
            }

            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(DefaultPrimary.copy(alpha = 0.1f))
                    .border(2.dp, DefaultPrimary.copy(alpha = 0.3f), CircleShape)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.setting_kapii),
                    contentDescription = "settings kapi",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Basic Information",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = DefaultPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DefaultPrimary,
                                focusedLabelColor = DefaultPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = surname,
                            onValueChange = { surname = it },
                            label = { Text("Surname") },
                            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = "Surname") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DefaultPrimary,
                                unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = DefaultPrimary,
                                unfocusedLabelColor = Color.Gray,
                                cursorColor = DefaultPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("+48 123 456 789") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DefaultPrimary,
                                focusedLabelColor = DefaultPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Date of Birth",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(bottom = 4.dp)
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
                                    text = "You must be at least 18 years old",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Professional Information",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = DefaultPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = experience,
                            onValueChange = { experience = it },
                            label = { Text("Experience (years)") },
                            leadingIcon = { Icon(Icons.Default.Star, contentDescription = "Experience") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DefaultPrimary,
                                focusedLabelColor = DefaultPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = consultationPrice,
                            onValueChange = { consultationPrice = it },
                            label = { Text("Consultation Price") },
                            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = "Price") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            prefix = { Text("$") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DefaultPrimary,
                                focusedLabelColor = DefaultPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = languages,
                            onValueChange = { languages = it },
                            label = { Text("Languages (comma separated)") },
                            leadingIcon = { Icon(Icons.Default.Language, contentDescription = "Languages") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DefaultPrimary,
                                focusedLabelColor = DefaultPrimary
                            )
                        )
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Speciality",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = DefaultPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = true }
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = selectedSpeciality.displayName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Expand",
                                        modifier = Modifier.rotate(if (expanded) 180f else 0f)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                Speciality.values().forEach { speciality ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = speciality.displayName,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        },
                                        onClick = {
                                            selectedSpeciality = speciality
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Address Information",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = DefaultPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Address") },
                            leadingIcon = { Icon(Icons.Default.Place, contentDescription = "Address") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DefaultPrimary,
                                focusedLabelColor = DefaultPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = "City") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DefaultPrimary,
                                focusedLabelColor = DefaultPrimary
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .width(120.dp),
                        border = BorderStroke(1.dp, DefaultPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DefaultPrimary)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val updatedData = mapOf(
                                "name" to name.text,
                                "surname" to surname.text,
                                "dob" to dobText,
                                "phone" to phone.text,
                                "address" to address.text,
                                "city" to city.text,
                                "consultationPrice" to consultationPrice.text,
                                "experience" to experience.text,
                                "languages" to languages.text.split(",").map { it.trim() },
                                "speciality" to selectedSpeciality.displayName
                            )
                            onSave(updatedData)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DefaultPrimary,
                            contentColor = Color.White
                        ),
                        enabled = isAdult,
                        modifier = Modifier.width(140.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
fun RegularAccountSettingsDialog(
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
                    .height(700.dp)
                    .padding(top = 70.dp)
                    .background(
                        color = DefaultBackground,
                        shape = RoundedCornerShape(20.dp)
                    )
            )
            Card(
                modifier = Modifier
                    .width(340.dp)
                    .padding(top = 80.dp)
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
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(DefaultPrimary.copy(alpha = 0.1f))
                            .border(2.dp, DefaultPrimary.copy(alpha = 0.3f), CircleShape)
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.setting_kapii),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Account Settings",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
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
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = "Surname"
                            )
                        },
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
}

fun logOut(navController: NavHostController) {
    val authRepo = AuthRepository()
    authRepo.signOut()
    navController.navigate("welcome") { popUpTo(0) }
}