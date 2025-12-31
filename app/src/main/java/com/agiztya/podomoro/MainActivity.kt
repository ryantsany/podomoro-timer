package com.agiztya.podomoro

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agiztya.podomoro.data.local.PomodoroDatabase
import com.agiztya.podomoro.data.repository.PomodoroRepository
import com.agiztya.podomoro.service.TimerService
import com.agiztya.podomoro.ui.settings.SettingsViewModel
import com.agiztya.podomoro.ui.splash.SplashScreen
import com.agiztya.podomoro.ui.theme.PodomoroTimerTheme
import com.agiztya.podomoro.ui.timer.TimerScreen
import com.agiztya.podomoro.ui.timer.TimerViewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    
    private var timerService: TimerService? = null
    private var isBound = false
    private var timerViewModel: TimerViewModel? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            isBound = true
            
            // Bind ViewModel to service
            timerViewModel?.bindToService(binder.getService(), this@MainActivity)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            timerViewModel?.unbindFromService()
        }
    }

    // Permission request launcher for Android 13+
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission result handled - notifications will work if granted
        // If not granted, the app will still function but without notifications
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition { false }
        
        // Request notification permission for Android 13+
        requestNotificationPermission()
        
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

        // Bind to TimerService
        Intent(this, TimerService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
            PodomoroTimerTheme(darkTheme = isDarkMode) {
                val timerVm: TimerViewModel = viewModel(factory = viewModelFactory)
                
                // Store reference for service binding
                timerViewModel = timerVm
                
                // If service is already bound, connect the ViewModel
                timerService?.let { service ->
                    timerVm.bindToService(service, this)
                }

                var showSplash by remember { mutableStateOf(true) }

                if(showSplash){
                    SplashScreen(
                        onFinish = { showSplash = false }
                    )
                }else{
                    TimerScreen(viewModel = timerVm, settingsViewModel = settingsViewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh timer when returning from settings
        timerViewModel?.refreshFromSettings()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            timerViewModel?.unbindFromService()
            unbindService(connection)
            isBound = false
        }
    }

    /**
     * Requests notification permission for Android 13 (API 33) and above.
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show rationale if needed, then request
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Directly request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}
