package com.example.samaajbot.data.models

import com.google.gson.annotations.SerializedName

data class RegisterRequest(val name: String, val email: String, val password: String)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type")   val tokenType: String,
    @SerializedName("user_id")      val userId: Int,
    val name: String,
    val email: String
)

data class UserResponse(
    val id: Int, val name: String, val email: String,
    @SerializedName("created_at") val createdAt: String
)

data class CommunityCreateRequest(val name: String, val description: String? = null)

data class JoinCommunityRequest(@SerializedName("join_code") val joinCode: String)

data class CommunityResponse(
    val id: Int, val name: String, val description: String?,
    @SerializedName("join_code")  val joinCode: String,
    @SerializedName("admin_id")   val adminId: Int,
    @SerializedName("created_at") val createdAt: String
)

data class DocumentResponse(
    val id: Int,
    @SerializedName("community_id")  val communityId: Int,
    val filename: String,
    @SerializedName("original_name") val originalName: String,
    @SerializedName("file_size")     val fileSize: Long,
    @SerializedName("is_processed")  val isProcessed: Boolean,
    @SerializedName("uploaded_at")   val uploadedAt: String
)

data class ChatRequest(
    @SerializedName("community_id") val communityId: Int,
    val question: String
)

data class ChatMessageResponse(
    val id: Int,
    @SerializedName("user_id")      val userId: Int,
    @SerializedName("community_id") val communityId: Int,
    val role: String,
    val content: String,
    @SerializedName("source_doc")   val sourceDoc: String?,
    @SerializedName("created_at")   val createdAt: String
)

data class MessageResponse(val message: String)
