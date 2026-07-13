package com.xarays.xaraysmarketplace.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xarays.xaraysmarketplace.model.OrderData
import com.xarays.xaraysmarketplace.model.Product
import com.xarays.xaraysmarketplace.ui.components.CustomButton
import com.xarays.xaraysmarketplace.ui.components.CustomTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    product: Product,
    onOrderComplete: (OrderData) -> Unit,
    onBack: () -> Unit
) {
    var buyerName by remember { mutableStateOf("") }
    var buyerEmail by remember { mutableStateOf("") }
    var buyerPhone by remember { mutableStateOf("") }
    var additionalInfo by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 24.sp)
                    }
                },
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
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Order Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Ringkasan Pesanan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = product.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Rp ${String.format("%,.0f", product.price)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = DividerDefaults.Thickness,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Rp ${String.format("%,.0f", product.price)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Buyer Information Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Data Pembeli",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    CustomTextField(
                        value = buyerName,
                        onValueChange = { buyerName = it },
                        label = "Nama Lengkap",
                        placeholder = "Masukkan nama lengkap"
                    )

                    CustomTextField(
                        value = buyerEmail,
                        onValueChange = { buyerEmail = it },
                        label = "Email",
                        placeholder = "contoh@email.com"
                    )

                    CustomTextField(
                        value = buyerPhone,
                        onValueChange = { buyerPhone = it },
                        label = "Nomor Telepon",
                        placeholder = "08xxxxxxxxxx"
                    )

                    CustomTextField(
                        value = additionalInfo,
                        onValueChange = { additionalInfo = it },
                        label = "Informasi Tambahan (Opsional)",
                        placeholder = "Catatan khusus untuk penjual"
                    )
                }
            }

            // Payment Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Informasi Pembayaran",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Transfer Bank: BCA 1234567890 a/n Admin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "• E-Wallet: DANA/OVO 081234567890",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "• Konfirmasi pembayaran via WhatsApp",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CustomButton(
                text = "Konfirmasi Pesanan",
                onClick = {
                    val orderData = OrderData(
                        productId = product.id,
                        buyerName = buyerName,
                        buyerEmail = buyerEmail,
                        buyerPhone = buyerPhone,
                        additionalInfo = additionalInfo
                    )
                    onOrderComplete(orderData)
                },
                enabled = buyerName.isNotEmpty() && buyerEmail.isNotEmpty() && buyerPhone.isNotEmpty()
            )
        }
    }
}