package com.agiztya.podomoro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.agiztya.podomoro.data.local.dao.PomodoroDao
import com.agiztya.podomoro.data.local.dao.PomodoroSettingDao
import com.agiztya.podomoro.data.local.entity.PomodoroSession
import com.agiztya.podomoro.data.local.entity.PomodoroSetting

@Database(
    entities = [PomodoroSession::class, PomodoroSetting::class],
    version = 2,
    exportSchema = false
)
abstract class PomodoroDatabase : RoomDatabase() {

    abstract fun pomodoroDao(): PomodoroDao
    abstract fun pomodoroSettingDao(): PomodoroSettingDao

    companion object {
        @Volatile
        private var INSTANCE: PomodoroDatabase? = null

        fun getDatabase(context: Context): PomodoroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PomodoroDatabase::class.java,
                    "pomodoro_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
