package com.humans.aura.core.services.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.humans.aura.core.services.database.dao.ActivityDao
import com.humans.aura.core.services.database.dao.DailyGoalDao
import com.humans.aura.core.services.database.entity.ActivityEntity
import com.humans.aura.core.services.database.entity.DailyGoalEntity
import com.humans.aura.core.services.database.entity.GoalSubtaskEntity

@Database(
    entities = [
        ActivityEntity::class,
        DailyGoalEntity::class,
        GoalSubtaskEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao

    abstract fun dailyGoalDao(): DailyGoalDao

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
    }
}
