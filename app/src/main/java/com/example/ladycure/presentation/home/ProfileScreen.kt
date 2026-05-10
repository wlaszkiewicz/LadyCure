package com.example.ladycure.presentation.home

import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.example.ladycure.R
import com.example.ladycure.ui.theme.DefaultBackground
import com.example.ladycure.ui.theme.DefaultOnPrimary
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import com.example.ladycure.utility.rememberImagePickerLauncher


@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val dimens = rememberResponsiveDimens()
    var showAccountSettingsDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberImagePickerLauncher { uri ->
        viewModel.uploadProfilePicture(uri)
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
                modifier = Modifier.padding(dimens.w(16 / 411f)),
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

        Spacer(modifier = Modifier.height(dimens.h(32 / 914f)))

        if (viewModel.userData == null) {
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
                    Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))
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
                    .padding(horizontal = dimens.w(16 / 411f), vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(dimens.h(24 / 914f))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(dimens.w(150 / 411f))
                            .clip(CircleShape)
                            .border(4.dp, DefaultPrimary, CircleShape)
                            .background(
                                Color.Transparent
                            )
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            viewModel.currentImageUrl != "" -> {
                                SubcomposeAsyncImage(
                                    model = viewModel.currentImageUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(DefaultPrimary.copy(alpha = 0.2f)),
                                    contentScale = ContentScale.Crop,
                                    loading = {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(dimens.w(70 / 411f)),
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

                    Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))

                    viewModel.userData?.let { user ->
                        Text(
                            text = "${user["name"]} ${user["surname"]}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = DefaultPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

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
                        icon = Icons.Default.Notifications,
                        onClick = { showNotifications = true }
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
                    onClick = {
                        viewModel.signOut()
                        navController.navigate("welcome") { popUpTo(0) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DefaultPrimary.copy(alpha = 0.8f),
                        contentColor = DefaultOnPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sign Out")
                }

                if (viewModel.errorMessage.isNotEmpty()) {
                    Text(
                        text = viewModel.errorMessage,
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
            userData = viewModel.userData,
            onDismiss = { showAccountSettingsDialog = false },
            onSave = { updatedData ->
                showAccountSettingsDialog = false
                viewModel.updateUserData(updatedData)
            },
            role = viewModel.userData?.get("role") as? String,
            onSaveDoctorProfile = { viewModel.saveDoctorProfile(it) }
        )
    }

    if (showNotifications) {
        navController.navigate("notifications/user")
    }

    if (showSupportDialog) {
        val dimens2 = rememberResponsiveDimens()
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
                    .width(dimens2.w(300 / 411f))
                    .zIndex(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                    contentColor = DefaultOnPrimary
                )
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = dimens2.w(24 / 411f),
                        vertical = dimens2.h(24 / 914f)
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Help,
                        contentDescription = "Help Icon",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(dimens2.w(80 / 411f))
                    )

                    Spacer(modifier = Modifier.height(dimens2.h(16 / 914f)))

                    Text(
                        text = "Need Help?",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = DefaultPrimary,
                        modifier = Modifier.padding(bottom = dimens2.h(16 / 914f))
                    )

                    Text(
                        text = "If you need assistance, please contact us via email.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary,
                        modifier = Modifier.padding(bottom = dimens2.h(16 / 914f))
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
