package com.xarays.marketplace.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.xarays.marketplace.model.OrderData
import kotlinx.coroutines.tasks.await

class OrderRepository {
    private val ordersCollection = FirebaseFirestore.getInstance().collection("orders")

    suspend fun createOrder(order: OrderData): Result<String> = try {
        val document = ordersCollection.document()
        document.set(
            hashMapOf(
                "productId" to order.productId,
                "productTitle" to order.productTitle,
                "productGame" to order.productGame,
                "totalAmount" to order.totalAmount,
                "buyerName" to order.buyerName,
                "buyerEmail" to order.buyerEmail,
                "buyerPhone" to order.buyerPhone,
                "additionalInfo" to order.additionalInfo,
                "gameId" to order.gameId,
                "paymentMethod" to order.paymentMethod,
                "userId" to order.userId,
                "status" to order.status,
                "orderDate" to order.orderDate,
                "adminNote" to order.adminNote,
                "proofUrl" to order.proofUrl,
                "adminReply" to order.adminReply,
                "updatedAt" to order.updatedAt
            )
        ).await()
        Result.success(document.id)
    } catch (exception: Exception) {
        Result.failure(exception)
    }

    suspend fun getOrdersByUser(userId: String): List<Map<String, Any?>> {
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            snapshot.documents.map { doc ->
                doc.data.orEmpty() + ("id" to doc.id)
            }
        } catch (exception: Exception) {
            throw exception
        }
    }

    suspend fun getAllOrders(): List<Map<String, Any?>> {
        return try {
            val snapshot = ordersCollection
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.map { it.data.orEmpty() + ("id" to it.id) }
        } catch (exception: Exception) {
            emptyList()
        }
    }

    suspend fun updateOrder(orderId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            ordersCollection.document(orderId).update(updates).await()
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
