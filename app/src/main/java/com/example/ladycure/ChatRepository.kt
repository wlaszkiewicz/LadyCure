package com.example.ladycure.repository

import android.net.Uri
import com.example.ladycure.data.Message
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    }

    suspend fun uploadFile(uri: Uri): String {
        val storageRef = storage.reference
        val fileRef = storageRef.child("chat_attachments/${UUID.randomUUID()}")
        val uploadTask = fileRef.putFile(uri).await()
        return fileRef.downloadUrl.await().toString()
    }

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
}