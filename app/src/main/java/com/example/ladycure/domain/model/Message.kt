package com.example.ladycure.domain.model

import com.google.firebase.Timestamp

data class Message(
    val sender: String = "", // User ID of the sender
    val senderName: String = "",
    val recipient: String = "", // User ID of the recipient
    val text: String = "",
    val timestamp: Timestamp = Timestamp.Companion.now(),
    val attachmentUrl: String? = null,
    val attachmentFileName: String? = null,
    val attachmentMimeType: String? = null
) {

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "sender" to sender,
            "senderName" to senderName,
            "recipient" to recipient,
            "text" to text,
            "timestamp" to timestamp,
            "attachmentUrl" to attachmentUrl,
            "attachmentFileName" to attachmentFileName,
            "attachmentMimeType" to attachmentMimeType
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): Message {
            return Message(
                sender = map["sender"] as? String ?: "",
                senderName = map["senderName"] as? String ?: "",
                recipient = map["recipient"] as? String ?: "",
                text = map["text"] as? String ?: "",
                timestamp = map["timestamp"] as? Timestamp ?: Timestamp.Companion.now(),
                attachmentUrl = map["attachmentUrl"] as? String,
                attachmentFileName = map["attachmentFileName"] as? String,
                attachmentMimeType = map["attachmentMimeType"] as? String
            )
        }
    }
}