package com.xarays.marketplace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
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

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        _isAuthenticated.value = auth.currentUser != null
        _userEmail.value = auth.currentUser?.email
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
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

    fun logout() {
        repo.logout()
    }

    override fun onCleared() {
        firebaseAuth.removeAuthStateListener(authStateListener)
        super.onCleared()
    }
}
