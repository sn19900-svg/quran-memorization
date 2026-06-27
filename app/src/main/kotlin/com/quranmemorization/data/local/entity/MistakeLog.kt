package com.quranmemorization.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * MistakeLog — one row per mistake event during recitation or self-testing.
 *
 * This fine-grained log allows the Dashboard to surface "weak verses" that
 * need extra attention, driving a personalized review order.
 *
 * @param id             Auto-generated primary key
 * @param dayNumber      FK → QuranSchedule; which day's portion the mistake belongs to
 * @param surahNumber    Surah (1–114) where the mistake occurred
 * @param verseNumber    Verse within that Surah
 * @param mistakeType    Categorized mistake: OMISSION | SUBSTITUTION | TAJWEED | SEQUENCE
 * @param sessionType    Where mistake was caught: MEMORIZATION | RECENT_REVIEW | DISTANT_REVIEW
 * @param timestampMs    System.currentTimeMillis() when the mistake was recorded
 * @param notes          Optional free-text note from the user (e.g. "confused with next verse")
 */
@Entity(
    tableName = "mistake_log",
    foreignKeys = [
        ForeignKey(
            entity        = QuranSchedule::class,
            parentColumns = ["day_number"],
            childColumns  = ["day_number"],
            onDelete      = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("day_number"),
        Index("surah_number", "verse_number"),
    ]
)
data class MistakeLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "day_number")    val dayNumber:   Int,
    @ColumnInfo(name = "surah_number")  val surahNumber: Int,
    @ColumnInfo(name = "verse_number")  val verseNumber: Int,
    @ColumnInfo(name = "mistake_type")  val mistakeType: MistakeType,
    @ColumnInfo(name = "session_type")  val sessionType: SessionType,
    @ColumnInfo(name = "timestamp_ms")  val timestampMs: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "notes")         val notes:       String = "",
) {
    enum class MistakeType {
        /** Skipped a word or phrase entirely */
        OMISSION,
        /** Replaced a word with an incorrect one */
        SUBSTITUTION,
        /** Correct words, incorrect Tajweed rule */
        TAJWEED,
        /** Verses or words in the wrong order */
        SEQUENCE,
    }

    enum class SessionType {
        MEMORIZATION,
        RECENT_REVIEW,
        DISTANT_REVIEW,
    }
}
