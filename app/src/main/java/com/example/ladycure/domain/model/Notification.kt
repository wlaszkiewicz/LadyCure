package com.example.ladycure.domain.model

import BabyBlue
import Green
import Red
import Yellow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.Timestamp

/**
 * Represents a notification sent to users in the system.
 *
 * @property id Unique identifier of the notification.
 * @property title The title or headline of the notification.
 * @property body The detailed message or content of the notification.
 * @property timestamp The time the notification was created or sent.
 * @property isRead Indicates whether the notification has been read by the user.
 * @property type The string identifier of the notification type.
 */
data class Notification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val timestamp: Timestamp? = null,
    val isRead: Boolean = false,
    val type: String = ""
) {
    /**
     * Gets the [NotificationType] enum corresponding to the notification's type string.
     * Defaults to [NotificationType.SYSTEM] if no match is found.
     */
    val typeEnum: NotificationType
        get() = NotificationType.entries.find { it.firestoreName == type }
            ?: NotificationType.SYSTEM


    companion object {
        /**
         * Creates a [Notification] instance from a map representation.
         *
         * @param map A map containing notification data.
         * @return A [Notification] object populated with the map data.
         */
        fun fromMap(map: Map<String, Any>): Notification {
            return Notification(
                title = map["title"] as String,
                body = map["body"] as String,
                timestamp = map["timestamp"] as? Timestamp,
                isRead = map["isRead"] as? Boolean == true,
                type = map["type"] as String
            )
        }
    }
}

/**
 * Enum representing various types of notifications.
 *
 * @property firestoreName The string identifier used in Firestore for this notification type.
 * @property displayName The user-friendly name of the notification type.
 * @property color The color associated with this notification type for UI purposes.
 * @property icon The icon representing this notification type.
 * @property showForPatient Whether this notification type should be shown to patients.
 * @property showForDoctor Whether this notification type should be shown to doctors.
 */
enum class NotificationType(
    val firestoreName: String,
    val displayName: String,
    val color: Color,
    val icon: ImageVector,
    val showForPatient: Boolean,
    val showForDoctor: Boolean
) {
    CONFORMATION(
        firestoreName = "confirmation",
        displayName = "Confirmation",
        color = Green,
        icon = Icons.Default.CheckCircle,
        showForPatient = true,
        showForDoctor = false
    ),
    REMINDER(
        firestoreName = "reminder",
        displayName = "Reminder",
        color = BabyBlue,
        icon = Icons.Default.Alarm,
        showForPatient = true,
        showForDoctor = true
    ),
    CANCELLATION(
        firestoreName = "cancellation",
        displayName = "Cancellation",
        color = Red,
        icon = Icons.Default.Warning,
        showForPatient = true,
        showForDoctor = true
    ),
    REVIEW(
        firestoreName = "feedback",
        displayName = "Review",
        color = Yellow,
        icon = Icons.Default.Star,
        showForPatient = true,
        showForDoctor = true
    ),
    SYSTEM(
        firestoreName = "system",
        displayName = "System",
        color = Color(0xFF9E9E9E),
        icon = Icons.Default.Info,
        showForPatient = true,
        showForDoctor = true
    ),
}