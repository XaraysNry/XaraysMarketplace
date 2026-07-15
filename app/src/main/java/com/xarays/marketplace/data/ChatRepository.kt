package com.xarays.marketplace.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val message: String,
    val orderId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

class ChatRepository {
    private val chatsCollection = FirebaseFirestore.getInstance().collection("chats")

    suspend fun sendMessage(message: ChatMessage): Result<String> = try {
        val doc = chatsCollection.document()
        doc.set(
            hashMapOf(
                "senderId" to message.senderId,
                "senderName" to message.senderName,
                "message" to message.message,
                "orderId" to message.orderId,
                "createdAt" to message.createdAt
            )
        ).await()
        Result.success(doc.id)
    } catch (exception: Exception) {
        Result.failure(exception)
    }

    suspend fun getMessages(): List<ChatMessage> {
        return try {
            val snapshot = chatsCollection.orderBy("createdAt", Query.Direction.ASCENDING).get().await()
            snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                ChatMessage(
                    id = doc.id,
                    senderId = data["senderId"] as? String ?: "",
                    senderName = data["senderName"] as? String ?: "",
                    message = data["message"] as? String ?: "",
                    orderId = data["orderId"] as? String,
                    createdAt = when (val value = data["createdAt"]) {
                        is Long -> value
                        is Number -> value.toLong()
                        else -> System.currentTimeMillis()
                    }
                )
            }
        } catch (exception: Exception) {
            emptyList()
        }
    }
}
