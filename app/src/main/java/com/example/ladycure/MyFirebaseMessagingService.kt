package com.example.ladycure

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import androidx.compose.material.ExperimentalMaterialApi
import androidx.core.app.NotificationCompat
import com.example.ladycure.data.repository.AuthRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

/**
 * A service for handling Firebase Cloud Messaging (FCM) events.
 *
 * This class listens for new FCM messages and handles token updates.
 * When a message is received, it generates and displays a local notification.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    val authRepo = AuthRepository()

    /**
     * Called when a message is received.
     *
     * @param remoteMessage The message received from FCM.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let { message ->
            sendNotification(message)
        }
    }

    /**
     * Called when a new FCM token is generated.
     *
     * @param token The new FCM token.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val repo = AuthRepository()
        repo.updateFcmToken(token)
    }

    /**
     * Builds and shows a local notification using the received FCM notification data.
     *
     * @param message The notification content from FCM.
     */
    @OptIn(ExperimentalMaterialApi::class)
    private fun sendNotification(message: RemoteMessage.Notification) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, FLAG_IMMUTABLE
        )

        val channelId = "default_notification_channel"

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setSmallIcon(R.drawable.icon)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val CHANNEL_NAME = "Default Channel"
        val IMPORTANCE_DEFAULT = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, CHANNEL_NAME, IMPORTANCE_DEFAULT)
        manager.createNotificationChannel(channel)

        manager.notify(Random.nextInt(), notificationBuilder.build())
    }
}