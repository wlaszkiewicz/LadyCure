package com.example.ladycure.chat

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Message(
    val sender: String = "",
    val senderName: String = "",
    val recipient: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val attachmentUrl: String? = null
) {

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "sender" to sender,
            "senderName" to senderName,
            "recipient" to recipient,
            "text" to text,
            "timestamp" to timestamp,
            "attachmentUrl" to attachmentUrl
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): Message {
            return Message(
                sender = map["sender"] as? String ?: "",
                senderName = map["senderName"] as? String ?: "",
                recipient = map["recipient"] as? String ?: "",
                text = map["text"] as? String ?: "",
                timestamp = map["timestamp"] as? Timestamp ?: Timestamp.now(),
                attachmentUrl = map["attachmentUrl"] as? String
            )
        }
    }
}