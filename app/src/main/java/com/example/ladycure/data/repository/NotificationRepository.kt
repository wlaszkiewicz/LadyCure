package com.example.ladycure.data.repository

import com.example.ladycure.domain.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()


    fun fetchNotifications(
        onResult: (List<Notification>) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
            ?: return onError("User not authenticated")

        firestore
            .collection("users")
            .document(userId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError("Failed to fetch notifications: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val notifications = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { Notification.fromMap(it).copy(id = doc.id) }
                    }
                    onResult(notifications)
                } else {
                    onResult(emptyList())
                }
            }
    }

    fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            val userId =
                auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
            firestore
                .collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUnreadNotificationsCount(
        onResult: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
            ?: return onError("User not authenticated")

        firestore
            .collection("users")
            .document(userId)
            .collection("notifications")
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError("Failed to fetch notifications: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val count = snapshot.documents.size
                    onResult(count)
                } else {
                    onResult(0)
                }
            }
    }

    fun markNotificationAsUnread(notificationId: String): Result<Unit> {
        return try {
            val userId =
                auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
            firestore
                .collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .update("isRead", false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun markNotificationAsRead(notificationId: String): Result<Unit> {
        return try {
            val userId =
                auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
            firestore
                .collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .update("isRead", true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun markAllNotificationsAsRead(): Result<Unit> {
        return try {
            val userId =
                auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
            firestore
                .collection("users")
                .document(userId)
                .collection("notifications")
                .get()
                .addOnSuccessListener { snapshot ->
                    val batch = firestore.batch()
                    for (doc in snapshot.documents) {
                        batch.update(doc.reference, "isRead", true)
                    }
                    batch.commit()
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}