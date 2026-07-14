package com.xarays.marketplace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xarays.marketplace.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repo = AuthRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    init {
        // Cek apakah user sudah login sebelumnya
        _isAuthenticated.value = repo.currentUser != null
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repo.login(email, password)

            if (result.isSuccess) {
                _isAuthenticated.value = true
            } else {
                // PERBAIKAN DI SINI: Gunakan exceptionOrNull()
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Login gagal"
            }
            _isLoading.value = false
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repo.register(email, password)

            if (result.isSuccess) {
                _isAuthenticated.value = true
            } else {
                // PERBAIKAN DI SINI: Gunakan exceptionOrNull()
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Registrasi gagal"
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}