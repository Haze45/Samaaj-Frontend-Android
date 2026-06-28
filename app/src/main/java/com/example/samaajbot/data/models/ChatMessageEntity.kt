package com.example.samaajbot.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: Int,
    val communityId: Int,
    val userId: Int,
    val role: String,
    val content: String,
    val sourceDoc: String?,
    val createdAt: String
)
