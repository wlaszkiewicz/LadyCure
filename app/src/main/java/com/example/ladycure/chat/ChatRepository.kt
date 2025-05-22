package com.example.ladycure.chat

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance("telecure")

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    }

    suspend fun createChat(chatId: String, participants: List<String>): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val chatRef = firestore.collection("chats").document(chatId)

                // Sprawdzenie, czy dokument już istnieje
                val chatSnapshot = transaction.get(chatRef)
                if (chatSnapshot.exists()) {
                    throw Exception("Chat o podanym ID już istnieje")
                }

                // Tworzenie nowego dokumentu w kolekcji "chats"
                val chatData = mapOf(
                    "participants" to participants,
                    "createdAt" to System.currentTimeMillis()
                )
                transaction.set(chatRef, chatData)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadFile(uri: Uri): String {
        val storageRef = storage.reference
        val fileRef = storageRef.child("chat_attachments/${UUID.randomUUID()}")
        val uploadTask = fileRef.putFile(uri).await()
        return fileRef.downloadUrl.await().toString()
    }

    // jak to bylo to pozostawialo in "type a message" wyslaną wiadomosc
//    suspend fun sendMessage(chatId: String, message: Message) {
//        val chatRef = db.collection("chats").document(chatId)
//        val document = chatRef.get().await()
//
//        if (!document.exists()) {
//            chatRef.set(mapOf("createdAt" to System.currentTimeMillis())).await()
//        }
//        chatRef.collection("messages").add(message.toMap()).await()
//    }

    fun sendMessage(chatId: String, message: Message) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message.toMap())
    }

    fun getMessages(chatId: String, onMessagesReceived: (List<Message>) -> Unit) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    Message.fromMap(doc.data ?: return@mapNotNull null)
                } ?: emptyList()

                onMessagesReceived(messages)
            }
    }

    suspend fun getCurrentUserName(): String {
        val uid = getCurrentUserId()
        val snapshot = firestore.collection("users").document(uid).get().await()
        return snapshot.getString("name") ?: "the user is not found"
    }

}