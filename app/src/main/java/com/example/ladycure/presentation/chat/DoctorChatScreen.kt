package com.example.ladycure.presentation.chat

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import Grey
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ladycure.data.repository.ChatRepository
import com.example.ladycure.data.repository.DoctorRepository
import com.example.ladycure.domain.model.Doctor
import com.example.ladycure.domain.model.Message
import com.example.ladycure.presentation.booking.FileTooLargeDialog
import com.example.ladycure.presentation.home.DoctorInfoCard
import com.example.ladycure.utility.PdfUploader
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Composable function for the Doctor Chat Screen.
 *
 * @param navController The NavController for navigation.
 * @param otherUserId The ID of the other user in the chat.
 * @param otherUserName The name of the other user in the chat.
 * @param chatRepository The repository for chat operations.
 * @param chatViewModel The ViewModel for chat operations.
 * @param doctorRepository The repository for doctor-related operations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorChatScreen(
    navController: NavController,
    otherUserId: String,
    otherUserName: String,
    chatRepository: ChatRepository = ChatRepository(),
    chatViewModel: ChatViewModel = ChatViewModel(chatRepository),
    doctorRepository: DoctorRepository = DoctorRepository(),
) {
    val currentUserId = chatRepository.getCurrentUserId()
    val chatId = listOf(currentUserId, otherUserId).sorted().joinToString("_")
    val context = LocalContext.current

    var showFileTooLargeDialog by remember { mutableStateOf(false) }

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

    var showDoctorProfile by remember { mutableStateOf(false) }
    var currentDoctor by remember { mutableStateOf<Doctor?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (PdfUploader.isFileTooLarge(context, it)) {
                showFileTooLargeDialog = true
            } else {
                attachmentUri = it
            }
        }
    }

    LaunchedEffect(chatId) {
        chatViewModel.initializeChat(chatId, listOf(currentUserId, otherUserId))
        chatRepository.getMessages(chatId) { messageList ->
            messages = messageList
        }
        otherUserProfilePictureUrl = chatRepository.getUserProfilePicture(otherUserId)
        currentUserProfilePictureUrl = chatRepository.getUserProfilePicture(currentUserId)

        chatRepository.getSpecificUserData(otherUserId).onSuccess { userData ->
            otherUserPhoneNumber = userData?.get("phone") as? String
            otherUserRole = userData?.get("role") as? String
        }.onFailure { e ->
            snackbarHostState.showSnackbar("Failed to load user data: ${e.message}")
        }
    }

    fun fetchDoctorProfile() {
        scope.launch {
            val result = doctorRepository.getDoctors()
            result.onSuccess { doctors ->
                currentDoctor = doctors.find { it.id == otherUserId }
                showDoctorProfile = true
            }.onFailure {
                snackbarHostState.showSnackbar("Failed to load doctor profile")
            }
        }
    }

    fun onProfileClick() {
        if (currentDoctor != null) {
            showDoctorProfile = true
        } else {
            fetchDoctorProfile()
        }
    }

    fun sendMessage() {
        if (messageText.isNotEmpty() || attachmentUri != null) {
            isSending = true
            scope.launch {
                try {
                    val userName = chatRepository.getCurrentUserName()
                    val attachmentFileName = if (attachmentUri != null) {
                        context.contentResolver.query(attachmentUri!!, null, null, null, null)
                            ?.use { cursor ->
                                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .background(DefaultBackground)
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (otherUserProfilePictureUrl != null) {
                            AsyncImage(
                                model = otherUserProfilePictureUrl,
                                contentDescription = "Other user profile picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
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

                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onProfileClick() }
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
                                    setData("tel:${otherUserPhoneNumber}".toUri())
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
                                contentDescription = "Call $otherUserName",
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
                if (attachmentUri != null) {
                    ModernAttachmentPreview(
                        uri = attachmentUri!!,
                        onRemove = { attachmentUri = null }
                    )
                }
                ModernMessageInputBar(
                    messageText = messageText,
                    onMessageChange = { messageText = it },
                    onSendMessage = { sendMessage() },
                    onAttachFile = {
                        filePickerLauncher.launch("*/*")
                    },
                    isSending = isSending,
                    hasAttachment = attachmentUri != null
                )

                if (showFileTooLargeDialog) {
                    FileTooLargeDialog(
                        onDismiss = { showFileTooLargeDialog = false }
                    )
                }
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
                        profilePictureUrl = senderProfilePictureUrl,
                        onProfileClick = ::onProfileClick
                    )
                }
            }
        }
        if (showDoctorProfile && currentDoctor != null) {
            DoctorProfileDialog(
                doctor = currentDoctor!!,
                onDismiss = { showDoctorProfile = false },
                onBookAppointment = {
                    showDoctorProfile = false
                    navController.navigate("services/${currentDoctor!!.id}")
                }
            )
        }
    }
}

/**
 * Composable function for the modern message input bar.
 *
 * @param messageText The current text in the message input field.
 * @param onMessageChange Callback for when the message text changes.
 * @param onSendMessage Callback for when the send button is clicked.
 * @param onAttachFile Callback for when the attach file button is clicked.
 * @param isSending Indicates if a message is currently being sent.
 * @param hasAttachment Indicates if there is an attachment.
 * @param modifier The modifier for this composable.
 */
@Composable
fun ModernMessageInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAttachFile: () -> Unit,
    isSending: Boolean,
    hasAttachment: Boolean,
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
                        text = if (hasAttachment) "Add message (optional)" else "Type your message...",
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

            val sendButtonEnabled by rememberUpdatedState(
                (messageText.isNotEmpty() || hasAttachment) && !isSending
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

/**
 * Composable function for a modern message bubble in the chat.
 *
 * @param message The message to display.
 * @param isCurrentUser True if the message was sent by the current user, false otherwise.
 * @param profilePictureUrl The URL of the sender's profile picture.
 * @param modifier The modifier for this composable.
 * @param snackbarHostState The SnackbarHostState to show snackbars.
 * @param scope The CoroutineScope for launching coroutines.
 * @param onProfileClick Callback for when the profile picture is clicked.
 */
@SuppressLint("ServiceCast")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ModernMessageBubble(
    message: Message,
    isCurrentUser: Boolean,
    profilePictureUrl: String?,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    scope: CoroutineScope = rememberCoroutineScope(),
    onProfileClick: () -> Unit
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
    var showImageDialog by remember { mutableStateOf(false) }
    var currentImageUrl by remember { mutableStateOf("") }

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
                    .background(DefaultBackground)
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                if (profilePictureUrl != null) {
                    AsyncImage(
                        model = profilePictureUrl,
                        contentDescription = "Receiver profile picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
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
                                Column {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = "Attached image",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                currentImageUrl = url
                                                showImageDialog = true
                                            },
                                        contentScale = ContentScale.Crop
                                    )
                                    Text(
                                        text = "Tap to view/download",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = textColor.copy(alpha = 0.6f)
                                        ),
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }
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
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
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

    if (showImageDialog) {
        Dialog(
            onDismissRequest = { showImageDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f),
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AsyncImage(
                        model = currentImageUrl,
                        contentDescription = "Full image preview",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentScale = ContentScale.Fit
                    )

                    IconButton(
                        onClick = {
                            val downloadManager =
                                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            val request = DownloadManager.Request(currentImageUrl.toUri())
                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                .setTitle("Downloading image")
                                .setDescription("Image from chat")

                            downloadManager.enqueue(request)
                            showImageDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Download started")
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(15.dp)
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                shape = CircleShape
                            ),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = "Download",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = { showImageDialog = false },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(15.dp)
                            .size(48.dp)
                            .background(
                                color = Color.Gray.copy(alpha = 0.6f),
                                shape = CircleShape
                            ),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Composable function for displaying a modern attachment preview.
 *
 * @param uri The URI of the attachment.
 * @param onRemove Callback for when the remove attachment button is clicked.
 * @param modifier The modifier for this composable.
 */
@Composable
fun ModernAttachmentPreview(
    uri: Uri,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fileName = remember(uri) {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
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

/**
 * Composable function for displaying a modern attachment preview from a URL.
 *
 * @param url The URL of the attachment.
 * @param fileName The name of the attachment file.
 * @param textColor The color of the text.
 * @param modifier The modifier for this composable.
 */
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
                    setData(url.toUri())
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

/**
 * Composable function for displaying a doctor profile in a dialog.
 *
 * @param doctor The doctor to display.
 * @param onDismiss Callback for when the dialog is dismissed.
 * @param onBookAppointment Callback for when the "Book Appointment" button is clicked.
 * @param modifier The modifier for this composable.
 */
@Composable
fun DoctorProfileDialog(
    doctor: Doctor,
    onDismiss: () -> Unit,
    onBookAppointment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = DefaultBackground
        ) {
            Column {
                DoctorInfoCard(
                    doctor = doctor,
                    onSelect = onBookAppointment,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}


/**
 * Formats a Date object to a time string (HH:mm).
 *
 * @return The formatted time string.
 */
fun Date.formatTime(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(this)
}