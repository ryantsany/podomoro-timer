package com.agiztya.podomoro.ui.timer

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agiztya.podomoro.data.local.entity.PomodoroSetting
import com.agiztya.podomoro.data.repository.PomodoroRepository
import com.agiztya.podomoro.service.TimerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Timer screen.
 * Delegates all timer logic to TimerService and observes its state.
 */
class TimerViewModel(private val repository: PomodoroRepository) : ViewModel() {

    private val _settings = repository.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PomodoroSetting())

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _taskName = MutableStateFlow("")
    val taskName: StateFlow<String> = _taskName.asStateFlow()

    // These will be bound to TimerService when available
    private var timerService: TimerService? = null
    private var applicationContext: Context? = null

    // Local state flows that mirror service state (for when service is bound)
    private val _currentTime = MutableStateFlow(25 * 60 * 1000L)
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _showCompleteScreen = MutableStateFlow(false)
    val showCompleteScreen: StateFlow<Boolean> = _showCompleteScreen.asStateFlow()

    init {
        // Initialize current time based on settings when they are loaded
        viewModelScope.launch {
            _settings.collect { settings ->
                if (!_isTimerRunning.value && !_showCompleteScreen.value) {
                    _currentTime.value = getDurationForTab(_selectedTab.value)
                }
            }
        }
    }

    /**
     * Binds the ViewModel to the TimerService for state synchronization.
     */
    fun bindToService(service: TimerService, context: Context) {
        timerService = service
        applicationContext = context.applicationContext
        
        // Observe service state flows
        viewModelScope.launch {
            service.currentTime.collect { time ->
                _currentTime.value = time
            }
        }
        
        viewModelScope.launch {
            service.isTimerRunning.collect { running ->
                _isTimerRunning.value = running
            }
        }
        
        viewModelScope.launch {
            service.isTimerFinished.collect { finished ->
                _showCompleteScreen.value = finished
            }
        }
        
        viewModelScope.launch {
            service.timerMode.collect { mode ->
                _selectedTab.value = when (mode) {
                    TimerService.TimerMode.FOCUS -> 0
                    TimerService.TimerMode.SHORT_BREAK -> 1
                    TimerService.TimerMode.LONG_BREAK -> 2
                }
            }
        }
    }

    /**
     * Unbinds from the TimerService.
     */
    fun unbindFromService() {
        timerService = null
    }

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
        
        val mode = when (index) {
            0 -> TimerService.TimerMode.FOCUS
            1 -> TimerService.TimerMode.SHORT_BREAK
            2 -> TimerService.TimerMode.LONG_BREAK
            else -> TimerService.TimerMode.FOCUS
        }
        
        timerService?.setTimerMode(mode) ?: run {
            // Fallback when service not bound
            _currentTime.value = getDurationForTab(index)
        }
    }

    fun onTaskNameChanged(newName: String) {
        _taskName.value = newName
        timerService?.setTaskName(newName)
    }

    fun toggleTimer() {
        val context = applicationContext ?: return
        
        if (_isTimerRunning.value) {
            pauseTimer(context)
        } else {
            startTimer(context)
        }
    }

    private fun startTimer(context: Context) {
        val duration = getDurationForTab(_selectedTab.value)
        val mode = when (_selectedTab.value) {
            0 -> TimerService.TimerMode.FOCUS
            1 -> TimerService.TimerMode.SHORT_BREAK
            2 -> TimerService.TimerMode.LONG_BREAK
            else -> TimerService.TimerMode.FOCUS
        }
        
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_DURATION, duration)
            putExtra(TimerService.EXTRA_MODE, mode.ordinal)
            putExtra(TimerService.EXTRA_TASK_NAME, _taskName.value)
        }
        context.startService(intent)
    }

    private fun pauseTimer(context: Context) {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_PAUSE
        }
        context.startService(intent)
    }

    fun stopTimer() {
        val context = applicationContext ?: return
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_STOP
        }
        context.startService(intent)
        
        // Reset local state
        _currentTime.value = getDurationForTab(_selectedTab.value)
    }

    fun dismissCompleteScreen() {
        _showCompleteScreen.value = false
        timerService?.resetFinishedState()
    }

    fun takeABreak() {
        _selectedTab.value = 1
        _showCompleteScreen.value = false
        timerService?.apply {
            resetFinishedState()
            setTimerMode(TimerService.TimerMode.SHORT_BREAK)
        } ?: run {
            _currentTime.value = getDurationForTab(1)
        }
    }

    fun skipBreak() {
        _selectedTab.value = 0
        _showCompleteScreen.value = false
        timerService?.apply {
            resetFinishedState()
            setTimerMode(TimerService.TimerMode.FOCUS)
        } ?: run {
            _currentTime.value = getDurationForTab(0)
        }
    }

    fun extendBreak() {
        val context = applicationContext ?: return
        _showCompleteScreen.value = false
        
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_DURATION, 5 * 60 * 1000L)
            putExtra(TimerService.EXTRA_MODE, TimerService.TimerMode.SHORT_BREAK.ordinal)
        }
        context.startService(intent)
    }

    fun refreshFromSettings() {
        timerService?.refreshDuration() ?: run {
            viewModelScope.launch {
                _currentTime.value = getDurationForTab(_selectedTab.value)
            }
        }
    }

    private fun getDurationForTab(index: Int): Long {
        val settings = _settings.value
        if (settings != null) {
            return when (index) {
                0 -> settings.pomodoroDuration * 60 * 1000L
                1 -> settings.shortBreakDuration * 60 * 1000L
                2 -> settings.longBreakDuration * 60 * 1000L
                else -> settings.pomodoroDuration * 60 * 1000L
            }
        }
        return when (index) {
            0 -> 25 * 60 * 1000L
            1 -> 5 * 60 * 1000L
            2 -> 15 * 60 * 1000L
            else -> 25 * 60 * 1000L
        }
    }
}
