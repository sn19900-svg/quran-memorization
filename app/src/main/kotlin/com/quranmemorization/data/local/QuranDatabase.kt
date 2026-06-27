package com.quranmemorization.data.local

import android.content.Context
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

/**
 * QuranDatabase — single Room database for the entire application.
 *
 * Version history:
 *  v1 — Initial schema: quran_schedule, user_progress, mistake_log
 *
 * The database is created lazily (first access) and lives in the app's
 * private storage.  Hilt provides the singleton via [DatabaseModule].
 */
@Database(
    entities  = [QuranSchedule::class, UserProgress::class, MistakeLog::class],
    version   = 1,
    exportSchema = true,   // schema JSON exported to app/schemas/ for migration auditing
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

/**
 * Converters — Room type converters for enums stored as strings.
 * Storing as strings (not ordinals) is safer across schema migrations.
 */
class Converters {
    // MistakeLog.MistakeType
    @androidx.room.TypeConverter
    fun mistakeTypeToString(value: MistakeLog.MistakeType): String = value.name

    @androidx.room.TypeConverter
    fun stringToMistakeType(value: String): MistakeLog.MistakeType =
        MistakeLog.MistakeType.valueOf(value)

    // MistakeLog.SessionType
    @androidx.room.TypeConverter
    fun sessionTypeToString(value: MistakeLog.SessionType): String = value.name

    @androidx.room.TypeConverter
    fun stringToSessionType(value: String): MistakeLog.SessionType =
        MistakeLog.SessionType.valueOf(value)
}
