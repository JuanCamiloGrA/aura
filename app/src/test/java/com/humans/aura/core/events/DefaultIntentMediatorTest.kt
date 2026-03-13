package com.humans.aura.core.events

import app.cash.turbine.test
import com.humans.aura.core.domain.models.AppIntent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultIntentMediatorTest {

    @Test
    fun emit_delivers_sleep_intent() = runTest {
        val mediator = DefaultIntentMediator()

        mediator.intents.test {
            val expected = AppIntent.SleepLogged("Sleep", 123L)
            mediator.emit(expected)

            assert(awaitItem() == expected)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
