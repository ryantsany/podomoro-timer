package com.agiztya.podomoro.data.repository

import com.agiztya.podomoro.data.local.dao.PomodoroDao
import com.agiztya.podomoro.data.local.dao.PomodoroSettingDao
import com.agiztya.podomoro.data.local.entity.PomodoroSession
import com.agiztya.podomoro.data.local.entity.PomodoroSetting
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class PomodoroRepository(
    private val pomodoroDao: PomodoroDao,
    private val settingDao: PomodoroSettingDao
) {

    fun getAllSessions(): Flow<List<PomodoroSession>> = pomodoroDao.getAllSessions()

    fun getTodayFocusMinutes(): Flow<Int?> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return pomodoroDao.getTodayFocusMinutes(calendar.timeInMillis)
    }

    fun getTodaySessionsCount(): Flow<Int> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return pomodoroDao.getTodaySessionsCount(calendar.timeInMillis)
    }

    suspend fun insertSession(session: PomodoroSession) {
        pomodoroDao.insertSession(session)
    }

    fun getSessionsForLast7Days(): Flow<List<PomodoroSession>> {
        val sevenDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return pomodoroDao.getSessionsSince(sevenDaysAgo)
    }

    // Settings logic
    fun getSettings(): Flow<PomodoroSetting?> = settingDao.getSettings()

    suspend fun saveSettings(setting: PomodoroSetting) {
        settingDao.saveSettings(setting)
    }
}
