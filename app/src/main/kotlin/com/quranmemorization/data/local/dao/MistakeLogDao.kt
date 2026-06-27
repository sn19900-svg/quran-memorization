package com.quranmemorization.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quranmemorization.data.local.entity.MistakeLog
import kotlinx.coroutines.flow.Flow

/**
 * MistakeLogDao — append-only log; rows are only inserted, never updated.
 *
 * Queries expose the "weak verses" surface — verses with the most logged
 * mistakes — which the Dashboard uses for the personalised review nudge.
 */
@Dao
interface MistakeLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mistake: MistakeLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mistakes: List<MistakeLog>)

    // ── Reactive queries ──────────────────────────────────────────────────────

    /** All mistakes for a single day's portion, most recent first. */
    @Query("""
        SELECT * FROM mistake_log
        WHERE day_number = :dayNumber
        ORDER BY timestamp_ms DESC
    """)
    fun observeByDay(dayNumber: Int): Flow<List<MistakeLog>>

    /** Top 10 weakest verse positions (surah + verse with most mistakes). */
    @Query("""
        SELECT * FROM mistake_log
        GROUP BY surah_number, verse_number
        ORDER BY COUNT(*) DESC
        LIMIT 10
    """)
    fun observeWeakestVerses(): Flow<List<MistakeLog>>

    /** Total mistake count, used in the summary card on the Dashboard. */
    @Query("SELECT COUNT(*) FROM mistake_log WHERE day_number = :dayNumber")
    fun observeMistakeCountForDay(dayNumber: Int): Flow<Int>

    // ── Suspend (one-shot) ────────────────────────────────────────────────────

    @Query("SELECT COUNT(*) FROM mistake_log")
    suspend fun totalCount(): Int

    @Query("DELETE FROM mistake_log WHERE day_number = :dayNumber")
    suspend fun deleteForDay(dayNumber: Int)
}
