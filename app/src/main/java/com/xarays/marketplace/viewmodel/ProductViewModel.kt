package com.xarays.marketplace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xarays.marketplace.data.ProductRepository
import com.xarays.marketplace.model.Product
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private val repo = ProductRepository()
    private var productsListener: ListenerRegistration? = null

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        observeProducts()
    }

    fun observeProducts() {
        _isLoading.value = true
        _errorMessage.value = null

        productsListener?.remove()
        productsListener = repo.observeAllProducts(
            onChange = { result ->
                _products.value = result
                _errorMessage.value = if (result.isEmpty()) {
                    "Belum ada produk ditemukan di database."
                } else {
                    null
                }
                _isLoading.value = false
            },
            onError = { error ->
                _errorMessage.value = "Gagal memuat data: ${error.message}"
                _isLoading.value = false
            }
        )
    }

    fun getProductById(productId: String, onSuccess: (Product) -> Unit, onError: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val product = repo.getProductById(productId)
            _isLoading.value = false

            if (product != null) {
                onSuccess(product)
            } else {
                _errorMessage.value = "Produk tidak ditemukan"
                onError()
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        productsListener?.remove()
        productsListener = null
        super.onCleared()
    }
}
