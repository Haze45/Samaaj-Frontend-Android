package com.example.samaajbot.data.repository

import com.example.samaajbot.data.api.ChatDao
import com.example.samaajbot.data.api.SamaajBotApi
import com.example.samaajbot.data.models.*
import com.example.samaajbot.utils.Resource
import com.example.samaajbot.utils.SessionManager
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: SamaajBotApi,
    private val sessionManager: SessionManager
) {
    suspend fun register(name: String, email: String, password: String): Resource<String> {
        return try {
            val r = api.register(RegisterRequest(name, email, password))
            if (r.isSuccessful) Resource.Success("Registered successfully")
            else Resource.Error(r.errorBody()?.string() ?: "Registration failed")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    suspend fun login(email: String, password: String): Resource<String> {
        return try {
            val r = api.login(email, password)
            if (r.isSuccessful && r.body() != null) {
                val b = r.body()!!
                sessionManager.saveSession(b.accessToken, b.userId, b.name, b.email)
                Resource.Success("Login successful")
            } else Resource.Error("Invalid email or password")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    suspend fun logout() = sessionManager.clearSession()
}

class CommunityRepository @Inject constructor(private val api: SamaajBotApi) {
    suspend fun createCommunity(name: String, desc: String?): Resource<CommunityResponse> {
        return try {
            val r = api.createCommunity(CommunityCreateRequest(name, desc))
            if (r.isSuccessful && r.body() != null) Resource.Success(r.body()!!)
            else Resource.Error(r.errorBody()?.string() ?: "Failed to create")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    suspend fun joinCommunity(code: String): Resource<CommunityResponse> {
        return try {
            val r = api.joinCommunity(JoinCommunityRequest(code))
            if (r.isSuccessful && r.body() != null) Resource.Success(r.body()!!)
            else Resource.Error("Invalid join code")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    suspend fun getMyCommunities(): Resource<List<CommunityResponse>> {
        return try {
            val r = api.getMyCommunities()
            if (r.isSuccessful && r.body() != null) Resource.Success(r.body()!!)
            else Resource.Error("Failed to load communities")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    suspend fun leaveCommunity(id: Int): Resource<String> {
        return try {
            val r = api.leaveCommunity(id)
            if (r.isSuccessful) Resource.Success("Left community")
            else Resource.Error("Failed to leave")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }
}

class DocumentRepository @Inject constructor(private val api: SamaajBotApi) {
    suspend fun uploadDocument(communityId: Int, file: File): Resource<DocumentResponse> {
        return try {
            val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val r = api.uploadDocument(communityId, part)
            if (r.isSuccessful && r.body() != null) Resource.Success(r.body()!!)
            else Resource.Error(r.errorBody()?.string() ?: "Upload failed")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    suspend fun getDocuments(communityId: Int): Resource<List<DocumentResponse>> {
        return try {
            val r = api.getDocuments(communityId)
            if (r.isSuccessful && r.body() != null) Resource.Success(r.body()!!)
            else Resource.Error("Failed to load documents")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    suspend fun deleteDocument(communityId: Int, documentId: Int): Resource<String> {
        return try {
            val r = api.deleteDocument(communityId, documentId)
            if (r.isSuccessful) Resource.Success("Deleted")
            else Resource.Error("Failed to delete")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }
}

class ChatRepository @Inject constructor(
    private val api: SamaajBotApi,
    private val chatDao: ChatDao
) {
    fun getLocalMessages(communityId: Int): Flow<List<ChatMessageEntity>> =
        chatDao.getMessages(communityId)

    suspend fun askQuestion(communityId: Int, question: String): Resource<ChatMessageEntity> {
        return try {
            val tempUserMsg = ChatMessageEntity(
                id = System.currentTimeMillis().toInt(),
                communityId = communityId,
                userId = 0,
                role = "user",
                content = question,
                sourceDoc = null,
                createdAt = java.time.LocalDateTime.now().toString()
            )
            chatDao.insertMessage(tempUserMsg)

            val r = api.askQuestion(ChatRequest(communityId, question))
            if (r.isSuccessful && r.body() != null) {
                val botMsg = r.body()!!.toEntity()
                chatDao.insertMessage(botMsg)
                Resource.Success(botMsg)
            } else Resource.Error(r.errorBody()?.string() ?: "Failed to get answer")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    suspend fun syncHistory(communityId: Int) {
        try {
            val r = api.getChatHistory(communityId)
            if (r.isSuccessful && r.body() != null)
                chatDao.clearMessages(communityId)
                chatDao.insertMessages(r.body()!!.map { it.toEntity() })
        } catch (_: Exception) {}
    }

    suspend fun clearHistory(communityId: Int): Resource<String> {
        return try {
            val r = api.clearHistory(communityId)
            if (r.isSuccessful) {
                chatDao.clearMessages(communityId)
                Resource.Success("Cleared")
            } else Resource.Error("Failed to clear")
        } catch (e: Exception) { Resource.Error(e.message ?: "Network error") }
    }

    private fun ChatMessageResponse.toEntity() = ChatMessageEntity(
        id, communityId, userId, role, content, sourceDoc, createdAt
    )
}
