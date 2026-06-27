package com.quranmemorization.presentation.recitation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quranmemorization.domain.model.RecitationWord
import com.quranmemorization.presentation.theme.Crimson
import com.quranmemorization.presentation.theme.Gold600
import com.quranmemorization.presentation.theme.Teal900

/**
 * RecitationScreen — interactive tap-to-reveal self-testing flow.
 *
 * UX flow:
 *  1. Words appear as blank tiles — the user recites from memory.
 *  2. Tap a tile → word is revealed.
 *  3. Long-press (or second tap when revealed) → toggles "mistake" (red highlight).
 *  4. "Finish" → summary shows score and lets the user confirm or re-do.
 *
 * In a full production build this screen integrates Android's
 * SpeechRecognizer API for voice verification.  Here we provide the
 * manual tap-to-reveal flow which works offline without permissions.
 *
 * The word list below is a sample Fatiha — in production this is loaded
 * from the bundled Quran text DB via a dedicated RecitationViewModel.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecitationScreen(
    dayNumber:   Int,
    sessionType: String,
    onDismiss:   () -> Unit,
) {
    // ── Sample words — replace with DB-loaded verse words in production ───────
    val sampleWords = remember {
        listOf(
            "بِسْمِ", "اللَّهِ", "الرَّحْمَٰنِ", "الرَّحِيمِ",
            "الْحَمْدُ", "لِلَّهِ", "رَبِّ", "الْعَالَمِينَ",
            "الرَّحْمَٰنِ", "الرَّحِيمِ",
            "مَالِكِ", "يَوْمِ", "الدِّينِ",
            "إِيَّاكَ", "نَعْبُدُ", "وَإِيَّاكَ", "نَسْتَعِينُ",
            "اهْدِنَا", "الصِّرَاطَ", "الْمُسْتَقِيمَ",
            "صِرَاطَ", "الَّذِينَ", "أَنْعَمْتَ", "عَلَيْهِمْ",
            "غَيْرِ", "الْمَغْضُوبِ", "عَلَيْهِمْ", "وَلَا", "الضَّالِّينَ",
        ).mapIndexed { index, text ->
            RecitationWord(index = index, arabicText = text)
        }
    }

    val words = remember { mutableStateListOf(*sampleWords.toTypedArray()) }
    var isFinished by remember { mutableStateOf(false) }

    val revealedCount  = words.count { it.isRevealed }
    val mistakeCount   = words.count { it.isMistake }
    val score = if (words.isEmpty()) 0
                else ((1f - mistakeCount.toFloat() / words.size) * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            modifier      = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "إغلاق")
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text      = sessionTypeLabel(sessionType),
                    style     = MaterialTheme.typography.titleMedium.copy(
                        fontWeight    = FontWeight.Bold,
                        textDirection = TextDirection.Rtl,
                    ),
                    textAlign = TextAlign.End,
                )
                Text(
                    text  = "اليوم $dayNumber",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Instructions ──────────────────────────────────────────────────────
        if (!isFinished) {
            Text(
                text      = "اقرأ من حفظك، ثم اضغط على الكلمة للكشف عنها.\nاضغط مرة أخرى لتسجيل خطأ.",
                style     = MaterialTheme.typography.bodySmall.copy(
                    color         = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDirection = TextDirection.Rtl,
                    textAlign     = TextAlign.End,
                    lineHeight    = 20.sp,
                ),
                modifier  = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
        }

        // ── Word tiles (FlowRow — RTL) ─────────────────────────────────────
        if (!isFinished) {
            FlowRow(
                modifier             = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalArrangement  = Arrangement.spacedBy(8.dp),
            ) {
                words.forEach { word ->
                    WordTile(
                        word    = word,
                        onClick = {
                            val idx = words.indexOf(word)
                            if (idx < 0) return@WordTile
                            val current = words[idx]
                            words[idx] = when {
                                !current.isRevealed -> current.copy(isRevealed = true)
                                !current.isMistake  -> current.copy(isMistake = true)
                                else                -> current.copy(isMistake = false)
                            }
                        }
                    )
                    Spacer(Modifier.width(4.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // Reveal all / finish buttons
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(
                    onClick  = { words.indices.forEach { i -> words[i] = words[i].copy(isRevealed = true) } },
                    modifier = Modifier.weight(1f),
                ) { Text("كشف الكل") }

                Button(
                    onClick  = { isFinished = true },
                    modifier = Modifier.weight(1f),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    )
                ) { Text("إنهاء التسميع") }
            }
        }

        // ── Summary (after finishing) ─────────────────────────────────────────
        if (isFinished) {
            SessionSummary(
                totalWords   = words.size,
                mistakeCount = mistakeCount,
                score        = score,
                onRepeat     = {
                    words.indices.forEach { i ->
                        words[i] = words[i].copy(isRevealed = false, isMistake = false)
                    }
                    isFinished = false
                },
                onDone       = onDismiss,
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ── Word Tile ─────────────────────────────────────────────────────────────────

@Composable
private fun WordTile(word: RecitationWord, onClick: () -> Unit) {
    val bgColor = when {
        word.isMistake  -> Crimson.copy(alpha = 0.15f)
        word.isRevealed -> Teal900.copy(alpha = 0.08f)
        else            -> MaterialTheme.colorScheme.surfaceVariant
    }
    val borderColor = when {
        word.isMistake  -> Crimson
        word.isRevealed -> Teal900
        else            -> MaterialTheme.colorScheme.outline
    }
    val textColor = when {
        word.isMistake  -> Crimson
        word.isRevealed -> MaterialTheme.colorScheme.onSurface
        else            -> Color.Transparent   // hidden — shows blank tile
    }

    Box(
        modifier          = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment  = Alignment.Center,
    ) {
        Text(
            text  = if (word.isRevealed) word.arabicText else "＿＿",
            style = MaterialTheme.typography.bodyLarge.copy(
                color         = textColor,
                fontWeight    = FontWeight.SemiBold,
                textDirection = TextDirection.Rtl,
                fontSize      = 20.sp,
            ),
        )
    }
}

// ── Session Summary ───────────────────────────────────────────────────────────

@Composable
private fun SessionSummary(
    totalWords:   Int,
    mistakeCount: Int,
    score:        Int,
    onRepeat:     () -> Unit,
    onDone:       () -> Unit,
) {
    Column(
        horizontalAlignment   = Alignment.CenterHorizontally,
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
    ) {
        // Score circle
        Box(
            modifier          = Modifier
                .background(
                    color  = when {
                        score >= 90 -> Teal900.copy(alpha = 0.1f)
                        score >= 70 -> Gold600.copy(alpha = 0.1f)
                        else        -> Crimson.copy(alpha = 0.1f)
                    },
                    shape  = RoundedCornerShape(50),
                )
                .padding(32.dp),
            contentAlignment  = Alignment.Center,
        ) {
            Text(
                text  = "$score%",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color      = when {
                        score >= 90 -> Teal900
                        score >= 70 -> Gold600
                        else        -> Crimson
                    },
                    fontWeight = FontWeight.Bold,
                ),
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text      = scoreLabel(score),
            style     = MaterialTheme.typography.titleMedium.copy(
                fontWeight    = FontWeight.Bold,
                textDirection = TextDirection.Rtl,
            ),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text  = "الأخطاء: $mistakeCount من $totalWords كلمة",
            style = MaterialTheme.typography.bodyMedium.copy(
                color         = MaterialTheme.colorScheme.onSurfaceVariant,
                textDirection = TextDirection.Rtl,
            ),
        )

        Spacer(Modifier.height(24.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextButton(onClick = onRepeat, modifier = Modifier.weight(1f)) {
                Text("إعادة")
            }
            Button(
                onClick  = onDone,
                modifier = Modifier.weight(1f),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                )
            ) {
                Text("حفظ النتيجة")
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun sessionTypeLabel(type: String): String = when (type) {
    "RECENT_REVIEW"  -> "المراجعة القريبة"
    "DISTANT_REVIEW" -> "المراجعة البعيدة"
    else             -> "تسميع الحفظ الجديد"
}

private fun scoreLabel(score: Int): String = when {
    score >= 95 -> "ممتاز جداً — ما شاء الله! 🌟"
    score >= 85 -> "ممتاز — أحسنت"
    score >= 70 -> "جيد — واصل المراجعة"
    score >= 50 -> "مقبول — راجع الأخطاء جيداً"
    else        -> "بحاجة إلى مزيد من التكرار"
}
