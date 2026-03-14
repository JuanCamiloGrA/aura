package com.humans.aura.core.services.database.entity.chat

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["session_id"])],
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "session_id")
    val sessionId: Long,
    val role: String,
    @ColumnInfo(name = "original_text")
    val originalText: String,
    @ColumnInfo(name = "normalized_english_text")
    val normalizedEnglishText: String,
    @ColumnInfo(name = "source_language_code")
    val sourceLanguageCode: String,
    @ColumnInfo(name = "created_at_epoch_millis")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "is_synced_to_d1")
    val isSyncedToD1: Boolean,
)
