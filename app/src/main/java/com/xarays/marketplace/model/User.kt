package com.xarays.marketplace.model

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "buyer" // "buyer" or "admin"
)