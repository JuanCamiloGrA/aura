package com.humans.aura.core.di

import org.junit.Test
import org.koin.dsl.module
import org.koin.test.verify.verify

class AppModulesTest {

    @Test
    fun app_modules_verify() {
        module {
            includes(*appModules.toTypedArray())
        }.verify()
    }
}
