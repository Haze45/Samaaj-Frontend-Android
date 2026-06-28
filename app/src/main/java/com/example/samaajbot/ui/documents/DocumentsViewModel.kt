package com.example.samaajbot.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.samaajbot.data.models.DocumentResponse
import com.example.samaajbot.data.repository.DocumentRepository
import com.example.samaajbot.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DocumentsViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    private val _documents = MutableStateFlow<Resource<List<DocumentResponse>>>(Resource.Loading())
    val documents: StateFlow<Resource<List<DocumentResponse>>> = _documents

    private val _uploadState = MutableStateFlow<Resource<DocumentResponse>?>(null)
    val uploadState: StateFlow<Resource<DocumentResponse>?> = _uploadState

    fun loadDocuments(communityId: Int) {
        viewModelScope.launch {
            _documents.value = Resource.Loading()
            _documents.value = repository.getDocuments(communityId)
        }
    }

    fun uploadDocument(communityId: Int, file: File) {
        viewModelScope.launch {
            _uploadState.value = Resource.Loading()
            _uploadState.value = repository.uploadDocument(communityId, file)
            loadDocuments(communityId)
        }
    }

    fun deleteDocument(communityId: Int, documentId: Int) {
        viewModelScope.launch {
            repository.deleteDocument(communityId, documentId)
            loadDocuments(communityId)
        }
    }

    fun resetUploadState() { _uploadState.value = null }
}
