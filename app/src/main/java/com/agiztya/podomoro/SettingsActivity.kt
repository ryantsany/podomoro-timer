package com.agiztya.podomoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.agiztya.podomoro.ui.settings.SettingScreen
import com.agiztya.podomoro.ui.theme.PodomoroTimerTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PodomoroTimerTheme {
                SettingScreen(onBack = { finish() })
            }
        }
    }
}