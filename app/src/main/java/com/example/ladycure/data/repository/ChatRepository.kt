package com.example.ladycure.data.repository

import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.example.ladycure.domain.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Repository responsible for handling chat-related operations such as
 * message sending, file uploads, user information retrieval, and real-time listeners.
 */
class ChatRepository {
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()


    /**
     * Gets the UID of the currently authenticated user.
     *
     * @return The UID of the current user.
     * @throws IllegalStateException If the user is not authenticated.
     */
    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    }

    /**
     * Creates a chat document in Firestore if it doesn't already exist.
     *
     * @param chatId The ID of the chat.
     * @param participants The list of user IDs participating in the chat.
     * @return A [Result] indicating success or failure.
     */
    suspend fun createChatIfNotExists(chatId: String, participants: List<String>): Result<Unit> {
        return try {
            val currentUserName = getCurrentUserName()
            val participantsWithNames = participants.map { participantId ->
                if (participantId == getCurrentUserId()) currentUserName else participantId
            }

            val chatRef = firestore.collection("chats").document(chatId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(chatRef)
                if (!snapshot.exists()) {
                    val chatData = mapOf(
                        "participants" to participants,
                        "createdAt" to System.currentTimeMillis()
                    )
                    transaction.set(chatRef, chatData)
                    Log.d("ChatRepository", "Chat created in transaction: $chatId")
                } else {
                    Log.d("ChatRepository", "Chat already exists (checked in transaction): $chatId")
                }
                null
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error during creating a chat $chatId", e)
            Result.failure(e)
        }
    }

    /**
     * Uploads a file to Firebase Storage and returns the download URL.
     *
     * @param uri The [Uri] of the file to upload.
     * @return The download URL of the uploaded file.
     */
    suspend fun uploadFile(uri: Uri): String {
        val fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(
            null
        )
        val fileName = "${UUID.randomUUID()}${if (fileExtension != null) ".$fileExtension" else ""}"
        val storageRef = storage.reference.child("chat_attachments/$fileName")
        val uploadTask = storageRef.putFile(uri).await()
        return storageRef.downloadUrl.await().toString()
    }

    /**
     * Sends a message to a specific chat.
     *
     * @param chatId The ID of the chat.
     * @param message The [Message] object to send.
     * @return A [Result] indicating success or failure.
     */
    suspend fun sendMessage(chatId: String, message: Message): Result<Unit> {
        return try {
            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(message.toMap())
                .await()
            Log.d("ChatRepository", "Message sent successfully to $chatId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error sending message to $chatId", e)
            Result.failure(e)
        }
    }

    /**
     * Sets up a listener to receive real-time updates for messages in a chat.
     *
     * @param chatId The ID of the chat.
     * @param onMessagesReceived Callback with a list of [Message] objects.
     */
    fun getMessages(chatId: String, onMessagesReceived: (List<Message>) -> Unit) {
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("ChatRepository", "Error listening for chat: $chatId", error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    Message.Companion.fromMap(doc.data ?: return@mapNotNull null)
                } ?: emptyList()

                Log.d("ChatRepository", "Received ${messages.size} messages for $chatId")
                onMessagesReceived(messages)
            }
    }

    /**
     * Retrieves the current user's full name (name + surname).
     *
     * @return The full name or a fallback string if not found.
     */
    suspend fun getCurrentUserName(): String {
        val uid = getCurrentUserId()
        val snapshot = firestore.collection("users").document(uid).get().await()
        val name = snapshot.getString("name") ?: ""
        val surname = snapshot.getString("surname") ?: ""
        return if (name.isNotBlank() && surname.isNotBlank()) "$name $surname"
        else if (name.isNotBlank()) name
        else "the user is not found"
    }

    /**
     * Retrieves the URL of a user's profile picture from Firestore.
     *
     * @param userId The user's UID.
     * @return The profile picture URL or null if not available.
     */
    suspend fun getUserProfilePicture(userId: String): String? {
        return try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            snapshot.getString("profilePictureUrl")
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching profile picture for user $userId", e)
            null
        }
    }

    /**
     * Retrieves a specific user's data from Firestore.
     *
     * @param userId The user's UID.
     * @return A [Result] containing a map of user data or null.
     */
    suspend fun getSpecificUserData(userId: String): Result<Map<String, Any>?> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            if (document.exists()) {
                Result.success(document.data)
            } else {
                Log.d("ChatRepository", "User document for $userId does not exist.")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching specific user data for $userId", e)
            Result.failure(e)
        }
    }

    /**
     * Listens for real-time changes to a user's online status.
     *
     * @param userId The user's UID.
     * @param onStatusChanged Callback with the user's online status.
     */
    fun listenForUserStatus(userId: String, onStatusChanged: (Boolean) -> Unit) {
        firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRepository", "Listen failed", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val isOnline = snapshot.getBoolean("isOnline") ?: false
                    onStatusChanged(isOnline)
                }
            }
    }
}