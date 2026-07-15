package com.xarays.marketplace

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.xarays.marketplace.ui.navigation.Screen
import com.xarays.marketplace.ui.screens.*
import com.xarays.marketplace.ui.theme.GameMarketplaceTheme
import com.xarays.marketplace.viewmodel.AuthViewModel
import com.xarays.marketplace.viewmodel.ChatViewModel
import com.xarays.marketplace.viewmodel.ProductViewModel
import com.xarays.marketplace.viewmodel.OrderViewModel
import com.xarays.marketplace.viewmodel.OrdersViewModel

@Composable
fun XaraysMarketplaceApp() {
    GameMarketplaceTheme {
        val navController = rememberNavController()
        val authViewModel: AuthViewModel = viewModel()
        val productViewModel: ProductViewModel = viewModel()
        val orderViewModel: OrderViewModel = viewModel()
        val ordersViewModel: OrdersViewModel = viewModel()
        val chatViewModel: ChatViewModel = viewModel()
        val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
        val userEmail by authViewModel.userEmail.collectAsState()
        val products by productViewModel.products.collectAsState()
        val isLoading by productViewModel.isLoading.collectAsState()
        val errorMessage by productViewModel.errorMessage.collectAsState()
        val isOrderSubmitting by orderViewModel.isSubmitting.collectAsState()
        val orderErrorMessage by orderViewModel.errorMessage.collectAsState()
        val orders by ordersViewModel.orders.collectAsState()
        val ordersLoading by ordersViewModel.isLoading.collectAsState()
        val ordersError by ordersViewModel.errorMessage.collectAsState()
        val messages by chatViewModel.messages.collectAsState()
        val chatLoading by chatViewModel.isLoading.collectAsState()
        val chatError by chatViewModel.errorMessage.collectAsState()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        val currentBuyerName = FirebaseAuth.getInstance().currentUser?.displayName
            ?.takeIf { it.isNotBlank() }
            ?: userEmail?.substringBefore("@")?.ifBlank { "Customer" }
            ?: "Customer"
        val currentUserName = userEmail?.substringBefore("@")?.ifBlank { "Customer" } ?: "Customer"

        LaunchedEffect(currentUserId) {
            if (currentUserId.isNotBlank()) {
                ordersViewModel.loadOrders(currentUserId)
            }
        }

        LaunchedEffect(Unit) {
            chatViewModel.loadMessages()
        }

        // Ambil rute saat ini untuk menentukan apakah Navbar harus muncul
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        // Home, Pesanan, Chat, dan Profil berbagi satu bottom navigation.
        val bottomBarScreens = setOf(Screen.Home.route, Screen.Orders.route, Screen.Chat.route, Screen.Profile.route)
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
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Pesanan") },
                            label = { Text("Pesanan") },
                            selected = currentDestination?.hierarchy?.any { it.route == Screen.Orders.route } == true,
                            onClick = { navigateToMainDestination(Screen.Orders.route) }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Chat, contentDescription = "Chat") },
                            label = { Text("Chat") },
                            selected = currentDestination?.hierarchy?.any { it.route == Screen.Chat.route } == true,
                            onClick = { navigateToMainDestination(Screen.Chat.route) }
                        )
                        // Item Profile
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Person, contentDescription = "Profil") },
                            label = { Text("Profil") },
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
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onProductClick = { productId ->
                            navController.navigate(Screen.ProductDetail.createRoute(productId))
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        isLoggedIn = isAuthenticated,
                        userEmail = userEmail,
                        orders = orders,
                        onLogin = { navController.navigate(Screen.Login.route) },
                        onLogout = {
                            authViewModel.logout()
                        }
                    )
                }

                composable(Screen.Orders.route) {
                    OrdersScreen(
                        orders = orders,
                        isLoading = ordersLoading,
                        errorMessage = ordersError,
                        onRefresh = { ordersViewModel.loadOrders(currentUserId) }
                    )
                }

                composable(Screen.Chat.route) {
                    ChatScreen(
                        messages = messages,
                        isLoading = chatLoading,
                        errorMessage = chatError,
                        userId = currentUserId,
                        userName = currentUserName,
                        onSendMessage = { message ->
                            chatViewModel.sendMessage(message)
                        },
                        onRefresh = { chatViewModel.loadMessages() }
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
                            initialEmail = userEmail.orEmpty(),
                            initialBuyerName = currentBuyerName,
                            userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty(),
                            isSubmitting = isOrderSubmitting,
                            submitError = orderErrorMessage,
                            onSubmit = { order ->
                                orderViewModel.submitOrder(order) {
                                    ordersViewModel.loadOrders(order.userId)
                                    navController.navigate(Screen.OrderSuccess.route) {
                                    popUpTo(Screen.Home.route)
                                    }
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
