package com.agiztya.podomoro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.agiztya.podomoro.data.local.entity.PomodoroSetting
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroSettingDao {
    @Query("SELECT * FROM pomodoro_settings WHERE id = 0")
    fun getSettings(): Flow<PomodoroSetting?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(setting: PomodoroSetting)
}
