package com.example.ladycure.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.NotificationRepository
import com.example.ladycure.domain.model.Notification
import com.example.ladycure.domain.model.NotificationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {
    private val _allNotifications = MutableStateFlow<List<Notification>>(emptyList()) // Full source
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())     // Filtered
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _errors = MutableStateFlow<String?>(null)
    val errors: StateFlow<String?> = _errors.asStateFlow()

    internal var currentFilter: NotificationFilter = NotificationFilter.ALL
    internal var currentType: NotificationType? = null
    val notificationRepo = NotificationRepository()

    init {
        fetchNotifications()
    }

    private fun fetchNotifications() {
        notificationRepo.fetchNotifications(
            onResult = { fetchedNotifications ->
                _allNotifications.value = cleanNotifications(fetchedNotifications)
                _unreadCount.value = fetchedNotifications.count { !it.isRead }
                applyFilters()
            },
            onError = { error ->
                _errors.value = error
            }
        )
    }

    fun deleteNotification(notificationId: String) {
        _allNotifications.update { list -> list.filterNot { it.id == notificationId } }
        _notifications.update { list -> list.filterNot { it.id == notificationId } }

        viewModelScope.launch {
            notificationRepo.deleteNotification(notificationId)
                .onFailure { e ->
                    _errors.value = "Failed to delete notification: ${e.message}"
                }
        }
    }

    fun markAsRead(notificationId: String) {
        _allNotifications.update { list ->
            list.map { if (it.id == notificationId) it.copy(isRead = true) else it }
        }
        _notifications.update { list ->
            list.map { if (it.id == notificationId) it.copy(isRead = true) else it }
        }

        _unreadCount.value = _allNotifications.value.count { !it.isRead }

        viewModelScope.launch {
            notificationRepo.markNotificationAsRead(notificationId)
                .onFailure { e ->
                    _errors.value = "Failed to mark notification as read: ${e.message}"
                }
        }
    }

    fun markAsUnread(notificationId: String) {
        _allNotifications.update { list ->
            list.map { if (it.id == notificationId) it.copy(isRead = false) else it }
        }
        _notifications.update { list ->
            list.map { if (it.id == notificationId) it.copy(isRead = false) else it }
        }

        _unreadCount.value = _allNotifications.value.count { !it.isRead }

        viewModelScope.launch {
            notificationRepo.markNotificationAsUnread(notificationId)
                .onFailure { e ->
                    _errors.value = "Failed to mark notification as unread: ${e.message}"
                }
        }
    }


    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                notificationRepo.markAllNotificationsAsRead().onSuccess {
                    _allNotifications.update { list ->
                        list.map { it.copy(isRead = true) }
                    }
                    _unreadCount.value = 0
                    applyFilters()
                }.onFailure { e ->
                    _errors.value = "Failed to mark all as read: ${e.message}"
                }
            } catch (e: Exception) {
                _errors.value = "Failed to mark all as read: ${e.message}"
            }
        }
    }

    private fun cleanNotifications(notifications: List<Notification>): List<Notification> {
        val emojiRegex = Regex("[\\p{So}\\p{Cn}]+\\s*$")
        return notifications.map { notification ->
            notification.copy(
                title = notification.title.replace(emojiRegex, "").trim()
            )
        }
    }

    fun setFilter(filter: NotificationFilter) {
        currentFilter = if (currentFilter == filter) {
            NotificationFilter.ALL
        } else {
            filter
        }
        applyFilters()
    }

    fun setTypeFilter(type: NotificationType?) {
        currentType = if (currentType == type) {
            null
        } else {
            type
        }
        applyFilters()
    }


    private fun applyFilters() {
        val base = _allNotifications.value
        val filtered = base.filter { notification ->
            when (currentFilter) {
                NotificationFilter.ALL -> true
                NotificationFilter.READ -> notification.isRead
                NotificationFilter.UNREAD -> !notification.isRead
            } && (currentType == null || notification.typeEnum == currentType)
        }
        _notifications.value = filtered
    }
}
