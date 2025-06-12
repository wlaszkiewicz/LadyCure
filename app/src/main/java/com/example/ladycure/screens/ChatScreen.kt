package com.example.ladycure.screens

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.ladycure.repository.AppointmentRepository
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.repository.UserRepository
import com.example.ladycure.utility.SnackbarController
import kotlinx.coroutines.launch

data class ChatParticipantInfo(
    val uid: String,
    val fullName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavHostController, snackbarController: SnackbarController?) {
    var role by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingAdditional by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var showParticipantsView by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }

    val activeParticipants = remember { mutableStateOf<List<ChatParticipantInfo>>(emptyList()) }
    val allPossibleParticipants =
        remember { mutableStateOf<List<ChatParticipantInfo>>(emptyList()) }

    val context = LocalContext.current

    val authRepo = AuthRepository()
    val userRepo = UserRepository()
    val appointmentRepo = AppointmentRepository()

    val loadPossibleParticipants = remember(role) {
        suspend {
            isLoadingAdditional = true
            try {
                val result = if (Role.DOCTOR == Role.fromValue(role)) {
                    appointmentRepo.getPatientsFromAppointmentsWithUids()
                } else {
                    appointmentRepo.getDoctorsFromAppointmentsWithUids()
                }

                if (result.isSuccess) {
                    allPossibleParticipants.value = result.getOrNull()?.distinct() ?: emptyList()
                } else {
                    error = result.exceptionOrNull()?.message ?: "Failed to load participants"
                }
            } finally {
                isLoadingAdditional = false
            }
        }
    }

    LaunchedEffect(Unit) {
        val roleResult = userRepo.getUserRole()
        if (roleResult.isSuccess) {
            role = roleResult.getOrNull() ?: ""
        } else {
            error = roleResult.exceptionOrNull()?.message ?: "Unknown error"
        }
        val activeResult = appointmentRepo.getActiveChatParticipants()
        if (activeResult.isSuccess) {
            activeParticipants.value = activeResult.getOrNull() ?: emptyList()
        } else {
            error = activeResult.exceptionOrNull()?.message ?: "Failed to load active chats"
        }

        isLoading = false
    }

    LaunchedEffect(showParticipantsView, role) {
        if (showParticipantsView && allPossibleParticipants.value.isEmpty() && !isLoadingAdditional) {
            loadPossibleParticipants()
        }
    }

    LaunchedEffect(error) {
        if (error.isNotEmpty()) {
            snackbarController?.showMessage(error)
            error = ""
        }
    }

    BackHandler(enabled = showParticipantsView) {
        showParticipantsView = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground)
        ) {
            Surface(
                color = DefaultBackground,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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

                    if (!isLoading && (activeParticipants.value.isNotEmpty() || showParticipantsView)) {
                        IconButton(
                            onClick = { showParticipantsView = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(DefaultPrimary.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add conversation",
                                tint = DefaultPrimary
                            )
                        }
                    }
                }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DefaultPrimary)
                    }
                }

                showParticipantsView -> {
                    ParticipantsFullScreenView(
                        role = role,
                        participants = allPossibleParticipants.value.filter { allParticipant ->
                            activeParticipants.value.none { it.uid == allParticipant.uid }
                        },
                        isLoading = isLoadingAdditional,
                        onBack = { showParticipantsView = false },
                        onParticipantSelected = { participant ->
                            showParticipantsView = false
                            val encodedName = Uri.encode(participant.fullName)
                            navController.navigate("chat/${participant.uid}/$encodedName")
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                activeParticipants.value.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(activeParticipants.value) { participant ->
                            ChatParticipantItem(
                                participant = participant,
                                onClick = {
                                    val encodedName = Uri.encode(participant.fullName)
                                    navController.navigate("chat/${participant.uid}/$encodedName")
                                }
                            )
                        }
                    }
                }

                else -> {
                    InitialChatView(
                        role = role,
                        onFindDoctorsClick = { showParticipantsView = true },
                        onUrgentHelpClick = { showSupportDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (showSupportDialog) {
            SupportDialog(
                onDismiss = { showSupportDialog = false },
                navController = navController
            )
        }
    }
}

@Composable
private fun ParticipantsFullScreenView(
    role: String,
    participants: List<ChatParticipantInfo>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onParticipantSelected: (ChatParticipantInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DefaultBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = DefaultPrimary
                )
            }

            Text(
                text = if (Role.USER == Role.fromValue(role))
                    "Select Doctor"
                else "Select Patient",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = DefaultPrimary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DefaultPrimary)
            }
        } else {
            if (participants.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "No contacts",
                            tint = DefaultPrimary.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (Role.USER == Role.fromValue(role))
                                "No available doctors"
                            else "No available patients",
                            color = DefaultOnPrimary.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(participants) { participant ->
                        ChatParticipantItem(
                            participant = participant,
                            onClick = { onParticipantSelected(participant) },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun ChatParticipantItem(
    participant: ChatParticipantInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = DefaultOnPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(0.5.dp, DefaultPrimary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(DefaultPrimary.copy(alpha = 0.05f))
                    .border(
                        width = 1.dp,
                        color = DefaultPrimary.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "User profile icon",
                    tint = DefaultPrimary.copy(alpha = 0.6f),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = participant.fullName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = DefaultPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Available now",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DefaultOnPrimary.copy(alpha = 0.6f)
                    )
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View chat",
                tint = DefaultPrimary.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
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
                onClick = onFindDoctorsClick as () -> Unit,
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
                    contentDescription = "Find contacts",
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
    participants: List<ChatParticipantInfo>,
    onParticipantSelected: (ChatParticipantInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(participants) { participant ->
            Card(
                onClick = { onParticipantSelected(participant) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                    contentColor = DefaultOnPrimary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(0.5.dp, DefaultPrimary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(DefaultPrimary.copy(alpha = 0.05f))
                            .border(
                                width = 1.dp,
                                color = DefaultPrimary.copy(alpha = 0.2f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User profile icon",
                            tint = DefaultPrimary.copy(alpha = 0.6f),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = participant.fullName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = DefaultPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Available now", // This might need a real-time status
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DefaultOnPrimary.copy(alpha = 0.6f)
                            )
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View chat",
                        tint = DefaultPrimary.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 8.dp)
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