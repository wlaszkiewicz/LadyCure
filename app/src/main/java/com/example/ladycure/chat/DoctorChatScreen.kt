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
import androidx.compose.material.icons.filled.Phone // Import Phone icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ladycure.R
import com.example.ladycure.data.doctor.Doctor
import com.example.ladycure.repository.UserRepository
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
    userRepository: UserRepository = UserRepository() // Inject UserRepository
) {
    val currentUserId = chatRepository.getCurrentUserId()
    val chatId = listOf(currentUserId, otherUserId).sorted().joinToString("_")
    val context = LocalContext.current


    var messageText by remember { mutableStateOf("") }
    var attachmentUri by remember { mutableStateOf<Uri?>(null) }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var isSending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var otherUserProfilePictureUrl by remember { mutableStateOf<String?>(null) }
    var currentUserProfilePictureUrl by remember { mutableStateOf<String?>(null) }
    var otherUserPhoneNumber by remember { mutableStateOf<String?>(null) }
    var otherUserRole by remember { mutableStateOf<String?>(null) }

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
        // Fetching profile pictures
        otherUserProfilePictureUrl = chatRepository.getUserProfilePicture(otherUserId)
        currentUserProfilePictureUrl = chatRepository.getUserProfilePicture(currentUserId)

        // Fetch phone number
        chatRepository.getSpecificUserData(otherUserId).onSuccess { userData ->
            otherUserPhoneNumber = userData?.get("phone") as? String
            otherUserRole = userData?.get("role") as? String
        }.onFailure { e ->
            snackbarHostState.showSnackbar("Failed to load user data: ${e.message}")
        }
    }

    Scaffold(
        topBar = {
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
                        if (otherUserProfilePictureUrl != null) {
                            AsyncImage(
                                model = otherUserProfilePictureUrl,
                                contentDescription = "Other user profile picture",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                modifier = Modifier.size(50.dp),
                                tint = DefaultOnPrimary.copy(alpha = 0.6f)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.BottomEnd)
                                .background(Color.Green, shape = CircleShape)
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
                            text = otherUserRole?.capitalize(Locale.getDefault()) ?: "User",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }

                    if (otherUserPhoneNumber != null) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${otherUserPhoneNumber}")
                                }
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("No app found to handle calls.")
                                    }
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Call ${otherUserName}",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding()
            ) {
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
                            isSending = true
                            scope.launch {
                                try {
                                    val userName = chatRepository.getCurrentUserName()
                                    val attachmentFileName = if (attachmentUri != null) {
                                        context.contentResolver.query(attachmentUri!!, null, null, null, null)?.use { cursor ->
                                            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                                            cursor.moveToFirst()
                                            cursor.getString(nameIndex)
                                        } ?: "Attachment"
                                    } else null

                                    val attachmentMimeType = if (attachmentUri != null) {
                                        context.contentResolver.getType(attachmentUri!!)
                                    } else null

                                    val message = Message(
                                        sender = currentUserId,
                                        senderName = userName,
                                        recipient = otherUserId,
                                        text = messageText,
                                        timestamp = Timestamp.now(),
                                        attachmentUrl = if (attachmentUri != null) {
                                            chatRepository.uploadFile(attachmentUri!!)
                                        } else null,
                                        attachmentFileName = attachmentFileName,
                                        attachmentMimeType = attachmentMimeType
                                    )

                                    chatRepository.sendMessage(chatId, message)
                                    messageText = ""
                                    attachmentUri = null
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(
                                        "Message could not be sent: ${e.message}"
                                    )
                                } finally {
                                    isSending = false
                                }
                            }
                        }
                    },
                    onAttachFile = {
                        filePickerLauncher.launch("*/*")
                    },
                    isSending = isSending
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
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
            ) {
                items(messages.reversed()) { message ->
                    val senderProfilePictureUrl = if (message.sender == currentUserId) {
                        currentUserProfilePictureUrl
                    } else {
                        otherUserProfilePictureUrl
                    }
                    ModernMessageBubble(
                        message = message,
                        isCurrentUser = message.sender == currentUserId,
                        profilePictureUrl = senderProfilePictureUrl
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
    isSending: Boolean,
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
                ),
                enabled = !isSending
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
                    enabled = !isSending,
                    decorationBox = { innerTextField ->
                        innerTextField()
                    }
                )
            }

            val sendButtonEnabled by rememberUpdatedState(messageText.isNotEmpty() && !isSending)
            val sendButtonColor by animateColorAsState(
                if (sendButtonEnabled) Color.White
                else Color.White.copy(alpha = 0.4f),
                animationSpec = tween(durationMillis = 200),
                label = "SendButtonColor"
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (sendButtonEnabled) Color.White else Grey.copy(alpha = 0.7f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = DefaultPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = onSendMessage,
                        modifier = Modifier.size(48.dp),
                        enabled = sendButtonEnabled,
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
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ModernMessageBubble(
    message: Message,
    isCurrentUser: Boolean,
    profilePictureUrl: String?,
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

    val senderDisplayName = if (isCurrentUser) "Me" else message.senderName
    val profilePictureSize = 36.dp
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isCurrentUser) {
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(profilePictureSize)
                    .clip(CircleShape)
                    .background(DefaultBackground),
                contentAlignment = Alignment.Center
            ) {
                if (profilePictureUrl != null) {
                    AsyncImage(
                        model = profilePictureUrl,
                        contentDescription = "Receiver profile picture",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Receiver profile picture",
                        modifier = Modifier.size(profilePictureSize),
                        tint = DefaultOnPrimary.copy(alpha = 0.6f)
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        Column(
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
        ) {
            Text(
                text = senderDisplayName,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = DefaultPrimary,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(
                    start = 4.dp,
                    end = 4.dp,
                    bottom = 4.dp
                )
            )

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
                    modifier = Modifier
                        .animateContentSize()
                        .widthIn(max = 280.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        if (targetMessage.text.isNotEmpty()) {
                            Text(
                                text = targetMessage.text,
                                style = MaterialTheme.typography.bodyLarge.copy(color = textColor)
                            )
                        }

                        targetMessage.attachmentUrl?.let { url ->
                            val fileName = targetMessage.attachmentFileName ?: "Attachment"
                            val mimeType = targetMessage.attachmentMimeType

                            if (mimeType?.startsWith("image/") == true) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Attached image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                data = Uri.parse(url)
                                                type = mimeType
                                            }
                                            context.startActivity(intent)
                                        },
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                ModernAttachmentPreview(
                                    url = url,
                                    fileName = fileName,
                                    textColor = textColor,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
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

        if (isCurrentUser) {
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(profilePictureSize)
                    .clip(CircleShape)
                    .background(DefaultBackground),
                contentAlignment = Alignment.Center
            ) {
                if (profilePictureUrl != null) {
                    AsyncImage(
                        model = profilePictureUrl,
                        contentDescription = "My profile picture",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "My profile picture",
                        modifier = Modifier.size(profilePictureSize),
                        tint = DefaultOnPrimary.copy(alpha = 0.6f)
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ModernAttachmentPreview(
    uri: Uri,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fileName = remember(uri) {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        } ?: "Attachment"
    }

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
                    text = fileName,
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
    fileName: String,
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
                text = fileName,
                style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun Date.formatTime(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(this)
}