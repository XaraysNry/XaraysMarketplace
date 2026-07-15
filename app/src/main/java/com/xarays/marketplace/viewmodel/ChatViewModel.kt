package com.xarays.marketplace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xarays.marketplace.data.ChatMessage
import com.xarays.marketplace.data.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadMessages() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _messages.value = repository.getMessages()
            _isLoading.value = false
        }
    }

    fun sendMessage(message: ChatMessage, onSent: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.sendMessage(message)
            result.onSuccess {
                loadMessages()
                onSent()
            }.onFailure {
                _errorMessage.value = it.message ?: "Gagal mengirim pesan."
            }
        }
    }
}
