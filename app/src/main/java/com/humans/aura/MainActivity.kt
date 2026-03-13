package com.humans.aura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.humans.aura.core.presentation.AuraApp
import com.humans.aura.core.presentation.theme.AuraTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AuraTheme {
                AuraApp()
            }
        }
    }
}
