package com.example.ladycure.chat

import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*

class ChatRepository {
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance("telecure")
    private val usersFirestore = FirebaseFirestore.getInstance("telecure")

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    }

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

    suspend fun uploadFile(uri: Uri): String {
        val fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(
            null
        )
        val fileName = "${UUID.randomUUID()}${if (fileExtension != null) ".$fileExtension" else ""}"
        val storageRef = storage.reference.child("chat_attachments/$fileName")
        val uploadTask = storageRef.putFile(uri).await()
        return storageRef.downloadUrl.await().toString()
    }

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
                    Message.fromMap(doc.data ?: return@mapNotNull null)
                } ?: emptyList()

                Log.d("ChatRepository", "Received ${messages.size} messages for $chatId")
                onMessagesReceived(messages)
            }
    }

    suspend fun getCurrentUserName(): String {
        val uid = getCurrentUserId()
        val snapshot = firestore.collection("users").document(uid).get().await()
        val name = snapshot.getString("name") ?: ""
        val surname = snapshot.getString("surname") ?: ""
        return if (name.isNotBlank() && surname.isNotBlank()) "$name $surname"
        else if (name.isNotBlank()) name
        else "the user is not found"
    }

    suspend fun getUserProfilePicture(userId: String): String? {
        return try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            snapshot.getString("profilePictureUrl")
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching profile picture for user $userId", e)
            null
        }
    }

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
}