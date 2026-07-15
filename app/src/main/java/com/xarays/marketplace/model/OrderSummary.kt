package com.xarays.marketplace.model

data class OrderSummary(
    val id: String,
    val productTitle: String,
    val productGame: String,
    val totalAmount: Double,
    val buyerName: String,
    val buyerEmail: String,
    val buyerPhone: String,
    val gameId: String,
    val status: String,
    val paymentMethod: String,
    val orderDate: Long,
    val adminNote: String = "",
    val proofUrl: String = "",
    val adminReply: String = ""
)
