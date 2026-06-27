package com.quranmemorization.domain.repository

import com.quranmemorization.data.local.entity.MistakeLog
import com.quranmemorization.domain.model.DailyTask
import com.quranmemorization.domain.model.OverallProgress
import kotlinx.coroutines.flow.Flow

/**
 * QuranRepository — the domain-layer contract.
 *
 * The use-cases and ViewModels depend only on this interface, never on Room
 * classes directly.  This makes the business logic trivially unit-testable
 * with a fake/mock implementation.
 */
interface QuranRepository {

    // ── Daily task ────────────────────────────────────────────────────────────

    /** Reactive stream of today's complete task (memorization + reviews). */
    fun observeTodayTask(dayNumber: Int): Flow<DailyTask?>

    // ── Completion actions ────────────────────────────────────────────────────

    /** Mark the memorization portion for [dayNumber] as complete. */
    suspend fun markMemorizationComplete(dayNumber: Int, score: Int)

    /** Mark the recent review ward as complete for today. */
    suspend fun markRecentReviewComplete(dayNumber: Int)

    /** Mark the distant review ward as complete and advance the interval. */
    suspend fun markDistantReviewComplete(dayNumber: Int)

    // ── Mistake logging ───────────────────────────────────────────────────────

    suspend fun logMistake(mistake: MistakeLog)

    fun observeMistakesForDay(dayNumber: Int): Flow<List<MistakeLog>>

    // ── Overall progress ──────────────────────────────────────────────────────

    fun observeOverallProgress(): Flow<OverallProgress>

    // ── App start ─────────────────────────────────────────────────────────────

    /** Seed the schedule if first launch; resolve today's day number. */
    suspend fun initializeIfNeeded(): Int   // returns today's dayNumber (1–365)
}
