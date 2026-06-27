package com.quranmemorization.domain.model

/**
 * DailyTask — the complete picture of what a user must do on a given day.
 *
 * This is the primary model that the DashboardViewModel exposes to the UI.
 * It is assembled by the repository from QuranSchedule + UserProgress data.
 */
data class DailyTask(
    /** Day number in the 365-day plan (1–365). */
    val dayNumber: Int,

    /** Human-readable memorization portion label in Arabic. */
    val portionTitle: String,

    /** Mushaf page range to memorize today. */
    val pageStart: Int,
    val pageEnd:   Int,

    /** Juz' this portion belongs to. */
    val juzNumber: Int,

    /** Whether the user has ticked today's memorization as done. */
    val isMemorizationComplete: Boolean,

    /** Recent-ward review: days memorized in last 7 days, reviewed today. */
    val recentReviewItems:         List<ReviewItem>,
    val isRecentReviewComplete:    Boolean,

    /** Distant-ward review: older memorized portions scheduled for today. */
    val distantReviewItems:        List<ReviewItem>,
    val isDistantReviewComplete:   Boolean,

    /** 0–100 self-assessed recitation quality for this day's portion. */
    val recitationScore: Int,
)

/**
 * ReviewItem — a single memorized portion to be reviewed.
 */
data class ReviewItem(
    val dayNumber:    Int,
    val portionTitle: String,
    val pageStart:    Int,
    val pageEnd:      Int,
    /** How many mistakes were logged for this portion historically. */
    val mistakeCount: Int,
)

/**
 * OverallProgress — aggregated statistics for the progress bar and summary cards.
 */
data class OverallProgress(
    /** Days fully memorized (0–365). */
    val memorizedDays: Int,

    /** Total days in the plan (always 365). */
    val totalDays: Int = 365,

    /** Percentage complete (0.0–1.0). */
    val fractionComplete: Float = memorizedDays / totalDays.toFloat(),

    /** Estimated pages memorized (memorizedDays × avg pages/day ≈ 1.655). */
    val pagesMemorized: Int = (memorizedDays * 1.655f).toInt().coerceAtMost(604),

    /** Total Quran pages. */
    val totalPages: Int = 604,

    /** Cumulative mistakes across all sessions. */
    val totalMistakes: Int,

    /** Average self-assessed recitation score across all completed days. */
    val averageScore: Float,
)

/**
 * RecitationSession — mutable state for the interactive recitation screen.
 *
 * Words start hidden; the user taps to reveal and can mark each wrong.
 */
data class RecitationSession(
    val dayNumber:   Int,
    val portionTitle: String,
    val words:       List<RecitationWord>,
    val sessionType: SessionType,
) {
    enum class SessionType { MEMORIZATION, RECENT_REVIEW, DISTANT_REVIEW }
}

data class RecitationWord(
    val index:      Int,
    val arabicText: String,
    var isRevealed: Boolean   = false,
    var isMistake:  Boolean   = false,
)
