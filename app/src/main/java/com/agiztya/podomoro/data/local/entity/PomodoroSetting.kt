package com.agiztya.podomoro.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pomodoro_settings")
data class PomodoroSetting(
    @PrimaryKey val id: Int = 0, // We only need one row for settings
    val pomodoroDuration: Int = 25,
    val shortBreakDuration: Int = 5,
    val longBreakDuration: Int = 15,
    val isDarkMode: Boolean = false
)
