package com.quranmemorization.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * QuranSchedule — one row per day of the 365-day memorization plan.
 *
 * The Quran has 604 pages.  Dividing evenly gives ~1.655 pages/day.
 * We distribute them so each day covers a clean verse boundary;
 * a seed utility (ScheduleSeeder) pre-populates this table on first launch.
 *
 * @param dayNumber        1–365
 * @param surahStart       Surah number where memorization begins (1–114)
 * @param verseStart       Verse number inside [surahStart]
 * @param surahEnd         Surah number where memorization ends
 * @param verseEnd         Verse number inside [surahEnd]
 * @param pageStart        Mushaf page number (1–604) — Medina print standard
 * @param pageEnd          Mushaf page number (inclusive)
 * @param juzNumber        Juz' (para) number this day falls in (1–30)
 * @param arabicTitle      Human-readable label, e.g. "الفاتحة – البقرة ١–٥"
 */
@Entity(tableName = "quran_schedule")
data class QuranSchedule(
    @PrimaryKey
    @ColumnInfo(name = "day_number")
    val dayNumber: Int,

    @ColumnInfo(name = "surah_start")   val surahStart:   Int,
    @ColumnInfo(name = "verse_start")   val verseStart:   Int,
    @ColumnInfo(name = "surah_end")     val surahEnd:     Int,
    @ColumnInfo(name = "verse_end")     val verseEnd:     Int,
    @ColumnInfo(name = "page_start")    val pageStart:    Int,
    @ColumnInfo(name = "page_end")      val pageEnd:      Int,
    @ColumnInfo(name = "juz_number")    val juzNumber:    Int,
    @ColumnInfo(name = "arabic_title")  val arabicTitle:  String,
)
