package com.agiztya.podomoro

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agiztya.podomoro.data.local.PomodoroDatabase
import com.agiztya.podomoro.data.repository.PomodoroRepository
import com.agiztya.podomoro.service.TimerService
import com.agiztya.podomoro.ui.settings.SettingsViewModel
import com.agiztya.podomoro.ui.theme.PodomoroTimerTheme
import com.agiztya.podomoro.ui.timer.TimerScreen
import com.agiztya.podomoro.ui.timer.TimerViewModel

class MainActivity : ComponentActivity() {
    
    private var timerService: TimerService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = PomodoroDatabase.getDatabase(this)
        val repository = PomodoroRepository(database.pomodoroDao(), database.pomodoroSettingDao())
        
        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(TimerViewModel::class.java) -> TimerViewModel(repository) as T
                    modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(repository) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }

        Intent(this, TimerService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        enableEdgeToEdge()
        setContent {
            PodomoroTimerTheme {
                val timerViewModel: TimerViewModel = viewModel(factory = viewModelFactory)
                val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
                TimerScreen(viewModel = timerViewModel, settingsViewModel = settingsViewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }
}
