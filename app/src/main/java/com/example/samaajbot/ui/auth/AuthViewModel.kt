package com.example.samaajbot.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.samaajbot.data.repository.AuthRepository
import com.example.samaajbot.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<String>?>(null)
    val authState: StateFlow<Resource<String>?> = _authState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = Resource.Error("Email and password are required")
            return
        }
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            _authState.value = repository.login(email, password)
        }
    }

    fun register(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = Resource.Error("All fields are required")
            return
        }
        if (password.length < 6) {
            _authState.value = Resource.Error("Password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            _authState.value = repository.register(name, email, password)
        }
    }

    fun resetState() { _authState.value = null }
}
