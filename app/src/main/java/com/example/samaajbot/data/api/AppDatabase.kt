package com.example.samaajbot.data.api

import androidx.room.*
import com.example.samaajbot.data.models.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE communityId = :communityId ORDER BY id ASC")
    fun getMessages(communityId: Int): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE communityId = :communityId")
    suspend fun clearMessages(communityId: Int)
}

@Database(entities = [ChatMessageEntity::class], version = 1, exportSchema = false)
abstract class SamaajBotDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}
