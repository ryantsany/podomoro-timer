package com.agiztya.podomoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agiztya.podomoro.data.local.PomodoroDatabase
import com.agiztya.podomoro.data.repository.PomodoroRepository
import com.agiztya.podomoro.ui.settings.SettingScreen
import com.agiztya.podomoro.ui.settings.SettingsViewModel
import com.agiztya.podomoro.ui.theme.PodomoroTimerTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = PomodoroDatabase.getDatabase(this)
        val repository = PomodoroRepository(database.pomodoroDao(), database.pomodoroSettingDao())
        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(repository) as T
            }
        }

        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
            PodomoroTimerTheme(darkTheme = isDarkMode) {
                SettingScreen(
                    viewModel = settingsViewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}
