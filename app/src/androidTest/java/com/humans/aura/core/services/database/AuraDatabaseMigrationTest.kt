package com.humans.aura.core.services.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
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

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
