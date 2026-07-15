package com.xarays.marketplace.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.xarays.marketplace.model.OrderSummary

private enum class OrderSortOption(val label: String) {
    NEWEST("Terbaru"),
    OLDEST("Terlama")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    orders: List<OrderSummary>,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit
) {
    var sortOption by remember { mutableStateOf(OrderSortOption.NEWEST) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pesanan") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Lihat status pesanan dan bukti yang dikirim admin.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SortSelector(
                current = sortOption,
                onSelected = { sortOption = it }
            )

            if (isLoading) {
                CircularProgressIndicator()
            }

            if (!errorMessage.isNullOrBlank()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }

            val visibleOrders = when (sortOption) {
                OrderSortOption.NEWEST -> orders.sortedByDescending { it.orderDate }
                OrderSortOption.OLDEST -> orders.sortedBy { it.orderDate }
            }

            if (!isLoading && visibleOrders.isEmpty()) {
                Text("Belum ada pesanan.", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(visibleOrders, key = { it.id }) { order ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(order.productTitle, fontWeight = FontWeight.Bold)
                                Text(order.productGame, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Status: ${order.status}")
                                Text("Pembayaran: ${order.paymentMethod}")
                                Text("Total: Rp ${String.format("%,.0f", order.totalAmount)}")
                                if (order.proofUrl.isNotBlank()) {
                                    Text("Bukti admin: ${order.proofUrl}", color = MaterialTheme.colorScheme.primary)
                                }
                                if (order.adminReply.isNotBlank()) {
                                    Text("Balasan admin: ${order.adminReply}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortSelector(
    current: OrderSortOption,
    onSelected: (OrderSortOption) -> Unit
) {
    val options = OrderSortOption.entries
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
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
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
