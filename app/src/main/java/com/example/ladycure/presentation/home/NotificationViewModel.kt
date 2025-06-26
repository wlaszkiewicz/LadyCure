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

/**
 * ViewModel for managing notifications, providing filtering, marking as read/unread, and deletion functionalities.
 */
class NotificationsViewModel : ViewModel() {
    private val _allNotifications = MutableStateFlow<List<Notification>>(emptyList())
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())

    /**
     * Exposes the filtered list of notifications as a [StateFlow].
     */
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)

    /**
     * Exposes the count of unread notifications as a [StateFlow].
     */
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _errors = MutableStateFlow<String?>(null)

    /**
     * Exposes any error messages as a [StateFlow].
     */
    val errors: StateFlow<String?> = _errors.asStateFlow()

    internal var currentFilter: NotificationFilter = NotificationFilter.ALL
    internal var currentType: NotificationType? = null
    val notificationRepo = NotificationRepository()

    init {
        fetchNotifications()
    }

    /**
     * Fetches notifications from the repository and updates the [_allNotifications] and [_unreadCount] accordingly.
     * Applies current filters after fetching.
     */
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

    /**
     * Deletes a notification with the given [notificationId].
     * Updates both the all notifications list and the filtered list.
     * Communicates the deletion to the repository.
     * @param notificationId The ID of the notification to delete.
     */
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

    /**
     * Marks a notification with the given [notificationId] as read.
     * Updates both the all notifications list and the filtered list, and recalculates the unread count.
     * Communicates the change to the repository.
     * @param notificationId The ID of the notification to mark as read.
     */
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

    /**
     * Marks a notification with the given [notificationId] as unread.
     * Updates both the all notifications list and the filtered list, and recalculates the unread count.
     * Communicates the change to the repository.
     * @param notificationId The ID of the notification to mark as unread.
     */
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


    /**
     * Marks all notifications as read.
     * Updates the all notifications list and sets the unread count to 0.
     * Applies current filters after marking all as read.
     * Communicates the change to the repository.
     */
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

    /**
     * Cleans notification titles by removing trailing emojis.
     * @param notifications The list of notifications to clean.
     * @return A new list of notifications with cleaned titles.
     */
    private fun cleanNotifications(notifications: List<Notification>): List<Notification> {
        val emojiRegex = Regex("[\\p{So}\\p{Cn}]+\\s*$")
        return notifications.map { notification ->
            notification.copy(
                title = notification.title.replace(emojiRegex, "").trim()
            )
        }
    }

    /**
     * Sets the notification filter. If the new filter is the same as the current one,
     * it toggles back to [NotificationFilter.ALL].
     * Then applies the filters.
     * @param filter The [NotificationFilter] to set.
     */
    fun setFilter(filter: NotificationFilter) {
        currentFilter = if (currentFilter == filter) {
            NotificationFilter.ALL
        } else {
            filter
        }
        applyFilters()
    }

    /**
     * Sets the notification type filter. If the new type is the same as the current one,
     * it toggles back to `null` (no type filter).
     * Then applies the filters.
     * @param type The [NotificationType] to set, or `null` to clear the type filter.
     */
    fun setTypeFilter(type: NotificationType?) {
        currentType = if (currentType == type) {
            null
        } else {
            type
        }
        applyFilters()
    }


    /**
     * Applies the currently selected [currentFilter] and [currentType] to [_allNotifications]
     * and updates the [_notifications] [StateFlow].
     */
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