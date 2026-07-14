package com.xarays.marketplace.model

data class Product(
    val id: String,
    val title: String,
    val game: String,
    val price: Double,
    val description: String,
    val imageRes: Int,
    val type: ProductType,
    val sellerId: String = "admin"
)



