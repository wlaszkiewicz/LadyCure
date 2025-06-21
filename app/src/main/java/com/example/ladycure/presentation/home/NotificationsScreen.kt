package com.example.ladycure.presentation.home

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import Green
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ladycure.domain.model.Notification
import com.example.ladycure.domain.model.NotificationType
import com.example.ladycure.utility.SnackbarController
import java.time.format.DateTimeFormatter

@Composable
fun NotificationsScreen(
    navController: NavHostController,
    snackbarController: SnackbarController,
    role: String,
    viewModel: NotificationsViewModel = viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val errors by viewModel.errors.collectAsState()

    val isDoctor = role == "doctor"
    NotificationsContent(
        navController = navController,
        snackbarController = snackbarController,
        notifications = notifications,
        unreadCount = unreadCount,
        isDoctor = isDoctor,
        onNotificationClick = { notification ->
            // Handle notification click
        },
        onReadNotification = { notificationId ->
            viewModel.markAsRead(notificationId)
        },
        onFilterChange = viewModel::setFilter,
        onTypeChange = viewModel::setTypeFilter,
        onMarkAllAsRead = viewModel::markAllAsRead,
        onDeleteNotification = { notificationId ->
            viewModel.deleteNotification(notificationId)
        },
        selectedFilter = viewModel.currentFilter,
        selectedType = viewModel.currentType,
        errors = errors,
    )
}

@Composable
private fun NotificationsContent(
    navController: NavHostController,
    snackbarController: SnackbarController,
    notifications: List<Notification>,
    unreadCount: Int,
    isDoctor: Boolean,
    onNotificationClick: (Notification) -> Unit,
    onReadNotification: (String) -> Unit,
    onFilterChange: (NotificationFilter) -> Unit,
    onTypeChange: (NotificationType?) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onDeleteNotification: (String) -> Unit,
    selectedFilter: NotificationFilter,
    selectedType: NotificationType?,
    errors: String? = null
) {
    LaunchedEffect(errors) {
        errors?.let { snackbarController.showMessage(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultBackground)
    ) {
        NotificationsTopBar(
            navController = navController,
            unreadCount = unreadCount
        )

        NotificationsFilterControls(
            modifier = Modifier.padding(horizontal = 16.dp),
            selectedFilter = selectedFilter,
            onFilterChange = onFilterChange,
            unreadCount = unreadCount,
            onMarkAllAsRead = onMarkAllAsRead,
            isDoctor = isDoctor,
            selectedType = selectedType,
            onTypeSelected = onTypeChange
        )

        if (notifications.isEmpty()) {
            EmptyNotifications()
        } else {
            NotificationsList(
                notifications = notifications,
                onNotificationClick = onNotificationClick,
                modifier = Modifier.padding(horizontal = 16.dp),
                onReadNotification = { notificationId ->
                    onReadNotification(notificationId)
                },
                onDeleteNotification = { notificationId ->
                    onDeleteNotification(notificationId)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsFilterControls(
    modifier: Modifier = Modifier,
    selectedFilter: NotificationFilter,
    onFilterChange: (NotificationFilter) -> Unit,
    unreadCount: Int,
    onMarkAllAsRead: () -> Unit,
    isDoctor: Boolean,
    selectedType: NotificationType?,
    onTypeSelected: (NotificationType?) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isMarkAsReadVisible = unreadCount > 0

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NotificationFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { onFilterChange(filter) },
                        label = { Text(filter.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DefaultPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = DefaultPrimary.copy(alpha = 0.1f),
                            labelColor = DefaultPrimary
                        ),
                        border = null,
                    )
                }
            }

            if (isMarkAsReadVisible) {
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onMarkAllAsRead) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mark all as read")
                }
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val types = if (isDoctor) {
                NotificationType.entries.filter { it.showForDoctor }
            } else {
                NotificationType.entries.filter { it.showForPatient }
            }

            types.forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    label = { Text(type.displayName) },
                    leadingIcon = {
                        Icon(
                            imageVector = type.icon,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                            tint = type.color.copy(alpha = if (selectedType == type) 1f else 0.6f)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = type.color.copy(alpha = 0.15f),
                        selectedLabelColor = type.color,
                        selectedLeadingIconColor = type.color,
                        labelColor = DefaultOnPrimary.copy(alpha = 0.8f)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = type.color.copy(alpha = 0.3f),
                        selectedBorderColor = type.color.copy(alpha = 0.8f),
                        selected = selectedType == type,
                        enabled = true
                    )
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsTopBar(
    navController: NavHostController,
    unreadCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp, start = 4.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DefaultPrimary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Notifications",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DefaultPrimary
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge(containerColor = DefaultPrimary) {
                        Text(
                            text = unreadCount.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Notifications",
                tint = DefaultPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
private fun NotificationsList(
    notifications: List<Notification>,
    onNotificationClick: (Notification) -> Unit,
    onReadNotification: (String) -> Unit,
    modifier: Modifier = Modifier,
    onDeleteNotification: (String) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(notifications.size) { index ->
            val notification = notifications[index]
            val dismissState = rememberDismissState(
                confirmStateChange = { value ->
                    when (value) {
                        DismissValue.DismissedToStart -> {
                            onDeleteNotification(notification.id)
                            false
                        }

                        DismissValue.DismissedToEnd -> {
                            onReadNotification(notification.id)
                            false
                        }

                        else -> false
                    }
                }
            )

            SwipeToDismiss(
                state = dismissState,
                background = {
                    val direction = dismissState.dismissDirection
                    val color = when (direction) {
                        DismissDirection.StartToEnd -> Green.copy(alpha = 0.5f)
                        DismissDirection.EndToStart -> Color.Red.copy(alpha = 0.5f)
                        else -> Color.Transparent
                    }

                    val icon = when (direction) {
                        DismissDirection.StartToEnd -> Icons.Default.Check
                        DismissDirection.EndToStart -> Icons.Default.Delete
                        else -> null
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color, shape = RoundedCornerShape(16.dp))
                            .padding(horizontal = 20.dp),
                        contentAlignment = if (direction == DismissDirection.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                    ) {
                        icon?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = if (direction == DismissDirection.StartToEnd) "Mark as Read" else "Delete",
                                tint = Color.White
                            )
                        }
                    }
                },
                directions = setOf(
                    DismissDirection.StartToEnd, // right swipe to mark as read
                    DismissDirection.EndToStart  // left swipe to delete
                ),
                dismissContent = {
                    NotificationItem(
                        notification = notification,
                        onClick = { onNotificationClick(notification) })
                }
            )
        }
    }
}

@Composable
private fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUnread = !notification.isRead
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isUnread) 3.dp else 1.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false
            ),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUnread) notification.typeEnum.color.copy(alpha = 0.1f) else Color.Transparent
            ),
            border = if (isUnread) BorderStroke(
                1.dp,
                notification.typeEnum.color.copy(alpha = 0.5f)
            ) else null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(notification.typeEnum.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = notification.typeEnum.icon,
                        contentDescription = notification.typeEnum.displayName,
                        tint = notification.typeEnum.color,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Medium,
                                color = DefaultOnPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = notification.timestamp!!.toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("MMM dd")),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DefaultOnPrimary.copy(alpha = 0.6f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = notification.body,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DefaultOnPrimary.copy(alpha = if (isUnread) 0.8f else 0.6f)
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isUnread) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(DefaultPrimary)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyNotifications() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "No notifications",
                tint = DefaultPrimary.copy(alpha = 0.3f),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "No notifications yet",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = DefaultOnPrimary.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = "You'll see important updates here",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = DefaultOnPrimary.copy(alpha = 0.4f)
                )
            )
        }
    }
}

enum class NotificationFilter(val displayName: String) {
    ALL("All"),
    UNREAD("Unread"),
    READ("Read");

    companion object {
        fun byType(type: NotificationType?, baseFilter: NotificationFilter): NotificationFilter {
            return baseFilter
        }
    }
}