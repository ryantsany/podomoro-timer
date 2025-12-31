package com.agiztya.podomoro.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agiztya.podomoro.data.local.entity.PomodoroSession
import com.agiztya.podomoro.data.repository.PomodoroRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

class StatsViewModel(private val repository: PomodoroRepository) : ViewModel() {

    val allSessions: StateFlow<List<PomodoroSession>> = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayFocusMinutes: StateFlow<Int> = repository.getTodayFocusMinutes()
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    val weeklyActivityData: StateFlow<List<Int>> = repository.getSessionsForLast7Days()
        .map { sessions ->
            val data = MutableList(7) { 0 }
            val calendar = Calendar.getInstance()
            val today = Calendar.getInstance()
            
            // Normalize today to start of day
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)

            sessions.filter { it.isFocusSession }.forEach { session ->
                calendar.timeInMillis = session.timestamp
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val diffMillis = today.timeInMillis - calendar.timeInMillis
                val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

                if (diffDays in 0..6) {
                    // Index 6 is today, 0 is 6 days ago
                    data[6 - diffDays] += session.durationMinutes
                }
            }
            data
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), List(7) { 0 })

    val todaySessionsCount: StateFlow<Int> = repository.getTodaySessionsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun getTodayFocusHoursAndMinutes(): Pair<Int, Int> {
        val totalMinutes = todayFocusMinutes.value
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return Pair(hours, minutes)
    }
}
