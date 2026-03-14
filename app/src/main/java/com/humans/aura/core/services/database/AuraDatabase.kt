package com.humans.aura.core.services.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.humans.aura.core.services.database.dao.ActivityDao
import com.humans.aura.core.services.database.dao.ChatDao
import com.humans.aura.core.services.database.dao.DailyGoalDao
import com.humans.aura.core.services.database.dao.DaySummaryDao
import com.humans.aura.core.services.database.entity.chat.ChatMessageEntity
import com.humans.aura.core.services.database.entity.chat.ChatSessionEntity
import com.humans.aura.core.services.database.entity.ActivityEntity
import com.humans.aura.core.services.database.entity.DailyGoalEntity
import com.humans.aura.core.services.database.entity.GoalSubtaskEntity
import com.humans.aura.core.services.database.entity.summary.DailySummaryEntity

@Database(
    entities = [
        ActivityEntity::class,
        DailyGoalEntity::class,
        GoalSubtaskEntity::class,
        DailySummaryEntity::class,
        ChatSessionEntity::class,
        ChatMessageEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao

    abstract fun dailyGoalDao(): DailyGoalDao

    abstract fun daySummaryDao(): DaySummaryDao

    abstract fun chatDao(): ChatDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE daily_goals ADD COLUMN is_ai_generation_pending INTEGER NOT NULL DEFAULT 0",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_activities_start_time_epoch_millis ON activities(start_time_epoch_millis)",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_activities_end_time_epoch_millis ON activities(end_time_epoch_millis)",
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("PRAGMA foreign_keys=OFF")
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS daily_summaries (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, day_start_epoch_millis INTEGER NOT NULL, summary_text TEXT, raw_context_json TEXT NOT NULL, prompt_version TEXT NOT NULL, model_name TEXT NOT NULL, generation_status TEXT NOT NULL, error_message TEXT, last_attempt_epoch_millis INTEGER, created_at_epoch_millis INTEGER NOT NULL, updated_at_epoch_millis INTEGER NOT NULL, is_synced_to_d1 INTEGER NOT NULL)",
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_daily_summaries_day_start_epoch_millis ON daily_summaries(day_start_epoch_millis)",
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS chat_sessions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, created_at_epoch_millis INTEGER NOT NULL, updated_at_epoch_millis INTEGER NOT NULL, is_synced_to_d1 INTEGER NOT NULL)",
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS chat_messages (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, session_id INTEGER NOT NULL, role TEXT NOT NULL, original_text TEXT NOT NULL, normalized_english_text TEXT NOT NULL, source_language_code TEXT NOT NULL, created_at_epoch_millis INTEGER NOT NULL, is_synced_to_d1 INTEGER NOT NULL, FOREIGN KEY(session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE)",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_chat_messages_session_id ON chat_messages(session_id)",
                )
                database.execSQL(
                    "INSERT INTO daily_summaries(day_start_epoch_millis, summary_text, raw_context_json, prompt_version, model_name, generation_status, error_message, last_attempt_epoch_millis, created_at_epoch_millis, updated_at_epoch_millis, is_synced_to_d1) SELECT day_start_epoch_millis, NULL, '{}', 'm2-initial', 'pending', 'PENDING', NULL, NULL, day_start_epoch_millis, day_start_epoch_millis, 0 FROM daily_goals WHERE is_ai_generation_pending = 1",
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS daily_goals_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, day_start_epoch_millis INTEGER NOT NULL, main_title TEXT NOT NULL, is_synced_to_d1 INTEGER NOT NULL)",
                )
                database.execSQL(
                    "INSERT INTO daily_goals_new(id, day_start_epoch_millis, main_title, is_synced_to_d1) SELECT id, day_start_epoch_millis, main_title, is_synced_to_d1 FROM daily_goals",
                )
                database.execSQL("DROP TABLE daily_goals")
                database.execSQL("ALTER TABLE daily_goals_new RENAME TO daily_goals")
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_daily_goals_day_start_epoch_millis ON daily_goals(day_start_epoch_millis)",
                )
                database.execSQL("PRAGMA foreign_keys=ON")
            }
        }
    }
}
