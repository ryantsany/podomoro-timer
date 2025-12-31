package com.agiztya.podomoro.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.agiztya.podomoro.data.local.PomodoroDatabase
import com.agiztya.podomoro.data.local.entity.PomodoroSession
import com.agiztya.podomoro.data.repository.PomodoroRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TimerService : Service() {

    private val binder = TimerBinder()
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var timerJob: Job? = null

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var repository: PomodoroRepository

    // Timer Mode enum
    enum class TimerMode(val displayName: String) {
        FOCUS("Focus"),
        SHORT_BREAK("Short Break"),
        LONG_BREAK("Long Break")
    }

    // State flows - Single source of truth
    private val _currentTime = MutableStateFlow(25 * 60 * 1000L)
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerMode = MutableStateFlow(TimerMode.FOCUS)
    val timerMode: StateFlow<TimerMode> = _timerMode.asStateFlow()

    private val _isTimerFinished = MutableStateFlow(false)
    val isTimerFinished: StateFlow<Boolean> = _isTimerFinished.asStateFlow()

    private var totalDuration = 25 * 60 * 1000L
    private var taskName: String = ""

    companion object {
        const val ACTION_START = "com.agiztya.podomoro.ACTION_START"
        const val ACTION_PAUSE = "com.agiztya.podomoro.ACTION_PAUSE"
        const val ACTION_RESUME = "com.agiztya.podomoro.ACTION_RESUME"
        const val ACTION_STOP = "com.agiztya.podomoro.ACTION_STOP"
        const val ACTION_SET_MODE = "com.agiztya.podomoro.ACTION_SET_MODE"

        const val EXTRA_DURATION = "EXTRA_DURATION"
        const val EXTRA_MODE = "EXTRA_MODE"
        const val EXTRA_TASK_NAME = "EXTRA_TASK_NAME"
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        
        // Initialize repository
        val database = PomodoroDatabase.getDatabase(this)
        repository = PomodoroRepository(database.pomodoroDao(), database.pomodoroSettingDao())
        
        // Load initial settings
        serviceScope.launch {
            loadSettingsForMode(_timerMode.value)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val duration = intent.getLongExtra(EXTRA_DURATION, 0L)
                val modeOrdinal = intent.getIntExtra(EXTRA_MODE, 0)
                taskName = intent.getStringExtra(EXTRA_TASK_NAME) ?: ""
                
                val mode = TimerMode.entries.getOrElse(modeOrdinal) { TimerMode.FOCUS }
                _timerMode.value = mode
                
                if (duration > 0) {
                    totalDuration = duration
                    _currentTime.value = duration
                }
                
                startTimer()
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> stopTimer()
            ACTION_SET_MODE -> {
                val modeOrdinal = intent.getIntExtra(EXTRA_MODE, 0)
                val mode = TimerMode.entries.getOrElse(modeOrdinal) { TimerMode.FOCUS }
                setTimerMode(mode)
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceJob.cancel()
    }

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    @SuppressLint("ForegroundServiceType")
    private fun startTimer() {
        if (_isTimerRunning.value) return
        
        _isTimerRunning.value = true
        _isTimerFinished.value = false
        
        // Start foreground with notification
        val totalMinutes = (totalDuration / 1000 / 60).toInt()
        val notification = notificationHelper.buildOngoingNotification(
            _currentTime.value,
            true,
            _timerMode.value.displayName,
            taskName,
            totalMinutes
        )
        startForeground(NotificationHelper.NOTIFICATION_ID_ONGOING, notification)
        
        timerJob = serviceScope.launch {
            while (_currentTime.value > 0 && _isTimerRunning.value) {
                delay(1000L)
                _currentTime.value -= 1000L
                notificationHelper.updateOngoingNotification(
                    _currentTime.value,
                    true,
                    _timerMode.value.displayName,
                    taskName,
                    totalMinutes
                )
            }
            
            if (_currentTime.value <= 0) {
                onTimerFinished()
            }
        }
    }

    private fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        
        val totalMinutes = (totalDuration / 1000 / 60).toInt()
        notificationHelper.updateOngoingNotification(
            _currentTime.value,
            false,
            _timerMode.value.displayName,
            taskName,
            totalMinutes
        )
        
        stopForeground(STOP_FOREGROUND_DETACH)
    }

    @SuppressLint("ForegroundServiceType")
    private fun resumeTimer() {
        if (_isTimerRunning.value) return
        
        _isTimerRunning.value = true
        
        val totalMinutes = (totalDuration / 1000 / 60).toInt()
        val notification = notificationHelper.buildOngoingNotification(
            _currentTime.value,
            true,
            _timerMode.value.displayName,
            taskName,
            totalMinutes
        )
        startForeground(NotificationHelper.NOTIFICATION_ID_ONGOING, notification)
        
        timerJob = serviceScope.launch {
            while (_currentTime.value > 0 && _isTimerRunning.value) {
                delay(1000L)
                _currentTime.value -= 1000L
                notificationHelper.updateOngoingNotification(
                    _currentTime.value,
                    true,
                    _timerMode.value.displayName,
                    taskName,
                    totalMinutes
                )
            }
            
            if (_currentTime.value <= 0) {
                onTimerFinished()
            }
        }
    }

    private fun stopTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        
        serviceScope.launch {
            loadSettingsForMode(_timerMode.value)
        }
        
        notificationHelper.cancelOngoingNotification()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun onTimerFinished() {
        _isTimerRunning.value = false
        _isTimerFinished.value = true
        
        // Show finished notification
        notificationHelper.showFinishedNotification(_timerMode.value.displayName)
        notificationHelper.cancelOngoingNotification()
        
        // Save session to database if it was a focus session
        if (_timerMode.value == TimerMode.FOCUS) {
            serviceScope.launch {
                val durationMinutes = (totalDuration / 1000 / 60).toInt()
                val session = PomodoroSession(
                    taskName = taskName.ifEmpty { "Focus Session" },
                    durationMinutes = durationMinutes,
                    isFocusSession = true
                )
                repository.insertSession(session)
            }
        }
        
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    fun setTimerMode(mode: TimerMode) {
        if (_isTimerRunning.value) return // Don't change mode while running
        
        _timerMode.value = mode
        _isTimerFinished.value = false
        
        serviceScope.launch {
            loadSettingsForMode(mode)
        }
    }

    fun setTaskName(name: String) {
        taskName = name
    }

    fun resetFinishedState() {
        _isTimerFinished.value = false
    }

    private suspend fun loadSettingsForMode(mode: TimerMode) {
        val settings = repository.getSettings().first()
        val duration = if (settings != null) {
            when (mode) {
                TimerMode.FOCUS -> settings.pomodoroDuration * 60 * 1000L
                TimerMode.SHORT_BREAK -> settings.shortBreakDuration * 60 * 1000L
                TimerMode.LONG_BREAK -> settings.longBreakDuration * 60 * 1000L
            }
        } else {
            when (mode) {
                TimerMode.FOCUS -> 25 * 60 * 1000L
                TimerMode.SHORT_BREAK -> 5 * 60 * 1000L
                TimerMode.LONG_BREAK -> 15 * 60 * 1000L
            }
        }
        
        totalDuration = duration
        _currentTime.value = duration
    }

    fun refreshDuration() {
        if (!_isTimerRunning.value) {
            serviceScope.launch {
                loadSettingsForMode(_timerMode.value)
            }
        }
    }
}
