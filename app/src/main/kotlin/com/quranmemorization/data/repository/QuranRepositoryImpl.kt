package com.quranmemorization.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.quranmemorization.data.local.ScheduleSeeder
import com.quranmemorization.data.local.dao.MistakeLogDao
import com.quranmemorization.data.local.dao.QuranScheduleDao
import com.quranmemorization.data.local.dao.UserProgressDao
import com.quranmemorization.data.local.entity.MistakeLog
import com.quranmemorization.data.local.entity.UserProgress
import com.quranmemorization.domain.model.DailyTask
import com.quranmemorization.domain.model.OverallProgress
import com.quranmemorization.domain.model.ReviewItem
import com.quranmemorization.domain.repository.QuranRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * QuranRepositoryImpl — implements [QuranRepository] on top of Room + DataStore.
 *
 * Spaced-repetition interval sequence (days after memorization):
 *   index 0 →  7 days
 *   index 1 → 14 days
 *   index 2 → 30 days
 *   index 3 → 60 days
 *   index 4 → 90 days  (repeats thereafter)
 */
@Singleton
class QuranRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scheduleDao: QuranScheduleDao,
    private val progressDao: UserProgressDao,
    private val mistakeDao:  MistakeLogDao,
    private val dataStore:   DataStore<Preferences>,
    private val seeder:      ScheduleSeeder,
) : QuranRepository {

    // ── Constants ─────────────────────────────────────────────────────────────

    /** Spaced-repetition interval sequence (in schedule-day units ≈ calendar days). */
    private val REVIEW_INTERVALS = intArrayOf(7, 14, 30, 60, 90)

    private object PrefKeys {
        /** The calendar date (epoch day) when the user started the app / day 1. */
        val START_EPOCH_DAY = longPreferencesKey("start_epoch_day")
        /** Cached today's day-number (1–365) to avoid repeated date math. */
        val TODAY_DAY_NUMBER = intPreferencesKey("today_day_number")
    }

    // ── Initialization ────────────────────────────────────────────────────────

    override suspend fun initializeIfNeeded(): Int {
        // Seed the 365-row schedule on first launch (no-op if already seeded)
        seeder.seedIfEmpty()

        val prefs = dataStore.data.first()
        val todayEpoch = LocalDate.now().toEpochDay()

        val startEpoch = prefs[PrefKeys.START_EPOCH_DAY] ?: run {
            // First launch — record start date
            dataStore.edit { it[PrefKeys.START_EPOCH_DAY] = todayEpoch }
            todayEpoch
        }

        val dayNumber = ((todayEpoch - startEpoch) + 1).toInt().coerceIn(1, 365)

        dataStore.edit { it[PrefKeys.TODAY_DAY_NUMBER] = dayNumber }
        return dayNumber
    }

    // ── Daily task ────────────────────────────────────────────────────────────

    override fun observeTodayTask(dayNumber: Int): Flow<DailyTask?> {
        val todayEpoch   = LocalDate.now().toEpochDay()
        val recentMinDay = todayEpoch - 7

        return combine(
            scheduleDao.observeByDay(dayNumber),
            progressDao.observeByDay(dayNumber),
            progressDao.getDaysForRecentReview(recentMinDay, todayEpoch),
            progressDao.getDaysForDistantReview(dayNumber),
        ) { schedule, progress, recentRows, distantRows ->
            schedule ?: return@combine null

            // Ensure a progress row exists for today
            if (progress == null) {
                progressDao.insert(UserProgress(dayNumber = dayNumber))
            }

            val recentItems  = recentRows.map  { it.toReviewItem()  }
            val distantItems = distantRows.map { it.toReviewItem() }

            DailyTask(
                dayNumber              = dayNumber,
                portionTitle           = schedule.arabicTitle,
                pageStart              = schedule.pageStart,
                pageEnd                = schedule.pageEnd,
                juzNumber              = schedule.juzNumber,
                isMemorizationComplete = progress?.isMemorizationComplete ?: false,
                recentReviewItems      = recentItems,
                isRecentReviewComplete = progress?.recentReviewComplete ?: false,
                distantReviewItems     = distantItems,
                isDistantReviewComplete= progress?.distantReviewComplete ?: false,
                recitationScore        = progress?.recitationScore ?: 0,
            )
        }
    }

    // ── Completion actions ────────────────────────────────────────────────────

    override suspend fun markMemorizationComplete(dayNumber: Int, score: Int) {
        val existing = progressDao.getByDay(dayNumber) ?: UserProgress(dayNumber)
        val nextInterval = REVIEW_INTERVALS[existing.reviewIntervalIndex.coerceAtMost(REVIEW_INTERVALS.size - 1)]
        progressDao.upsert(
            existing.copy(
                isMemorizationComplete = true,
                memorizationDate       = LocalDate.now().toEpochDay(),
                nextReviewDayNumber    = dayNumber + nextInterval,
                recitationScore        = score,
            )
        )
    }

    override suspend fun markRecentReviewComplete(dayNumber: Int) {
        val existing = progressDao.getByDay(dayNumber) ?: return
        progressDao.update(existing.copy(recentReviewComplete = true))
    }

    override suspend fun markDistantReviewComplete(dayNumber: Int) {
        val existing = progressDao.getByDay(dayNumber) ?: return
        val nextIndex    = (existing.reviewIntervalIndex + 1).coerceAtMost(REVIEW_INTERVALS.size - 1)
        val nextInterval = REVIEW_INTERVALS[nextIndex]
        progressDao.update(
            existing.copy(
                distantReviewComplete  = true,
                reviewIntervalIndex    = nextIndex,
                nextReviewDayNumber    = dayNumber + nextInterval,
            )
        )
    }

    // ── Mistake logging ───────────────────────────────────────────────────────

    override suspend fun logMistake(mistake: MistakeLog) {
        mistakeDao.insert(mistake)
        // Increment mistake counter on the progress row
        val existing = progressDao.getByDay(mistake.dayNumber) ?: return
        progressDao.update(existing.copy(mistakeCount = existing.mistakeCount + 1))
    }

    override fun observeMistakesForDay(dayNumber: Int): Flow<List<MistakeLog>> =
        mistakeDao.observeByDay(dayNumber)

    // ── Overall progress ──────────────────────────────────────────────────────

    override fun observeOverallProgress(): Flow<OverallProgress> =
        combine(
            progressDao.observeMemorizedDaysCount(),
            progressDao.observeTotalMistakes(),
            progressDao.observeAverageScore(),
        ) { days, mistakes, avgScore ->
            OverallProgress(
                memorizedDays  = days,
                totalMistakes  = mistakes,
                averageScore   = avgScore,
            )
        }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Convert a UserProgress DB row into a lightweight ReviewItem for the UI. */
    private suspend fun UserProgress.toReviewItem(): ReviewItem {
        val schedule = scheduleDao.getByDay(dayNumber)
        return ReviewItem(
            dayNumber    = dayNumber,
            portionTitle = schedule?.arabicTitle ?: "اليوم $dayNumber",
            pageStart    = schedule?.pageStart   ?: 0,
            pageEnd      = schedule?.pageEnd     ?: 0,
            mistakeCount = mistakeCount,
        )
    }
}
