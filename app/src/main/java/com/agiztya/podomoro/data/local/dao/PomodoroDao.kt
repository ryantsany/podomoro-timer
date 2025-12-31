package com.agiztya.podomoro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.agiztya.podomoro.data.local.entity.PomodoroSession
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroDao {
    @Insert
    suspend fun insertSession(session: PomodoroSession)

    @Query("SELECT * FROM pomodoro_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<PomodoroSession>>

    @Query("SELECT SUM(durationMinutes) FROM pomodoro_sessions WHERE isFocusSession = 1 AND timestamp >= :startOfDay")
    fun getTodayFocusMinutes(startOfDay: Long): Flow<Int?>

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE isFocusSession = 1 AND timestamp >= :startOfDay")
    fun getTodaySessionsCount(startOfDay: Long): Flow<Int>
    
    @Query("SELECT * FROM pomodoro_sessions WHERE timestamp >= :startTime ORDER BY timestamp ASC")
    fun getSessionsSince(startTime: Long): Flow<List<PomodoroSession>> 
}
