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

data class Notification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val timestamp: Timestamp? = null,
    val isRead: Boolean = false,
    val type: String = ""
) {
    val typeEnum: NotificationType
        get() = NotificationType.entries.find { it.firestoreName == type }
            ?: NotificationType.SYSTEM


    companion object {
        fun fromMap(map: Map<String, Any>): Notification {
            return Notification(
                // id is handled separately, so we don't include it here
                title = map["title"] as String,
                body = map["body"] as String,
                timestamp = map["timestamp"] as? Timestamp,
                isRead = map["isRead"] as? Boolean == true,
                type = map["type"] as String
            )
        }
    }
}

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