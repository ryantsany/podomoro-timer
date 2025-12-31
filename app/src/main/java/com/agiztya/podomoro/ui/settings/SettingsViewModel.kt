package com.agiztya.podomoro.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agiztya.podomoro.data.local.entity.PomodoroSetting
import com.agiztya.podomoro.data.repository.PomodoroRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: PomodoroRepository) : ViewModel() {

    private val _settings = repository.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PomodoroSetting())

    val podomoroLength: StateFlow<Int> = _settings
        .map { it?.pomodoroDuration ?: 25 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 25)

    val shortBreakLength: StateFlow<Int> = _settings
        .map { it?.shortBreakDuration ?: 5 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    val longBreakLength: StateFlow<Int> = _settings
        .map { it?.longBreakDuration ?: 15 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 15)

    val isDarkMode: StateFlow<Boolean> = _settings
        .map { it?.isDarkMode ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleDarkMode(isEnabled: Boolean) {
        updateSettings { it.copy(isDarkMode = isEnabled) }
    }

    fun updatePomodoroLength(newLength: Int) {
        updateSettings { it.copy(pomodoroDuration = newLength) }
    }

    fun updateShortBreakLength(newLength: Int) {
        updateSettings { it.copy(shortBreakDuration = newLength) }
    }

    fun updateLongBreakLength(newLength: Int) {
        updateSettings { it.copy(longBreakDuration = newLength) }
    }

    private fun updateSettings(transform: (PomodoroSetting) -> PomodoroSetting) {
        viewModelScope.launch {
            val current = _settings.value ?: PomodoroSetting()
            repository.saveSettings(transform(current))
        }
    }
}
