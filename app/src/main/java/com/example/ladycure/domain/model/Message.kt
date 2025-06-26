package com.example.ladycure.domain.model

import com.google.firebase.Timestamp

/**
 * Represents a message exchanged between users in the system.
 *
 * @property sender The unique identifier of the user who sent the message.
 * @property senderName The display name of the sender.
 * @property recipient The unique identifier of the message recipient.
 * @property text The textual content of the message.
 * @property timestamp The time at which the message was sent.
 * @property attachmentUrl Optional URL to an attachment associated with the message.
 * @property attachmentFileName Optional filename of the attached file.
 * @property attachmentMimeType Optional MIME type of the attachment.
 */
data class Message(
    val sender: String = "",
    val senderName: String = "",
    val recipient: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.Companion.now(),
    val attachmentUrl: String? = null,
    val attachmentFileName: String? = null,
    val attachmentMimeType: String? = null
) {

    /**
     * Converts the [Message] instance into a map suitable for storage or transmission.
     *
     * @return A map representing the message fields.
     */
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

    /**
     * Creates a [Message] instance from a map of key-value pairs.
     *
     * @param map A map containing message data.
     * @return A [Message] object populated with data from the map.
     */
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