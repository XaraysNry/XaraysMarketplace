package com.xarays.marketplace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xarays.marketplace.data.OrderRepository
import com.xarays.marketplace.model.OrderSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrdersViewModel : ViewModel() {
    private val repository = OrderRepository()

    private val _orders = MutableStateFlow<List<OrderSummary>>(emptyList())
    val orders: StateFlow<List<OrderSummary>> = _orders

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadOrders(userId: String) {
        if (userId.isBlank()) {
            _orders.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val snapshot = repository.getOrdersByUser(userId)
                _orders.value = snapshot.mapIndexed { index, data ->
                    OrderSummary(
                        id = data["id"] as? String ?: index.toString(),
                        productTitle = data["productTitle"] as? String ?: "-",
                        productGame = data["productGame"] as? String ?: "-",
                        totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0,
                        buyerName = data["buyerName"] as? String ?: "-",
                        buyerEmail = data["buyerEmail"] as? String ?: "-",
                        buyerPhone = data["buyerPhone"] as? String ?: "-",
                        gameId = data["gameId"] as? String ?: "",
                        status = data["status"] as? String ?: "MENUNGGU_PEMBAYARAN",
                        paymentMethod = data["paymentMethod"] as? String ?: "-",
                        orderDate = (data["orderDate"] as? Number)?.toLong() ?: 0L,
                        adminNote = data["adminNote"] as? String ?: "",
                        proofUrl = data["proofUrl"] as? String ?: "",
                        adminReply = data["adminReply"] as? String ?: ""
                    )
                }.sortedByDescending { it.orderDate }
            } catch (exception: Exception) {
                _orders.value = emptyList()
                _errorMessage.value = exception.message ?: "Gagal memuat riwayat pesanan."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
