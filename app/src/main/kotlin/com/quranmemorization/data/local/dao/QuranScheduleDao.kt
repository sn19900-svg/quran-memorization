package com.quranmemorization.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quranmemorization.data.local.entity.QuranSchedule
import kotlinx.coroutines.flow.Flow

/**
 * QuranScheduleDao — read-only after initial seeding.
 *
 * The schedule is immutable (365 pre-computed rows); only the seed utility
 * writes here.  All queries return [Flow] so the UI reacts to DB changes.
 */
@Dao
interface QuranScheduleDao {

    // ── Seeding ──────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(schedules: List<QuranSchedule>)

    // ── Single-day lookup ─────────────────────────────────────────────────────

    @Query("SELECT * FROM quran_schedule WHERE day_number = :dayNumber LIMIT 1")
    suspend fun getByDay(dayNumber: Int): QuranSchedule?

    @Query("SELECT * FROM quran_schedule WHERE day_number = :dayNumber LIMIT 1")
    fun observeByDay(dayNumber: Int): Flow<QuranSchedule?>

    // ── Range / list queries ──────────────────────────────────────────────────

    /** All 365 rows ordered by day — used for the full schedule screen. */
    @Query("SELECT * FROM quran_schedule ORDER BY day_number ASC")
    fun observeAll(): Flow<List<QuranSchedule>>

    /** Days belonging to a specific Juz', used for Juz'-level progress view. */
    @Query("SELECT * FROM quran_schedule WHERE juz_number = :juz ORDER BY day_number ASC")
    fun observeByJuz(juz: Int): Flow<List<QuranSchedule>>

    /** Quick count to verify seeding completed. */
    @Query("SELECT COUNT(*) FROM quran_schedule")
    suspend fun count(): Int
}
