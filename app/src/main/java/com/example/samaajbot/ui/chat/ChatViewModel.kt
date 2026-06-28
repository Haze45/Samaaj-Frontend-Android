package com.example.samaajbot.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.samaajbot.data.models.ChatMessageEntity
import com.example.samaajbot.data.repository.ChatRepository
import com.example.samaajbot.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private var communityId = -1

    private val _askState = MutableStateFlow<Resource<ChatMessageEntity>?>(null)
    val askState: StateFlow<Resource<ChatMessageEntity>?> = _askState

    private val _messages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val messages: StateFlow<List<ChatMessageEntity>> = _messages

    fun init(communityId: Int) {
        this.communityId = communityId
        viewModelScope.launch {
            repository.getLocalMessages(communityId).collect {
                _messages.value = it
            }
        }
        viewModelScope.launch { repository.syncHistory(communityId) }
    }

    fun askQuestion(question: String) {
        if (communityId == -1) return
        viewModelScope.launch {
            _askState.value = Resource.Loading()
            _askState.value = repository.askQuestion(communityId, question)
        }
    }

    fun clearHistory() {
        viewModelScope.launch { repository.clearHistory(communityId) }
    }

    fun resetAskState() { _askState.value = null }
}
