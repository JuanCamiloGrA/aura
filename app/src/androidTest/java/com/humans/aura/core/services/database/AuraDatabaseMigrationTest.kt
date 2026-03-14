package com.humans.aura.core.services.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class AuraDatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AuraDatabase::class.java,
        listOf(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrate_1_to_2() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("INSERT INTO daily_goals (id, day_start_epoch_millis, main_title, is_synced_to_d1) VALUES (1, 0, 'Goal', 0)")
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 2, true, AuraDatabase.MIGRATION_1_2)
    }

    @Test
    fun migrate_2_to_3_backfills_pending_summaries_and_rebuilds_daily_goals() {
        helper.createDatabase(TEST_DB, 2).apply {
            execSQL("INSERT INTO daily_goals (id, day_start_epoch_millis, main_title, is_ai_generation_pending, is_synced_to_d1) VALUES (1, 1000, 'Close loops', 1, 0)")
            execSQL("INSERT INTO daily_goals (id, day_start_epoch_millis, main_title, is_ai_generation_pending, is_synced_to_d1) VALUES (2, 2000, 'Deep work', 0, 1)")
            execSQL("INSERT INTO goal_subtasks (id, goal_id, title, is_completed, position, is_synced_to_d1) VALUES (1, 1, 'Review', 1, 0, 0)")
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 3, true, AuraDatabase.MIGRATION_2_3).apply {
            assertEquals(
                listOf("1000|PENDING|pending"),
                queryRows(
                    "SELECT day_start_epoch_millis, generation_status, model_name FROM daily_summaries ORDER BY day_start_epoch_millis ASC",
                ),
            )
            assertFalse(columnNames("daily_goals").contains("is_ai_generation_pending"))
            assertEquals(
                listOf("chat_messages", "chat_sessions"),
                queryRows(
                    "SELECT name FROM sqlite_master WHERE type = 'table' AND name IN ('chat_sessions', 'chat_messages') ORDER BY name ASC",
                ),
            )
        }
    }

    private fun SupportSQLiteDatabase.columnNames(tableName: String): List<String> =
        query("PRAGMA table_info($tableName)").use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.getString(nameIndex))
                }
            }
        }

    private fun SupportSQLiteDatabase.queryRows(sql: String): List<String> =
        query(sql).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add((0 until cursor.columnCount).joinToString("|") { index -> cursor.getString(index) })
                }
            }
        }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
