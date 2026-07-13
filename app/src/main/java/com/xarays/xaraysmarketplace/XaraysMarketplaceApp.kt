package com.xarays.xaraysmarketplace

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.xarays.xaraysmarketplace.model.Product
import com.xarays.xaraysmarketplace.model.ProductType
import com.xarays.xaraysmarketplace.ui.navigation.Screen
import com.xarays.xaraysmarketplace.ui.screens.*
import com.xarays.xaraysmarketplace.ui.theme.GameMarketplaceTheme

@Composable
fun XaraysMarketplaceApp() {
    GameMarketplaceTheme {
        val navController = rememberNavController()

        // Sample products data
        val products = remember {
            listOf(
                Product(
                    id = "1",
                    title = "Akun Mobile Legends Mythic",
                    game = "Mobile Legends",
                    price = 500000.0,
                    description = "Akun ML rank Mythic, skin limited, hero lengkap. Sudah include email dan bisa di-bind ulang.",
                    imageRes = 0,
                    type = ProductType.ACCOUNT
                ),
                Product(
                    id = "2",
                    title = "Top Up Free Fire 1000 Diamond",
                    game = "Free Fire",
                    price = 150000.0,
                    description = "Top up diamond Free Fire 1000 diamond. Proses cepat 5-10 menit. Masukkan ID player saat checkout.",
                    imageRes = 0,
                    type = ProductType.TOPUP
                ),
                Product(
                    id = "3",
                    title = "Akun PUBG Mobile Conqueror",
                    game = "PUBG Mobile",
                    price = 750000.0,
                    description = "Akun PUBG rank Conqueror season lalu. Skin rare, UC balance masih ada. Full access.",
                    imageRes = 0,
                    type = ProductType.ACCOUNT
                ),
                Product(
                    id = "4",
                    title = "Top Up Genshin Impact 300 Genesis Crystal",
                    game = "Genshin Impact",
                    price = 75000.0,
                    description = "Top up Genesis Crystal Genshin Impact. Via UID. Proses instan.",
                    imageRes = 0,
                    type = ProductType.TOPUP
                ),
                Product(
                    id = "5",
                    title = "Akun Valorant Immortal",
                    game = "Valorant",
                    price = 600000.0,
                    description = "Akun Valorant rank Immortal. Skin bundle lengkap. Email bisa diganti.",
                    imageRes = 0,
                    type = ProductType.ACCOUNT
                )
            )
        }

        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onTimeout = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    products = products,
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    },
                    onLoginClick = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // ⭐ PERBAIKAN DI SINI - Product Detail
            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(
                    navArgument("productId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                // Ambil productId dengan safe call
                val productId = backStackEntry.arguments?.getString("productId")

                // Cek jika productId tidak null
                if (productId != null) {
                    val product = products.find { it.id == productId }

                    if (product != null) {
                        ProductDetailScreen(
                            product = product,
                            onBuyClick = {
                                navController.navigate(Screen.Checkout.createRoute(productId))
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    } else {
                        // Handle jika product tidak ditemukan
                        Text("Product tidak ditemukan")
                    }
                }
            }

            // ⭐ PERBAIKAN DI SINI - Checkout
            composable(
                route = Screen.Checkout.route,
                arguments = listOf(
                    navArgument("productId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                // Ambil productId dengan safe call
                val productId = backStackEntry.arguments?.getString("productId")

                // Cek jika productId tidak null
                if (productId != null) {
                    val product = products.find { it.id == productId }

                    if (product != null) {
                        CheckoutScreen(
                            product = product,
                            onOrderComplete = { orderData ->
                                navController.navigate(Screen.OrderSuccess.route) {
                                    popUpTo(Screen.Home.route)
                                }
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    } else {
                        // Handle jika product tidak ditemukan
                        Text("Product tidak ditemukan")
                    }
                }
            }

            composable(Screen.OrderSuccess.route) {
                OrderSuccessScreen(
                    onBackToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}