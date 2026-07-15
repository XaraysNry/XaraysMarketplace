package com.xarays.marketplace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xarays.marketplace.data.OrderRepository
import com.xarays.marketplace.model.OrderData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() {
    private val repository = OrderRepository()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun submitOrder(order: OrderData, onSuccess: (String) -> Unit) {
        if (_isSubmitting.value) return

        viewModelScope.launch {
            _isSubmitting.value = true
            _errorMessage.value = null
            val result = repository.createOrder(order)
            _isSubmitting.value = false

            result.onSuccess(onSuccess).onFailure {
                _errorMessage.value = it.message ?: "Pesanan gagal disimpan. Silakan coba lagi."
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
