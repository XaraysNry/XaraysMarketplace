package com.xarays.marketplace

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.xarays.marketplace.model.Product
import com.xarays.marketplace.model.ProductType
import com.xarays.marketplace.ui.navigation.Screen
import com.xarays.marketplace.ui.screens.*
import com.xarays.marketplace.ui.theme.GameMarketplaceTheme
import com.xarays.marketplace.viewmodel.AuthViewModel

@Composable
fun XaraysMarketplaceApp() {
    GameMarketplaceTheme {
        val navController = rememberNavController()

        // Inisialisasi AuthViewModel di level atas agar bisa dipakai di Login & Register
        val authViewModel: AuthViewModel = viewModel()

        // Ambil state autentikasi untuk proteksi halaman (opsional)
        val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

        // Sample products data (Nanti bisa diganti dengan data dari Firestore)
        val products = remember {
            listOf(
                Product(
                    id = "1",
                    title = "Akun Mobile Legends Mythic",
                    game = "Mobile Legends",
                    price = 500000.0,
                    description = "Akun ML rank Mythic, skin limited, hero lengkap.",
                    imageRes = 0,
                    type = ProductType.ACCOUNT
                ),
                Product(
                    id = "2",
                    title = "Top Up Free Fire 1000 Diamond",
                    game = "Free Fire",
                    price = 150000.0,
                    description = "Top up diamond Free Fire 1000 diamond. Proses cepat.",
                    imageRes = 0,
                    type = ProductType.TOPUP
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
                        // Jika sudah login, langsung ke Home. Jika belum, bisa arahkan ke Login
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

            // ⭐ PERBAIKAN LOGIN SCREEN
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel, // Pass ViewModel
                    onLoginSuccess = {
                        navController.popBackStack() // Kembali ke Home setelah login
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route) // Pindah ke Register
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // ⭐ TAMBAHAN REGISTER SCREEN
            composable(Screen.Register.route) {
                RegisterScreen(
                    viewModel = authViewModel, // Pass ViewModel yang sama
                    onRegisterSuccess = {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    },
                    onBackToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")
                if (productId != null) {
                    val product = products.find { it.id == productId }
                    if (product != null) {
                        ProductDetailScreen(
                            product = product,
                            onBuyClick = {
                                // Opsional: Cek apakah user sudah login sebelum beli
                                if (isAuthenticated) {
                                    navController.navigate(Screen.Checkout.createRoute(productId))
                                } else {
                                    navController.navigate(Screen.Login.route)
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    } else {
                        Text("Product tidak ditemukan")
                    }
                }
            }

            composable(
                route = Screen.Checkout.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")
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
                            onBack = { navController.popBackStack() }
                        )
                    } else {
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