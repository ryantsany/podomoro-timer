package com.agiztya.podomoro.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agiztya.podomoro.data.local.entity.PomodoroSession
import com.agiztya.podomoro.data.local.entity.PomodoroSetting
import com.agiztya.podomoro.data.repository.PomodoroRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TimerViewModel(private val repository: PomodoroRepository) : ViewModel() {

    private val _settings = repository.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PomodoroSetting())

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _currentTime = MutableStateFlow(25 * 60 * 1000L)
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    private val _showCompleteScreen = MutableStateFlow(false)
    val showCompleteScreen: StateFlow<Boolean> = _showCompleteScreen.asStateFlow()

    private val _taskName = MutableStateFlow("")
    val taskName: StateFlow<String> = _taskName.asStateFlow()

    private var timerJob: Job? = null

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

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
        resetTimer()
    }

    fun onTaskNameChanged(newName: String) {
        _taskName.value = newName
    }

    fun toggleTimer() {
        if (_isTimerRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_currentTime.value > 0) {
                delay(1000L)
                _currentTime.value -= 1000L
            }
            onTimerFinished()
        }
    }

    private fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    private fun resetTimer() {
        pauseTimer()
        _currentTime.value = getDurationForTab(_selectedTab.value)
    }

    private fun onTimerFinished() {
        _isTimerRunning.value = false
        _showCompleteScreen.value = true
        
        // Save session to database
        if (_selectedTab.value == 0) {
            viewModelScope.launch {
                val session = PomodoroSession(
                    taskName = _taskName.value.ifEmpty { "Focus Session" },
                    durationMinutes = (_settings.value!!.pomodoroDuration),
                    isFocusSession = true
                )
                repository.insertSession(session)
            }
        }
    }

    fun dismissCompleteScreen() {
        _showCompleteScreen.value = false
    }

    fun takeABreak() {
        _selectedTab.value = 1
        _showCompleteScreen.value = false
        resetTimer()
    }

    fun skipBreak() {
        _selectedTab.value = 0
        _showCompleteScreen.value = false
        resetTimer()
    }

    fun extendBreak() {
        _currentTime.value = 5 * 60 * 1000L
        _showCompleteScreen.value = false
        startTimer()
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
        return 25 * 60 * 1000L
    }
}
