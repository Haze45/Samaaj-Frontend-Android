package com.example.samaajbot.data.api

import com.example.samaajbot.data.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface SamaajBotApi {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<UserResponse>

    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<UserResponse>

    @POST("communities")
    suspend fun createCommunity(@Body request: CommunityCreateRequest): Response<CommunityResponse>

    @POST("communities/join")
    suspend fun joinCommunity(@Body request: JoinCommunityRequest): Response<CommunityResponse>

    @GET("communities")
    suspend fun getMyCommunities(): Response<List<CommunityResponse>>

    @DELETE("communities/{id}/leave")
    suspend fun leaveCommunity(@Path("id") id: Int): Response<MessageResponse>

    @Multipart
    @POST("documents/{communityId}/upload")
    suspend fun uploadDocument(
        @Path("communityId") communityId: Int,
        @Part file: MultipartBody.Part
    ): Response<DocumentResponse>

    @GET("documents/{communityId}")
    suspend fun getDocuments(@Path("communityId") communityId: Int): Response<List<DocumentResponse>>

    @DELETE("documents/{communityId}/{documentId}")
    suspend fun deleteDocument(
        @Path("communityId") communityId: Int,
        @Path("documentId") documentId: Int
    ): Response<MessageResponse>

    @POST("chat/ask")
    suspend fun askQuestion(@Body request: ChatRequest): Response<ChatMessageResponse>

    @GET("chat/history/{communityId}")
    suspend fun getChatHistory(
        @Path("communityId") communityId: Int,
        @Query("limit") limit: Int = 50
    ): Response<List<ChatMessageResponse>>

    @DELETE("chat/history/{communityId}")
    suspend fun clearHistory(@Path("communityId") communityId: Int): Response<MessageResponse>
}
