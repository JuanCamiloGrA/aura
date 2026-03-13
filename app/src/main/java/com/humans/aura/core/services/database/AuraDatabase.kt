package com.humans.aura.core.services.database

import androidx.room.Database
import androidx.room.RoomDatabase
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
    version = 1,
    exportSchema = true,
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao

    abstract fun dailyGoalDao(): DailyGoalDao
}
