package com.agiztya.podomoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agiztya.podomoro.data.local.PomodoroDatabase
import com.agiztya.podomoro.data.repository.PomodoroRepository
import com.agiztya.podomoro.ui.stats.StatScreen
import com.agiztya.podomoro.ui.stats.StatsViewModel
import com.agiztya.podomoro.ui.theme.PodomoroTimerTheme

class StatsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = PomodoroDatabase.getDatabase(this)
        val repository = PomodoroRepository(database.pomodoroDao(), database.pomodoroSettingDao())
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StatsViewModel(repository) as T
            }
        }

        enableEdgeToEdge()
        setContent {
            PodomoroTimerTheme {
                val statsViewModel: StatsViewModel = viewModel(factory = viewModelFactory)
                StatScreen(
                    viewModel = statsViewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}
