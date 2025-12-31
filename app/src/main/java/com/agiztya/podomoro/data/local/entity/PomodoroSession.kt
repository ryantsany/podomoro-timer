package com.agiztya.podomoro.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val taskName: String,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val isFocusSession: Boolean = true
)
