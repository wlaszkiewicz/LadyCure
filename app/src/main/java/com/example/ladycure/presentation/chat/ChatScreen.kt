package com.example.ladycure.presentation.chat

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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.ladycure.domain.model.Role
import com.example.ladycure.ui.theme.DefaultBackground
import com.example.ladycure.ui.theme.DefaultOnPrimary
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import com.example.ladycure.utility.SnackbarController
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class ChatParticipantInfo(
    val uid: String,
    val fullName: String,
    val specialty: String? = null,
    //val isOnline: Boolean = false,
    val lastSeen: Long? = null,
    val lastMessage: String? = null,
    val lastMessageTime: Long? = null,
    val lastMessageSender: String? = null,
    val unreadCount: Int = 0,
    val lastMessageSenderName: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavHostController,
    snackbarController: SnackbarController?,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val dimens = rememberResponsiveDimens()
    var showParticipantsView by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Unread")
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(showParticipantsView, viewModel.role) {
        if (showParticipantsView && viewModel.allPossibleParticipants.isEmpty()) {
            viewModel.loadPossibleParticipants()
        }
    }

    LaunchedEffect(viewModel.error) {
        if (viewModel.error.isNotEmpty()) {
            snackbarController?.showMessage(viewModel.error)
            viewModel.clearError()
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
                Column {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = dimens.w(0.039f),
                            vertical = dimens.h(0.017f)
                        ),
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
                                text = if (Role.USER == Role.fromValue(viewModel.role))
                                    "Connect with medical professionals"
                                else "Contact your patients",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DefaultOnPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }

                    SearchBar(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.padding(horizontal = dimens.w(0.039f), vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimens.w(0.039f), vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filters.forEach { filterOption ->
                            DateFilterChip(
                                label = filterOption,
                                selected = filter == filterOption,
                                onSelected = { filter = filterOption }
                            )
                        }
                    }
                }
            }

            when {
                viewModel.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DefaultPrimary)
                    }
                }

                showParticipantsView -> {
                    ParticipantsFullScreenView(
                        role = viewModel.role,
                        participants = viewModel.allPossibleParticipants.filter { allParticipant ->
                            viewModel.activeParticipants.none { it.uid == allParticipant.uid }
                        },
                        isLoading = viewModel.isLoadingAdditional,
                        onBack = { showParticipantsView = false },
                        onParticipantSelected = { participant ->
                            showParticipantsView = false
                            val encodedName = Uri.encode(participant.fullName)
                            navController.navigate("chat/${participant.uid}/$encodedName")
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                viewModel.activeParticipants.isNotEmpty() -> {
                    val filteredParticipants = viewModel.activeParticipants
                        .filter { participant ->
                            (searchQuery.isEmpty() ||
                                    participant.fullName.contains(
                                        searchQuery,
                                        ignoreCase = true
                                    )) &&
                                    when (filter) {
//                                        "Online" -> participant.isOnline
//                                        "Offline" -> !participant.isOnline
                                        "Unread" -> participant.unreadCount > 0
                                        else -> true // "All"
                                    }
                        }
                        .sortedByDescending { it.lastMessageTime ?: 0 }

                    if (filteredParticipants.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No matching conversations",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DefaultOnPrimary.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .padding(horizontal = dimens.w(0.039f), vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredParticipants) { participant ->
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
                }

                else -> {
                    InitialChatView(
                        role = viewModel.role,
                        onFindDoctorsClick = { showParticipantsView = true },
                        onUrgentHelpClick = { showSupportDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (!viewModel.isLoading && !showParticipantsView) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = dimens.h(0.057f), end = dimens.w(0.088f)),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = { showParticipantsView = true },
                    containerColor = DefaultPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.size(dimens.w(0.136f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New chat",
                        modifier = Modifier.size(24.dp)
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
    val dimens = rememberResponsiveDimens()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DefaultBackground)
            .padding(horizontal = dimens.w(0.039f), vertical = dimens.h(0.017f))
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

            Spacer(modifier = Modifier.width(dimens.w(0.117f)))
        }

        Spacer(modifier = Modifier.height(dimens.h(0.017f)))

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
                            modifier = Modifier.size(dimens.w(0.156f))
                        )
                        Spacer(modifier = Modifier.height(dimens.h(0.017f)))
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
                            modifier = Modifier.padding(horizontal = dimens.w(0.019f))
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
    val dimens = rememberResponsiveDimens()
    val itemViewModel: ChatParticipantItemViewModel = hiltViewModel(key = participant.uid)

    LaunchedEffect(participant.uid) {
        itemViewModel.loadData(participant.uid)
    }

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
                .padding(horizontal = dimens.w(0.039f), vertical = dimens.h(0.017f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(dimens.w(0.136f))) {
                Box(
                    modifier = Modifier
                        .size(dimens.w(0.136f))
                        .clip(CircleShape)
                        .background(DefaultPrimary.copy(alpha = 0.05f))
                        .border(
                            width = 1.dp,
                            color = DefaultPrimary.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (itemViewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = DefaultPrimary,
                            strokeWidth = 2.dp
                        )
                    } else if (itemViewModel.profilePictureUrl != null) {
                        AsyncImage(
                            model = itemViewModel.profilePictureUrl,
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile icon",
                            tint = DefaultPrimary.copy(alpha = 0.6f),
                            modifier = Modifier.size(dimens.w(0.097f))
                        )
                    }
                }

//                Box(
//                    modifier = Modifier
//                        .size(12.dp)
//                        .align(Alignment.BottomEnd)
//                        .background(
//                            color = if (isOnline) Color.Green else Color.Red,
//                            shape = CircleShape
//                        )
//                        .border(
//                            width = 1.dp,
//                            color = Color.White,
//                            shape = CircleShape
//                        )
//                )
            }

            Spacer(modifier = Modifier.width(dimens.w(0.039f)))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = participant.fullName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = DefaultPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                participant.specialty?.let { specialty ->
                    Text(
                        text = specialty,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DefaultOnPrimary.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                participant.lastMessage?.let { message ->
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val timeText = participant.lastMessageTime?.let { time ->
                            formatMessageTimeWithDate(time)
                        } ?: ""

                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle(
                                        color = DefaultPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                ) {
                                    append("${participant.lastMessageSender}: ")
                                }
                                append(message)
                                withStyle(
                                    style = SpanStyle(
                                        color = DefaultOnPrimary.copy(alpha = 0.6f)
                                    )
                                ) {
                                    append(" • $timeText")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatMessageTimeWithDate(timestamp: Long): String {
    val date = Date(timestamp)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dayFormat = SimpleDateFormat("d", Locale.getDefault())
    val monthNameFormat = SimpleDateFormat("MMM", Locale.ENGLISH)

    return "${timeFormat.format(date)}, ${dayFormat.format(date)} ${monthNameFormat.format(date)}"
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
                        setData("mailto:ladycure_admin@gmail.com".toUri())
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
    val dimens = rememberResponsiveDimens()
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(dimens.w(0.487f))
                .background(
                    color = DefaultPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = dimens.w(0.058f), vertical = dimens.h(0.026f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = "Chat illustration",
                tint = DefaultPrimary,
                modifier = Modifier.size(dimens.w(0.195f))
            )
        }

        Spacer(modifier = Modifier.height(dimens.h(0.035f)))

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
            modifier = Modifier.padding(horizontal = dimens.w(0.058f))
        )

        Spacer(modifier = Modifier.height(dimens.h(0.035f)))

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onFindDoctorsClick as () -> Unit,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(dimens.h(0.061f)),
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
                        .height(dimens.h(0.061f)),
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
    val dimens = rememberResponsiveDimens()
    LazyColumn(
        modifier = modifier.padding(horizontal = dimens.w(0.039f), vertical = 8.dp),
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
                        .padding(horizontal = dimens.w(0.039f), vertical = dimens.h(0.017f)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(dimens.w(0.136f))
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
                            modifier = Modifier.size(dimens.w(0.097f))
                        )
                    }

                    Spacer(modifier = Modifier.width(dimens.w(0.039f)))

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

@Composable
private fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = rememberResponsiveDimens()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(dimens.h(0.061f)),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = DefaultBackground,
            unfocusedContainerColor = DefaultBackground,
            focusedIndicatorColor = DefaultPrimary,
            unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.3f),
            focusedTextColor = DefaultOnPrimary,
            unfocusedTextColor = DefaultOnPrimary
        ),
        placeholder = {
            Text(
                "Search conversations...",
                color = DefaultOnPrimary.copy(alpha = 0.5f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = DefaultOnPrimary,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = DefaultOnPrimary.copy(alpha = 0.5f)
                    )
                }
            }
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge
    )
}

@Composable
fun DateFilterChip(
    label: String,
    selected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = rememberResponsiveDimens()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) DefaultPrimary else DefaultPrimary.copy(alpha = 0.1f),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) DefaultPrimary else DefaultPrimary.copy(alpha = 0.3f)
        ),
        onClick = onSelected
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Color.White else DefaultOnPrimary,
            modifier = Modifier.padding(horizontal = dimens.w(0.039f), vertical = 8.dp)
        )
    }
}

@Preview
@Composable
fun ChatScreenPreview() {
    ChatScreen(navController = rememberNavController(), snackbarController = null)
}