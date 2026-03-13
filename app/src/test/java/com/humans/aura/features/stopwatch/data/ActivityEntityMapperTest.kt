package com.humans.aura.features.stopwatch.data

import com.humans.aura.core.domain.models.Activity
import com.humans.aura.core.domain.models.ActivityStatus
import com.humans.aura.core.services.database.entity.ActivityEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityEntityMapperTest {

    @Test
    fun entity_to_domain_maps_all_fields() {
        val entity = ActivityEntity(1, "Focus", 10L, null, "ACTIVE", false)

        assertEquals(Activity(1, "Focus", 10L, null, ActivityStatus.ACTIVE, false), entity.toDomain())
    }

    @Test
    fun domain_to_entity_maps_all_fields() {
        val domain = Activity(2, "Review", 20L, 40L, ActivityStatus.LOST, false)

        assertEquals(ActivityEntity(2, "Review", 20L, 40L, "LOST", false), domain.toEntity())
    }
}
