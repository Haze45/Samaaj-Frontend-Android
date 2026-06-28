package com.example.samaajbot.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.samaajbot.data.models.CommunityResponse
import com.example.samaajbot.data.repository.AuthRepository
import com.example.samaajbot.data.repository.CommunityRepository
import com.example.samaajbot.utils.Resource
import com.example.samaajbot.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val communityRepo: CommunityRepository,
    private val authRepo: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _communities = MutableStateFlow<Resource<List<CommunityResponse>>>(Resource.Loading())
    val communities: StateFlow<Resource<List<CommunityResponse>>> = _communities

    private val _actionState = MutableStateFlow<Resource<String>?>(null)
    val actionState: StateFlow<Resource<String>?> = _actionState

    var currentUserId: Int = -1
        private set

    init { loadCommunities() }

    fun loadCommunities() {
        viewModelScope.launch {
            currentUserId = sessionManager.userId.first() ?: -1
            _communities.value = Resource.Loading()
            _communities.value = communityRepo.getMyCommunities()
        }
    }

    fun createCommunity(name: String, description: String?) {
        if (name.isBlank()) { _actionState.value = Resource.Error("Name is required"); return }
        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            when (val result = communityRepo.createCommunity(name, description)) {
                is Resource.Success -> {
                    _actionState.value = Resource.Success("Created! Share code: ${result.data.joinCode}")
                    loadCommunities()
                }
                is Resource.Error   -> _actionState.value = Resource.Error(result.message)
                is Resource.Loading -> {}
            }
        }
    }

    fun joinCommunity(code: String) {
        if (code.isBlank()) { _actionState.value = Resource.Error("Code is required"); return }
        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            when (val result = communityRepo.joinCommunity(code.trim().uppercase())) {
                is Resource.Success -> {
                    _actionState.value = Resource.Success("Joined ${result.data.name}!")
                    loadCommunities()
                }
                is Resource.Error   -> _actionState.value = Resource.Error(result.message)
                is Resource.Loading -> {}
            }
        }
    }

    fun logout() { viewModelScope.launch { authRepo.logout() } }

    fun resetActionState() { _actionState.value = null }
}
