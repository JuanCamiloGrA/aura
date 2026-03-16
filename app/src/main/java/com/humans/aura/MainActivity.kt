package com.humans.aura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.humans.aura.core.presentation.AuraApp
import com.humans.aura.features.configuration.presentation.ConfigurationThemeHost

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            ConfigurationThemeHost {
                AuraApp()
            }
        }
    }
}
