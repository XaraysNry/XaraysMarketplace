package com.xarays.marketplace.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xarays.marketplace.model.OrderSummary

private enum class ProfileSortOption(val label: String) {
    NEWEST("Terbaru"),
    OLDEST("Terlama")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isLoggedIn: Boolean,
    userEmail: String?,
    orders: List<OrderSummary>,
    onLogin: () -> Unit,
    onLogout: () -> Unit
) {
    var sortOption by remember { mutableStateOf(ProfileSortOption.NEWEST) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profil",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (!isLoggedIn) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Masuk untuk melihat profil Anda", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Silakan masuk terlebih dahulu untuk mengakses riwayat pesanan dan pengaturan akun.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
                    Text("Masuk")
                }
            }
        } else {
            val sortedOrders = when (sortOption) {
                ProfileSortOption.NEWEST -> orders.sortedByDescending { it.orderDate }
                ProfileSortOption.OLDEST -> orders.sortedBy { it.orderDate }
            }
            val ongoingOrders = sortedOrders.filterNot { it.status.isFinishedStatus() }
            val finishedOrders = sortedOrders.filter { it.status.isFinishedStatus() }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "👤", fontSize = 56.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = userEmail?.substringBefore("@") ?: "Pengguna",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = userEmail.orEmpty(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item { Divider() }

                item {
                    SortSelector(
                        current = sortOption,
                        onSelected = { sortOption = it }
                    )
                }

                item {
                    Text(
                        "Pesanan Berlangsung",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (ongoingOrders.isEmpty()) {
                    item {
                        Text(
                            text = "Belum ada pesanan yang sedang berlangsung.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(ongoingOrders, key = { it.id }) { order ->
                        OrderCard(order = order)
                    }
                }

                item {
                    Text(
                        "Pesanan Selesai",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (finishedOrders.isEmpty()) {
                    item {
                        Text(
                            text = "Belum ada pesanan yang selesai.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(finishedOrders, key = { it.id }) { order ->
                        OrderCard(order = order)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Logout")
                    }
                }
            }
        }
    }
}

private fun String.isFinishedStatus(): Boolean {
    val normalized = uppercase()
    return normalized.contains("SELESAI") || normalized.contains("COMPLETED") || normalized.contains("DONE")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortSelector(
    current: ProfileSortOption,
    onSelected: (ProfileSortOption) -> Unit
) {
    val options = ProfileSortOption.entries
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = current.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Urutkan") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun OrderCard(order: OrderSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(order.productTitle, fontWeight = FontWeight.Bold)
            Text(order.productGame, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Status: ${order.status}")
            Text("Total: Rp ${String.format("%,.0f", order.totalAmount)}")
            if (order.proofUrl.isNotBlank()) {
                Text("Bukti: ${order.proofUrl}", color = MaterialTheme.colorScheme.primary)
            }
            if (order.adminReply.isNotBlank()) {
                Text("Balasan admin: ${order.adminReply}")
            }
        }
    }
}
