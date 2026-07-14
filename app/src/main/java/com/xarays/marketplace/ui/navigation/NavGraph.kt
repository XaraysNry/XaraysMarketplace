package com.xarays.marketplace.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Login : Screen("login")

    // ⭐ TAMBAHKAN BARIS INI
    object Register : Screen("register")

    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    object Checkout : Screen("checkout/{productId}") {
        fun createRoute(productId: String) = "checkout/$productId"
    }
    object OrderSuccess : Screen("order_success")
}