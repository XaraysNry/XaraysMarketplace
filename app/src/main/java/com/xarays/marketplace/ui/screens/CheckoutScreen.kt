package com.xarays.marketplace.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xarays.marketplace.model.OrderData
import com.xarays.marketplace.model.Product
import com.xarays.marketplace.ui.components.CustomButton
import com.xarays.marketplace.ui.components.CustomTextField

private val paymentMethods = listOf("Transfer Bank BCA", "DANA", "OVO")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    product: Product,
    initialEmail: String,
    initialBuyerName: String,
    userId: String,
    isSubmitting: Boolean,
    submitError: String?,
    onSubmit: (OrderData) -> Unit,
    onBack: () -> Unit
) {
    var buyerName by remember(initialBuyerName) { mutableStateOf(initialBuyerName) }
    var buyerEmail by remember(initialEmail) { mutableStateOf(initialEmail) }
    var additionalInfo by remember { mutableStateOf("") }
    var gameId by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf(paymentMethods.first()) }
    var paymentExpanded by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    val isFreeFireTopUp = product.game.equals("Free Fire", ignoreCase = true) || product.title.contains("free fire", ignoreCase = true)

    fun validate(): String? = when {
        buyerName.trim().length < 2 -> "Nama profil belum tersedia."
        !android.util.Patterns.EMAIL_ADDRESS.matcher(buyerEmail.trim()).matches() -> "Masukkan alamat email yang valid."
        isFreeFireTopUp && gameId.trim().isEmpty() -> "Masukkan ID Game Free Fire."
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isSubmitting) {
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CheckoutCard(title = "Ringkasan Pesanan") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(product.title, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "Rp ${String.format("%,.0f", product.price)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = DividerDefaults.Thickness,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Rp ${String.format("%,.0f", product.price)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            CheckoutCard(title = "Data Pembeli") {
                CustomTextField(buyerName, { buyerName = it }, "Nama Lengkap", "Nama diisi dari profil")
                Spacer(Modifier.height(12.dp))
                CustomTextField(buyerEmail, { buyerEmail = it }, "Email", "contoh@email.com")
                if (isFreeFireTopUp) {
                    Spacer(Modifier.height(12.dp))
                    CustomTextField(gameId, { gameId = it }, "ID Game Free Fire", "Masukkan ID game kamu")
                }
                Spacer(Modifier.height(12.dp))
                CustomTextField(additionalInfo, { additionalInfo = it }, "Informasi Tambahan (Opsional)", "Catatan khusus untuk penjual")
            }

            CheckoutCard(title = "Metode Pembayaran") {
                ExposedDropdownMenuBox(
                    expanded = paymentExpanded,
                    onExpandedChange = { paymentExpanded = !paymentExpanded }
                ) {
                    androidx.compose.material3.OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih metode pembayaran") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(paymentExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = paymentExpanded, onDismissRequest = { paymentExpanded = false }) {
                        paymentMethods.forEach { method ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(method) },
                                onClick = { paymentMethod = method; paymentExpanded = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = when (paymentMethod) {
                        "Transfer Bank BCA" -> "Transfer ke BCA 1234567890 a/n Admin."
                        else -> "Kirim pembayaran ke 081234567890 (${paymentMethod})."
                    },
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Pesanan akan diproses setelah pembayaran dikonfirmasi.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            (validationError ?: submitError)?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(4.dp))
            CustomButton(
                text = if (isSubmitting) "Menyimpan pesanan..." else "Konfirmasi Pesanan",
                enabled = !isSubmitting,
                onClick = {
                    validationError = validate()
                    if (validationError == null) {
                        onSubmit(
                            OrderData(
                                productId = product.id,
                                productTitle = product.title,
                                productGame = product.game,
                                totalAmount = product.price,
                                buyerName = buyerName.trim(),
                                buyerEmail = buyerEmail.trim(),
                                buyerPhone = "",
                                additionalInfo = additionalInfo.trim(),
                                gameId = if (isFreeFireTopUp) gameId.trim() else "",
                                paymentMethod = paymentMethod,
                                userId = userId
                            )
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun CheckoutCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
