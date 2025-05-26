package com.example.ladycure.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorChatScreen(
    navController: NavController,
    otherUserId: String,
    otherUserName: String,
    chatRepository: ChatRepository = ChatRepository(),
    chatViewModel: ChatViewModel = ChatViewModel(chatRepository),
) {
    val currentUserId = chatRepository.getCurrentUserId()
    val chatId = listOf(currentUserId, otherUserId).sorted().joinToString("_")

    var messageText by remember { mutableStateOf("") }
    var attachmentUri by remember { mutableStateOf<Uri?>(null) }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { attachmentUri = it }
    }

    LaunchedEffect(chatId) {
        chatViewModel.initializeChat(chatId, listOf(currentUserId, otherUserId))
        chatRepository.getMessages(chatId) { messageList ->
            messages = messageList
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Chat with $otherUserName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column {
                attachmentUri?.let { uri ->
                    AttachmentPreview(
                        uri = uri,
                        onRemove = { attachmentUri = null }
                    )
                }
                MessageInputBar(
                    messageText = messageText,
                    onMessageChange = { messageText = it },
                    onSendMessage = {
                        if (messageText.isNotEmpty() || attachmentUri != null) {
                            scope.launch {
                                try {
                                    val userName = chatRepository.getCurrentUserName()
                                    val message = Message(
                                        sender = currentUserId,
                                        senderName = userName,
                                        recipient = otherUserId,
                                        text = messageText,
                                        timestamp = Timestamp.now(),
                                        attachmentUrl = if (attachmentUri != null) {
                                            chatRepository.uploadFile(attachmentUri!!)
                                        } else null
                                    )

                                    chatRepository.sendMessage(chatId, message)
                                    messageText = ""
                                    attachmentUri = null
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(
                                        "Message could not be sent: ${e.message}"
                                    )
                                }
                            }
                        }
                    },
                    onAttachFile = {
                        filePickerLauncher.launch("*/*")
                    }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(messages.reversed()) { message ->
                MessageBubble(
                    message = message,
                    isCurrentUser = message.sender == currentUserId
                )
            }
        }
    }
}

@Composable
fun MessageInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAttachFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onAttachFile) {
            Icon(
                imageVector = Icons.Default.AttachFile,
                contentDescription = "Attach file"
            )
        }

        OutlinedTextField(
            value = messageText,
            onValueChange = onMessageChange,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            placeholder = { Text("Type a message...") },
            singleLine = false,
            maxLines = 3
        )

        IconButton(
            onClick = onSendMessage,
            enabled = messageText.isNotEmpty(),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send message"
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    val alignment = if (isCurrentUser) Alignment.End else Alignment.Start
    val color = if (isCurrentUser) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surfaceVariant


    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd.takeIf { isCurrentUser } ?: Alignment.CenterStart
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = color)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                if (!isCurrentUser) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(text = message.text)
                message.attachmentUrl?.let { url ->
                    // Display attachment preview or link
                    Text(
                        text = "[Attachment]",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = message.timestamp.toDate().toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun AttachmentPreview(
    uri: Uri,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = uri.lastPathSegment ?: "Attachment",
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove attachment"
            )
        }
    }
}