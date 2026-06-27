package com.quranmemorization.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * UserProgress — one row per day; records what the user has completed.
 *
 * Review intervals follow a simplified spaced-repetition model:
 *  • Day 1–7  after memorization → "recent review"  (Daily Ward)
 *  • Day 8+   after memorization → "distant review" at intervals defined by
 *             [nextReviewDayNumber] which the repository recalculates after
 *             each successful review using a multiplier sequence: 7, 14, 30, 60, 90 …
 *
 * @param dayNumber              FK → QuranSchedule.dayNumber
 * @param isMemorizationComplete User marked today's new portion as memorized
 * @param memorizationDate       Epoch-day (LocalDate.toEpochDay()) when memorized
 * @param recentReviewComplete   Recent-review ward done today
 * @param distantReviewComplete  Distant-review ward done today
 * @param nextReviewDayNumber    Next day in the schedule this portion must be reviewed
 * @param reviewIntervalIndex    Index into the review-interval sequence (0 = 7 days, 1 = 14 …)
 * @param mistakeCount           Total mistake events logged for this day's portion
 * @param recitationScore        0–100 self-assessed recitation quality score
 */
@Entity(
    tableName = "user_progress",
    foreignKeys = [
        ForeignKey(
            entity        = QuranSchedule::class,
            parentColumns = ["day_number"],
            childColumns  = ["day_number"],
            onDelete      = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("day_number")]
)
data class UserProgress(
    @PrimaryKey
    @ColumnInfo(name = "day_number")
    val dayNumber: Int,

    @ColumnInfo(name = "is_memorization_complete")
    val isMemorizationComplete: Boolean = false,

    @ColumnInfo(name = "memorization_date")
    val memorizationDate: Long? = null,           // LocalDate.toEpochDay()

    @ColumnInfo(name = "recent_review_complete")
    val recentReviewComplete: Boolean = false,

    @ColumnInfo(name = "distant_review_complete")
    val distantReviewComplete: Boolean = false,

    @ColumnInfo(name = "next_review_day_number")
    val nextReviewDayNumber: Int? = null,

    @ColumnInfo(name = "review_interval_index")
    val reviewIntervalIndex: Int = 0,

    @ColumnInfo(name = "mistake_count")
    val mistakeCount: Int = 0,

    @ColumnInfo(name = "recitation_score")
    val recitationScore: Int = 0,
)
