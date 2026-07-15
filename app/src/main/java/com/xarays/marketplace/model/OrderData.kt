package com.xarays.marketplace.model

data class OrderData(
    val productId: String,
    val productTitle: String,
    val productGame: String,
    val totalAmount: Double,
    val buyerName: String,
    val buyerEmail: String,
    val buyerPhone: String = "",
    val additionalInfo: String = "",
    val gameId: String = "",
    val paymentMethod: String,
    val userId: String,
    val status: String = "MENUNGGU_PEMBAYARAN",
    val orderDate: Long = System.currentTimeMillis(),
    val adminNote: String = "",
    val proofUrl: String = "",
    val adminReply: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
