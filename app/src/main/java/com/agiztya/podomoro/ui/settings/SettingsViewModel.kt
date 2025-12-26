package com.agiztya.podomoro.ui.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel : ViewModel() {

    // State untuk podomoro Length (Default 25)
    private val _podomoroLength = MutableStateFlow(25)
    val podomoroLength: StateFlow<Int> = _podomoroLength.asStateFlow()

    // State untuk Short Break (Default 5)
    private val _shortBreakLength = MutableStateFlow(5)
    val shortBreakLength: StateFlow<Int> = _shortBreakLength.asStateFlow()

    // State untuk Long Break (Default 15)
    private val _longBreakLength = MutableStateFlow(15)
    val longBreakLength: StateFlow<Int> = _longBreakLength.asStateFlow()

    // State untuk Dark Mode (Default false/mati)
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Fungsi untuk mengubah Dark Mode
    fun toggleDarkMode(isEnabled: Boolean) {
        _isDarkMode.value = isEnabled
    }

    // Fungsi untuk mengubah Durasi (Nanti dipanggil dari Dialog)
    fun updatepodomoroLength(newLength: Int) {
        _podomoroLength.value = newLength
    }
}