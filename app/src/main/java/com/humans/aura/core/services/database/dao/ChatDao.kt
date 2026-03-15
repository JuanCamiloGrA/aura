package com.humans.aura.core.services.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.humans.aura.core.services.database.entity.chat.ChatMessageEntity
import com.humans.aura.core.services.database.entity.chat.ChatSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY updated_at_epoch_millis DESC")
    fun observeSessions(): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_messages WHERE session_id = :sessionId ORDER BY created_at_epoch_millis ASC")
    fun observeMessages(sessionId: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE session_id = :sessionId ORDER BY created_at_epoch_millis DESC LIMIT :limit")
    suspend fun getRecentMessages(
        sessionId: Long,
        limit: Int,
    ): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_sessions ORDER BY updated_at_epoch_millis DESC LIMIT 1")
    suspend fun getLatestSession(): ChatSessionEntity?

    @Query("SELECT * FROM chat_sessions ORDER BY created_at_epoch_millis ASC")
    suspend fun getAllSessions(): List<ChatSessionEntity>

    @Query("SELECT * FROM chat_messages ORDER BY created_at_epoch_millis ASC, id ASC")
    suspend fun getAllMessages(): List<ChatMessageEntity>

    @Insert
    suspend fun insertSession(session: ChatSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<ChatSessionEntity>)

    @Insert
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("UPDATE chat_sessions SET updated_at_epoch_millis = :updatedAtEpochMillis WHERE id = :sessionId")
    suspend fun updateSessionTimestamp(
        sessionId: Long,
        updatedAtEpochMillis: Long,
    )

    @Query("DELETE FROM chat_sessions")
    suspend fun deleteAllSessions()
}
