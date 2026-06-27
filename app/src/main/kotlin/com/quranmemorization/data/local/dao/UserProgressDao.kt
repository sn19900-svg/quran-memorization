package com.quranmemorization.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.quranmemorization.data.local.entity.UserProgress
import kotlinx.coroutines.flow.Flow

/**
 * UserProgressDao — full CRUD for the user's daily progress rows.
 *
 * Key design choices:
 *  • Reactive [Flow] queries let the Dashboard update the moment the user
 *    marks a task complete — no polling needed.
 *  • Ward queries (recent / distant review) are the heart of the spaced-
 *    repetition system and are kept in SQL for efficient indexing.
 */
@Dao
interface UserProgressDao {

    // ── Write ─────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(progress: UserProgress)

    @Update
    suspend fun update(progress: UserProgress)

    /** Upsert: insert or fully replace if the row already exists. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: UserProgress)

    // ── Observe (reactive) ────────────────────────────────────────────────────

    @Query("SELECT * FROM user_progress WHERE day_number = :dayNumber LIMIT 1")
    fun observeByDay(dayNumber: Int): Flow<UserProgress?>

    @Query("SELECT * FROM user_progress ORDER BY day_number ASC")
    fun observeAll(): Flow<List<UserProgress>>

    // ── Read (suspend) ────────────────────────────────────────────────────────

    @Query("SELECT * FROM user_progress WHERE day_number = :dayNumber LIMIT 1")
    suspend fun getByDay(dayNumber: Int): UserProgress?

    // ── Review Ward queries ───────────────────────────────────────────────────

    /**
     * Recent review ward — days memorized within the last 7 days
     * whose recent review is not yet complete.
     *
     * @param epochDayMin  (today's epochDay) - 7
     * @param epochDayMax  today's epochDay
     */
    @Query("""
        SELECT * FROM user_progress
        WHERE is_memorization_complete = 1
          AND memorization_date BETWEEN :epochDayMin AND :epochDayMax
          AND recent_review_complete = 0
        ORDER BY day_number ASC
    """)
    fun getDaysForRecentReview(epochDayMin: Long, epochDayMax: Long): Flow<List<UserProgress>>

    /**
     * Distant review ward — memorized days whose next scheduled review
     * is today or overdue.
     *
     * @param currentDayNumber  today's position in the 365-day plan
     */
    @Query("""
        SELECT * FROM user_progress
        WHERE is_memorization_complete = 1
          AND next_review_day_number <= :currentDayNumber
          AND distant_review_complete = 0
        ORDER BY next_review_day_number ASC
    """)
    fun getDaysForDistantReview(currentDayNumber: Int): Flow<List<UserProgress>>

    // ── Statistics ────────────────────────────────────────────────────────────

    @Query("SELECT COUNT(*) FROM user_progress WHERE is_memorization_complete = 1")
    fun observeMemorizedDaysCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(mistake_count), 0) FROM user_progress")
    fun observeTotalMistakes(): Flow<Int>

    @Query("SELECT COALESCE(AVG(recitation_score), 0.0) FROM user_progress WHERE recitation_score > 0")
    fun observeAverageScore(): Flow<Float>
}
