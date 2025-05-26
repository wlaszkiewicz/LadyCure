package com.example.ladycure.screens

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.ladycure.data.Role
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.utility.SnackbarController

@Composable
fun ChatScreen(navController: NavHostController, snackbarController: SnackbarController?) {
    var role by remember { mutableStateOf("") }
    val authRepo = AuthRepository()
    var error by remember { mutableStateOf("") }
    var showDoctorsList by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    val doctorNames = remember { mutableStateOf<List<String>>(emptyList()) }

    BackHandler(enabled = showDoctorsList) {
        showDoctorsList = false
    }

    LaunchedEffect(Unit) {
        val result = authRepo.getUserRole()
        if (result.isSuccess) {
            role = result.getOrNull() ?: ""
        } else {
            error = result.exceptionOrNull()?.message ?: "Unknown error"
        }

        if (Role.DOCTOR == Role.fromValue(role)) {
            val patientsResult = authRepo.getPatientsFromAppointments()
            if (patientsResult.isSuccess) {
                doctorNames.value = patientsResult.getOrNull()?.distinct() ?: emptyList()
            } else {
                error = patientsResult.exceptionOrNull()?.message ?: "Failed to load patient names"
            }
        } else if (Role.USER == Role.fromValue(role)) {
            val doctorsResult = authRepo.getDoctorsFromAppointments()
            if (doctorsResult.isSuccess) {
                doctorNames.value = doctorsResult.getOrNull()?.distinct() ?: emptyList()
            } else {
                error = doctorsResult.exceptionOrNull()?.message ?: "Failed to load doctor names"
            }
        }
    }

    LaunchedEffect(error) {
        if (error.isNotEmpty()) {
            snackbarController?.showMessage(error)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultBackground)
    ) {
        // Header
        Surface(
            color = DefaultBackground,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Health Chat",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DefaultPrimary
                    )
                )
                Text(
                    text = if (Role.USER == Role.fromValue(role))
                        "Connect with medical professionals"
                    else "Contact your patients",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DefaultOnPrimary.copy(alpha = 0.8f)
                )
            }
        }

        // Main Content
        if (role.isNotEmpty()) {
            if (!showDoctorsList) {
                InitialChatView(
                    role = role,
                    onFindDoctorsClick = { showDoctorsList = true },
                    onUrgentHelpClick = { showSupportDialog = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                DoctorsListView(
                    doctorNames = doctorNames.value,

                    onDoctorSelected = { doctorName ->
                        //tu na razie jest dwa razy doctorName, bedzie jego id
                        navController.navigate("chat/$doctorName/$doctorName")
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DefaultPrimary)
            }
        }
    }

    // Support Dialog
    if (showSupportDialog) {
        SupportDialog(
            onDismiss = { showSupportDialog = false },
            navController = navController
        )
    }
}

@Composable
private fun SupportDialog(
    onDismiss: () -> Unit,
    navController: NavHostController
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Need Help?",
                style = MaterialTheme.typography.titleLarge,
                color = DefaultPrimary
            )
        },
        text = {
            Text(
                text = "Can’t find your preferred doctor? Our support team is here to help! Please contact us via email.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:ladycure_admin@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "Urgent Help Request")
                    }
                    context.startActivity(intent)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultPrimary,
                    contentColor = Color.White
                )
            ) {
                Text("Contact Support")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = DefaultPrimary
                ),
                border = BorderStroke(1.dp, DefaultPrimary)
            ) {
                Text("Cancel")
            }
        },
        containerColor = DefaultBackground
    )
}

@Composable
private fun InitialChatView(
    role: String,
    onFindDoctorsClick: () -> Unit,
    onUrgentHelpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    color = DefaultPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = "Chat illustration",
                tint = DefaultPrimary,
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "No active conversations",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = DefaultOnPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (role == "user") {
                "Start a secure chat with a doctor or specialist to discuss your health concerns"
            } else {
                "Start a secure chat with your patients to discuss their health concerns"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = DefaultOnPrimary.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onFindDoctorsClick,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Find doctors",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (role == "user") {
                        "Browse Your Doctors"
                    } else {
                        "See Assigned Patients"
                    }
                )
            }

            if (role == "user") {
                Button(
                    onClick = onUrgentHelpClick,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = DefaultPrimary
                    ),
                    border = BorderStroke(1.dp, DefaultPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Urgent Help")
                }
            }
        }
    }
}

@Composable
private fun DoctorsListView(
    doctorNames: List<String>,
    onDoctorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(doctorNames) { doctorName ->
            Card(
                onClick = { onDoctorSelected(doctorName) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                    contentColor = DefaultOnPrimary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, DefaultPrimary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(DefaultPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Doctor profile",
                            tint = DefaultPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = doctorName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View chat",
                        tint = DefaultPrimary.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ChatScreenPreview() {
    ChatScreen(navController = rememberNavController(), snackbarController = null)
}