package com.xarays.marketplace

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.xarays.marketplace.R
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
        val authViewModel: AuthViewModel = viewModel()
        val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
        val userEmail by authViewModel.userEmail.collectAsState()

        // Ambil rute saat ini untuk menentukan apakah Navbar harus muncul
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        // Home dan Profil adalah tujuan utama yang berbagi satu bottom navigation.
        val bottomBarScreens = setOf(Screen.Home.route, Screen.Profile.route)
        val shouldShowBottomBar = currentDestination?.route in bottomBarScreens

        fun navigateToMainDestination(route: String) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        val products = remember {
            listOf(
                Product("1", "Akun Mobile Legends Mythic", "Mobile Legends", 500000.0, "Akun ML rank Mythic...", R.drawable.game_moba, ProductType.ACCOUNT),
                Product("2", "Top Up Free Fire 1000 Diamond", "Free Fire", 150000.0, "Top up diamond...", R.drawable.game_battle_royale, ProductType.TOPUP)
            )
        }

        Scaffold(
            bottomBar = {
                if (shouldShowBottomBar) {
                    NavigationBar {
                        // Item Home
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            selected = currentDestination?.hierarchy?.any { it.route == Screen.Home.route } == true,
                            onClick = { navigateToMainDestination(Screen.Home.route) }
                        )
                        // Item Profile
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile") },
                            selected = currentDestination?.hierarchy?.any { it.route == Screen.Profile.route } == true,
                            onClick = { navigateToMainDestination(Screen.Profile.route) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.padding(innerPadding) // Penting agar konten tidak tertutup Navbar
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(onTimeout = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    })
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        products = products,
                        onProductClick = { productId ->
                            navController.navigate(Screen.ProductDetail.createRoute(productId))
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        isLoggedIn = isAuthenticated,
                        userEmail = userEmail,
                        onLogin = { navController.navigate(Screen.Login.route) },
                        onLogout = {
                            authViewModel.logout()
                        }
                    )
                }

                composable(Screen.Login.route) {
                    LoginScreen(
                        viewModel = authViewModel,
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Register.route) {
                    RegisterScreen(
                        viewModel = authViewModel,
                        onRegisterSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        },
                        onBackToLogin = { navController.popBackStack() }
                    )
                }

                // ... Rute ProductDetail, Checkout, dan OrderSuccess tetap sama ...
                composable(
                    route = Screen.ProductDetail.route,
                    arguments = listOf(navArgument("productId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val productId = backStackEntry.arguments?.getString("productId")
                    val product = products.find { it.id == productId }
                    if (product != null) { 
                        ProductDetailScreen(
                            product = product,
                            onBuyClick = {
                                // Gunakan operator ?: "" untuk menangani kemungkinan null
                                val idToNavigate = productId ?: ""
                                if (isAuthenticated) {
                                    navController.navigate(Screen.Checkout.createRoute(idToNavigate))
                                } else {
                                    navController.navigate(Screen.Login.route)
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(
                    route = Screen.Checkout.route,
                    arguments = listOf(navArgument("productId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val productId = backStackEntry.arguments?.getString("productId")
                    val product = products.find { it.id == productId }
                    if (product != null) {
                        CheckoutScreen(
                            product = product,
                            onOrderComplete = {
                                navController.navigate(Screen.OrderSuccess.route) {
                                    popUpTo(Screen.Home.route)
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.OrderSuccess.route) {
                    OrderSuccessScreen(onBackToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    })
                }
            }
        }
    }
}
