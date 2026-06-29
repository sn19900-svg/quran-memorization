package com.quranmemorization.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.quranmemorization.data.local.dao.MistakeLogDao
import com.quranmemorization.data.local.dao.QuranScheduleDao
import com.quranmemorization.data.local.dao.UserProgressDao
import com.quranmemorization.data.local.entity.MistakeLog
import com.quranmemorization.data.local.entity.QuranSchedule
import com.quranmemorization.data.local.entity.UserProgress

@Database(
    entities  = [QuranSchedule::class, UserProgress::class, MistakeLog::class],
    version   = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class QuranDatabase : RoomDatabase() {

    abstract fun quranScheduleDao(): QuranScheduleDao
    abstract fun userProgressDao():  UserProgressDao
    abstract fun mistakeLogDao():    MistakeLogDao

    companion object {
        const val DATABASE_NAME = "quran_memorization.db"
    }
}

class Converters {
    @androidx.room.TypeConverter
    fun mistakeTypeToString(value: MistakeLog.MistakeType): String = value.name

    @androidx.room.TypeConverter
    fun stringToMistakeType(value: String): MistakeLog.MistakeType =
        MistakeLog.MistakeType.valueOf(value)

    @androidx.room.TypeConverter
    fun sessionTypeToString(value: MistakeLog.SessionType): String = value.name

    @androidx.room.TypeConverter
    fun stringToSessionType(value: String): MistakeLog.SessionType =
        MistakeLog.SessionType.valueOf(value)
}
