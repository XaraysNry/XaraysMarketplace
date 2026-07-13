package com.xarays.xaraysmarketplace.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Login : Screen("login")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    object Checkout : Screen("checkout/{productId}") {
        fun createRoute(productId: String) = "checkout/$productId"
    }
    object OrderSuccess : Screen("order_success")
}