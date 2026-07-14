package com.xarays.marketplace.model

data class OrderData(
    val productId: String,
    val buyerName: String,
    val buyerEmail: String,
    val buyerPhone: String,
    val additionalInfo: String = "",
    val orderDate: Long = System.currentTimeMillis()
)