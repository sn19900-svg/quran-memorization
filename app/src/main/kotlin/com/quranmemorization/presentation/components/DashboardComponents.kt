package com.quranmemorization.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.quranmemorization.domain.model.DailyTask
import com.quranmemorization.domain.model.OverallProgress
import com.quranmemorization.domain.model.ReviewItem
import com.quranmemorization.presentation.theme.Gold600
import com.quranmemorization.presentation.theme.Teal900

// ── Progress Header ───────────────────────────────────────────────────────────

/**
 * ProgressHeader — shows the overall Quran completion progress bar,
 * day counter, and page count.  Animates on first composition.
 */
@Composable
fun ProgressHeader(
    dayNumber: Int,
    progress:  OverallProgress,
    modifier:  Modifier = Modifier,
) {
    val animatedFraction by animateFloatAsState(
        targetValue  = progress.fractionComplete,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label        = "progress_animation",
    )

    Column(modifier = modifier.fillMaxWidth()) {

        // ── Percentage + day label ────────────────────────────────────────────
        Row(
            modifier      = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text  = "${(animatedFraction * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color      = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    text  = "من القرآن الكريم",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color         = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDirection = TextDirection.Rtl,
                    ),
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text  = "اليوم $dayNumber / 365",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color         = MaterialTheme.colorScheme.secondary,
                        textDirection = TextDirection.Rtl,
                    ),
                )
                Text(
                    text  = "${progress.pagesMemorized} / ${progress.totalPages} صفحة",
                    style = MaterialTheme.typography.bodySmall().copy(
                        color         = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDirection = TextDirection.Rtl,
                    ),
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Progress bar ──────────────────────────────────────────────────────
        LinearProgressIndicator(
            progress       = { animatedFraction },
            modifier       = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50)),
            color          = MaterialTheme.colorScheme.primary,
            trackColor     = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap      = StrokeCap.Round,
        )
    }
}

// ── Today's Memorization Card ─────────────────────────────────────────────────

/**
 * MemorizationTaskCard — shows the day's new memorization portion.
 *
 * Turns green and shows a checkmark when [task.isMemorizationComplete] is true.
 * The "Start" button launches the interactive recitation session.
 */
@Composable
fun MemorizationTaskCard(
    task:          DailyTask,
    onMarkDone:    () -> Unit,
    onStartRecite: () -> Unit,
    modifier:      Modifier = Modifier,
) {
    val isComplete  = task.isMemorizationComplete
    val cardColor by animateColorAsState(
        targetValue  = if (isComplete) MaterialTheme.colorScheme.primaryContainer
                       else            MaterialTheme.colorScheme.surface,
        animationSpec = tween(400),
        label        = "card_color",
    )

    SectionCard(
        modifier  = modifier,
        color     = cardColor,
        icon      = Icons.Default.MenuBook,
        iconTint  = if (isComplete) MaterialTheme.colorScheme.primary else Gold600,
        title     = "حفظ اليوم",
        subtitle  = "جزء ${task.juzNumber}  •  ص ${task.pageStart}–${task.pageEnd}",
    ) {
        // Arabic title
        Text(
            text      = task.portionTitle,
            style     = MaterialTheme.typography.titleMedium.copy(
                textDirection = TextDirection.Rtl,
                textAlign     = TextAlign.End,
                fontWeight    = FontWeight.SemiBold,
            ),
            modifier  = Modifier.fillMaxWidth(),
            color     = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // "Recite" launches the interactive test
            OutlinedButton(
                onClick  = onStartRecite,
                modifier = Modifier.weight(1f),
                enabled  = !isComplete,
            ) {
                Text("بدء التسميع")
            }

            // "Done" marks complete without the recitation flow
            Button(
                onClick  = onMarkDone,
                modifier = Modifier.weight(1f),
                enabled  = !isComplete,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                )
            ) {
                if (isComplete) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null,
                         modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("تم الحفظ")
                } else {
                    Text("✓ تمّ الحفظ")
                }
            }
        }
    }
}

// ── Review Card ───────────────────────────────────────────────────────────────

/**
 * ReviewWardCard — displays either the recent or distant review ward.
 *
 * Shows each [ReviewItem] with its page range and a mistake-count badge.
 */
@Composable
fun ReviewWardCard(
    title:       String,
    items:       List<ReviewItem>,
    isComplete:  Boolean,
    onMarkDone:  () -> Unit,
    modifier:    Modifier = Modifier,
) {
    val cardColor by animateColorAsState(
        targetValue  = if (isComplete) MaterialTheme.colorScheme.primaryContainer
                       else            MaterialTheme.colorScheme.surface,
        animationSpec = tween(400),
        label        = "review_card_color",
    )

    SectionCard(
        modifier  = modifier,
        color     = cardColor,
        icon      = Icons.Default.Refresh,
        iconTint  = if (isComplete) MaterialTheme.colorScheme.primary
                    else            MaterialTheme.colorScheme.secondary,
        title     = title,
        subtitle  = if (items.isEmpty()) "لا يوجد مراجعة اليوم"
                    else "${items.size} قطعة للمراجعة",
    ) {
        if (items.isEmpty()) {
            Text(
                text  = "أحسنت! لا توجد مراجعة مقررة الآن.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color         = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDirection = TextDirection.Rtl,
                    textAlign     = TextAlign.End,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.take(3).forEach { item ->
                    ReviewItemRow(item)
                }
                if (items.size > 3) {
                    Text(
                        text  = "... و${items.size - 3} قطع أخرى",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDirection = TextDirection.Rtl,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick  = onMarkDone,
                modifier = Modifier.fillMaxWidth(),
                enabled  = !isComplete && items.isNotEmpty(),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor   = MaterialTheme.colorScheme.onSecondary,
                )
            ) {
                Text(if (isComplete) "✓ تمّت المراجعة" else "تمّت المراجعة")
            }
        }
    }
}

@Composable
private fun ReviewItemRow(item: ReviewItem) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Mistake badge
        if (item.mistakeCount > 0) {
            Box(
                modifier          = Modifier
                    .size(28.dp)
                    .background(
                        color  = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                        shape  = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = item.mistakeCount.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color      = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        } else {
            Icon(
                imageVector      = Icons.Default.Star,
                contentDescription = null,
                tint             = Gold600,
                modifier         = Modifier.size(20.dp),
            )
        }

        Spacer(Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
            Text(
                text      = item.portionTitle,
                style     = MaterialTheme.typography.bodyMedium.copy(
                    textDirection = TextDirection.Rtl,
                    fontWeight    = FontWeight.Medium,
                ),
                textAlign = TextAlign.End,
            )
            Text(
                text  = "ص ${item.pageStart}–${item.pageEnd}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color         = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDirection = TextDirection.Rtl,
                ),
                textAlign = TextAlign.End,
            )
        }
    }
}

// ── Generic Section Card ──────────────────────────────────────────────────────

@Composable
fun SectionCard(
    title:    String,
    subtitle: String,
    icon:     ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    color:    Color    = MaterialTheme.colorScheme.surface,
    content:  @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Card header
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth(),
            ) {
                // Icon badge
                Box(
                    modifier          = Modifier
                        .size(44.dp)
                        .background(
                            color  = iconTint.copy(alpha = 0.12f),
                            shape  = RoundedCornerShape(12.dp),
                        ),
                    contentAlignment  = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint,
                         modifier = Modifier.size(24.dp))
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text      = title,
                        style     = MaterialTheme.typography.titleMedium.copy(
                            fontWeight    = FontWeight.Bold,
                            textDirection = TextDirection.Rtl,
                        ),
                        textAlign = TextAlign.End,
                    )
                    Text(
                        text  = subtitle,
                        style = MaterialTheme.typography.bodySmall().copy(
                            color         = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDirection = TextDirection.Rtl,
                        ),
                        textAlign = TextAlign.End,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

// ── Extension helpers ─────────────────────────────────────────────────────────

/** Convenience alias — Material3 Typography doesn't have bodySmall as a named slot in all BOM versions. */
@Composable
fun androidx.compose.material3.Typography.bodySmall() = this.labelSmall
