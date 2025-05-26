package com.example.ladycure.chat

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import Grey
import Pink10
import android.content.Intent
import androidx.compose.material3.TextFieldDefaults
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ladycure.R
import com.example.ladycure.data.doctor.Doctor
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            // Modern header with doctor info
            Surface(
                color = DefaultPrimary,
                tonalElevation = 4.dp,
                modifier = Modifier.shadow(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Profile picture with online status
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .background(DefaultBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            modifier = Modifier.size(50.dp),
                            tint = DefaultOnPrimary.copy(alpha = 0.6f)
                        )

                        // Online status indicator
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.BottomEnd)
                                .background(Color.White, shape = CircleShape)
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Green, shape = CircleShape)
                                    .border(
                                        width = 1.dp,
                                        color = DefaultPrimary,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = otherUserName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Doctor",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column {
                attachmentUri?.let { uri ->
                    ModernAttachmentPreview(
                        uri = uri,
                        onRemove = { attachmentUri = null }
                    )
                }
                ModernMessageInputBar(
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DefaultBackground,
                            DefaultBackground.copy(alpha = 0.9f)
                        )
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(messages.reversed()) { message ->
                    ModernMessageBubble(
                        message = message,
                        isCurrentUser = message.sender == currentUserId
                    )
                }
            }
        }
    }
}

@Composable
fun ModernMessageInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAttachFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        tonalElevation = 8.dp,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = DefaultPrimary
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onAttachFile,
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach file",
                    tint = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (messageText.isEmpty()) {
                    Text(
                        text = "Type your message...",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }

                BasicTextField(
                    value = messageText,
                    onValueChange = onMessageChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White
                    ),
                    maxLines = 3,
                    decorationBox = { innerTextField ->
                        innerTextField()
                    }
                )
            }

            val sendButtonEnabled by rememberUpdatedState(messageText.isNotEmpty())
            val sendButtonColor by animateColorAsState(
                if (sendButtonEnabled) Color.White
                else Color.White.copy(alpha = 0.4f),
                animationSpec = tween(durationMillis = 200),
                label = "SendButtonColor"
            )

            IconButton(
                onClick = onSendMessage,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color.White,
                        shape = CircleShape
                    ),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = DefaultPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send message",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ModernMessageBubble(
    message: Message,
    isCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    val bubbleColor by animateColorAsState(
        if (isCurrentUser) DefaultPrimary
        else MaterialTheme.colorScheme.surface,
        label = "Bubble color"
    )
    val textColor by animateColorAsState(
        if (isCurrentUser) Color.White
        else DefaultOnPrimary,
        label = "Text color"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        if (!isCurrentUser) {
            Text(
                text = message.senderName,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = DefaultPrimary,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
            )
        }

        // Animated message bubble
        AnimatedContent(
            targetState = message,
            transitionSpec = {
                fadeIn() with fadeOut()
            },
            label = "Message bubble animation"
        ) { targetMessage ->
            Card(
                shape = RoundedCornerShape(
                    topStart = if (isCurrentUser) 16.dp else 4.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = if (isCurrentUser) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(containerColor = bubbleColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.animateContentSize()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = targetMessage.text,
                        style = MaterialTheme.typography.bodyLarge.copy(color = textColor)
                    )

                    targetMessage.attachmentUrl?.let { url ->
                        ModernAttachmentPreview(
                            url = url,
                            textColor = textColor,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Text(
                        text = targetMessage.timestamp.toDate().formatTime(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = textColor.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernAttachmentPreview(
    uri: Uri,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = DefaultPrimary.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, DefaultPrimary.copy(alpha = 0.1f)),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attachment",
                    tint = DefaultPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = uri.lastPathSegment ?: "Attachment",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DefaultOnPrimary
                    )
                )
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove attachment",
                    tint = DefaultOnPrimary.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun ModernAttachmentPreview(
    url: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                }
                context.startActivity(intent)
            },
        color = textColor.copy(alpha = 0.28f),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AttachFile,
                contentDescription = "Attachment",
                tint = textColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "View Attachment",
                style = MaterialTheme.typography.bodyMedium.copy(color = textColor)
            )
        }
    }
}

fun Date.formatTime(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(this)
}