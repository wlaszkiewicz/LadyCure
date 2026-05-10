package com.example.ladycure.domain.usecase

import android.net.Uri
import com.example.ladycure.data.repository.ChatRepository
import com.example.ladycure.domain.model.Message
import com.google.firebase.Timestamp
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SendMessageUseCase @Inject constructor(
    private val chatRepo: ChatRepository
) {
    suspend operator fun invoke(
        chatId: String,
        text: String,
        attachmentUri: Uri?,
        attachmentFileName: String?,
        attachmentMimeType: String?,
        otherUserId: String
    ): Result<Unit> {
        return try {
            val senderId = chatRepo.getCurrentUserId()
            val senderName = chatRepo.getCurrentUserName()

            val attachmentUrl = attachmentUri?.let { chatRepo.uploadFile(it) }

            val message = Message(
                sender = senderId,
                senderName = senderName,
                recipient = otherUserId,
                text = text,
                timestamp = Timestamp.now(),
                attachmentUrl = attachmentUrl,
                attachmentFileName = attachmentFileName,
                attachmentMimeType = attachmentMimeType
            )

            chatRepo.sendMessage(chatId, message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
