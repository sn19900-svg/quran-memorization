package com.quranmemorization.presentation.recitation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quranmemorization.domain.model.RecitationWord
import com.quranmemorization.presentation.theme.Crimson
import com.quranmemorization.presentation.theme.Gold600
import com.quranmemorization.presentation.theme.Teal900

/**
 * VoiceRecitationScreen — شاشة التسميع الصوتي الكاملة.
 *
 * تدفق الاستخدام:
 *  1. يضغط المستخدم زر الميكروفون ويبدأ بتلاوة السورة من حفظه.
 *  2. التطبيق يستمع ويطابق كل كلمة منطوقة مع كلمات الآية المتوقعة.
 *  3. الكلمات الصحيحة تُكشف تلقائياً (تتحول من ＿＿ إلى النص الفعلي).
 *  4. عند الانتهاء تظهر نتيجة % ويُحفظ التقدم.
 *  5. إن كان المستخدم قد حفظ هذه السورة من قبل، يمكنه الضغط على
 *     "أعرفها بالفعل — تجاوز" للانتقال مباشرة للسورة التالية.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceRecitationScreen(
    dayNumber:   Int,
    sessionType: String,
    onDismiss:   () -> Unit,
    viewModel:   RecitationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var hasMicPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasMicPermission = granted }

    LaunchedEffect(dayNumber) {
        viewModel.initSession(dayNumber, sessionType)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        // ── رأس الشاشة ────────────────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "إغلاق")
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text  = "التسميع الصوتي",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight    = FontWeight.Bold,
                        textDirection = TextDirection.Rtl,
                    ),
                )
                if (state.totalDays > 1) {
                    Text(
                        text  = "${state.currentDayIndex + 1} / ${state.totalDays}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // شريط تقدّم الجلسة (عدة سور متتالية)
        if (state.totalDays > 1) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress   = { (state.currentDayIndex + 1f) / state.totalDays },
                modifier   = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                color      = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(Modifier.height(16.dp))

        when {
            state.sessionCompleted -> SessionCompletedView(onDismiss)
            !hasMicPermission      -> PermissionRequestView {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            state.isFinished       -> ResultSummaryView(
                state    = state,
                onRepeat = { viewModel.repeatCurrentPortion() },
                onNext   = {
                    if (state.currentDayIndex + 1 >= state.totalDays) onDismiss()
                    else viewModel.moveToNext()
                },
            )
            else -> RecitationActiveView(
                state         = state,
                portionTitle  = state.portionTitle,
                pageStart     = state.pageStart,
                pageEnd       = state.pageEnd,
                onStartMic    = { viewModel.startListening() },
                onStopMic     = { viewModel.stopListening() },
                onSkip        = { viewModel.skipCurrentPortion() },
                onFinish      = { viewModel.finishCurrentPortion() },
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ── طلب إذن الميكروفون ────────────────────────────────────────────────────────

@Composable
private fun PermissionRequestView(onRequestPermission: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.fillMaxWidth().padding(vertical = 32.dp),
    ) {
        Icon(
            Icons.Default.Mic,
            contentDescription = null,
            tint     = Gold600,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text      = "نحتاج إذن الميكروفون للاستماع إلى تلاوتك",
            style     = MaterialTheme.typography.bodyLarge.copy(
                textDirection = TextDirection.Rtl,
                textAlign     = TextAlign.Center,
            ),
            modifier  = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRequestPermission) {
            Text("السماح باستخدام الميكروفون")
        }
    }
}

// ── الجلسة النشطة — الاستماع والكشف ───────────────────────────────────────────

@Composable
private fun RecitationActiveView(
    state:        RecitationUiState,
    portionTitle: String,
    pageStart:    Int,
    pageEnd:      Int,
    onStartMic:   () -> Unit,
    onStopMic:    () -> Unit,
    onSkip:       () -> Unit,
    onFinish:     () -> Unit,
) {
    Column {
        // عنوان السورة الحالية
        Text(
            text      = portionTitle,
            style     = MaterialTheme.typography.titleLarge.copy(
                fontWeight    = FontWeight.Bold,
                textDirection = TextDirection.Rtl,
                textAlign     = TextAlign.End,
            ),
            modifier  = Modifier.fillMaxWidth(),
        )
        Text(
            text      = "ص $pageStart–$pageEnd",
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier  = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
        )

        Spacer(Modifier.height(8.dp))

        // ── زر تجاوز السورة (حفظتها من قبل) ────────────────────────────────────
        OutlinedButton(
            onClick  = onSkip,
            modifier = Modifier.fillMaxWidth(),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = Gold600),
        ) {
            Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text("أحفظ هذه السورة بالفعل — تجاوز ✓")
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text      = "اضغط على الميكروفون وابدأ التلاوة من حفظك",
            style     = MaterialTheme.typography.bodySmall.copy(
                color         = MaterialTheme.colorScheme.onSurfaceVariant,
                textDirection = TextDirection.Rtl,
                textAlign     = TextAlign.End,
            ),
            modifier  = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        // ── كلمات الآية (تُكشف تلقائياً عند نطقها بشكل صحيح) ───────────────────
        FlowRow(
            modifier               = Modifier.fillMaxWidth(),
            horizontalArrangement  = Arrangement.End,
            verticalArrangement    = Arrangement.spacedBy(8.dp),
        ) {
            state.words.forEach { word ->
                WordChip(word)
                Spacer(Modifier.size(4.dp))
            }
        }

        Spacer(Modifier.height(8.dp))

        // عرض ما يتم التقاطه صوتياً (نص حي)
        if (state.spokenText.isNotBlank()) {
            Text(
                text      = "سُمِع: ${state.spokenText}",
                style     = MaterialTheme.typography.labelSmall.copy(
                    color         = MaterialTheme.colorScheme.primary,
                    textDirection = TextDirection.Rtl,
                ),
                modifier  = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )
        }

        // رسالة خطأ إن وجدت
        state.errorMessage?.let { error ->
            Spacer(Modifier.height(8.dp))
            Text(
                text      = error,
                style     = MaterialTheme.typography.labelSmall.copy(
                    color         = MaterialTheme.colorScheme.error,
                    textDirection = TextDirection.Rtl,
                ),
                modifier  = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── زر الميكروفون المتحرك ────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            MicButton(
                isListening = state.isListening,
                onClick     = { if (state.isListening) onStopMic() else onStartMic() },
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text      = if (state.isListening) "🎙️ جارٍ الاستماع..." else "اضغط للبدء",
            style     = MaterialTheme.typography.bodyMedium,
            modifier  = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color     = if (state.isListening) Teal900 else MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(20.dp))

        // إحصائيات سريعة
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatChip("صحيحة", state.matchedCount.toString(), Teal900)
            StatChip("متبقية", (state.words.size - state.matchedCount).toString(), Gold600)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick  = onFinish,
            modifier = Modifier.fillMaxWidth(),
            enabled  = state.matchedCount > 0,
            colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Text("إنهاء وحفظ النتيجة")
        }
    }
}

// ── زر الميكروفون مع تأثير نبضة ───────────────────────────────────────────────

@Composable
private fun MicButton(isListening: Boolean, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = if (isListening) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(600),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scale",
    )

    Box(
        modifier = Modifier
            .size(88.dp)
            .background(
                color = if (isListening) Crimson else Teal900,
                shape = CircleShape,
            )
            .clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(88.dp)) {
            Icon(
                Icons.Default.Mic,
                contentDescription = "ميكروفون",
                tint     = Color.White,
                modifier = Modifier.size((36 * scale).dp),
            )
        }
    }
}

// ── شريحة كلمة واحدة ──────────────────────────────────────────────────────────

@Composable
private fun WordChip(word: RecitationWord) {
    val bgColor = if (word.isRevealed) Teal900.copy(alpha = 0.1f)
                  else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (word.isRevealed) MaterialTheme.colorScheme.onSurface
                    else Color.Transparent

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(
            text  = if (word.isRevealed) word.arabicText else "＿＿",
            style = MaterialTheme.typography.bodyLarge.copy(
                color         = textColor,
                fontWeight    = FontWeight.SemiBold,
                textDirection = TextDirection.Rtl,
                fontSize      = 19.sp,
            ),
        )
    }
}

// ── شريحة إحصائية ─────────────────────────────────────────────────────────────

@Composable
private fun StatChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium.copy(
            color = color, fontWeight = FontWeight.Bold))
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── ملخص النتيجة ──────────────────────────────────────────────────────────────

@Composable
private fun ResultSummaryView(
    state:    RecitationUiState,
    onRepeat: () -> Unit,
    onNext:   () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.fillMaxWidth().padding(vertical = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = when {
                        state.score >= 90 -> Teal900.copy(alpha = 0.1f)
                        state.score >= 70 -> Gold600.copy(alpha = 0.1f)
                        else               -> Crimson.copy(alpha = 0.1f)
                    },
                    shape = CircleShape,
                )
                .padding(32.dp),
        ) {
            Text(
                text  = "${state.score}%",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = when {
                        state.score >= 90 -> Teal900
                        state.score >= 70 -> Gold600
                        else               -> Crimson
                    },
                    fontWeight = FontWeight.Bold,
                ),
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text      = "${state.matchedCount} من ${state.words.size} كلمة صحيحة",
            style     = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Rtl),
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextButton(onClick = onRepeat, modifier = Modifier.weight(1f)) {
                Text("إعادة المحاولة")
            }
            Button(
                onClick  = onNext,
                modifier = Modifier.weight(1f),
                colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text(if (state.currentDayIndex + 1 >= state.totalDays) "إنهاء" else "السورة التالية")
            }
        }
    }
}

// ── اكتملت كل الجلسة ───────────────────────────────────────────────────────────

@Composable
private fun SessionCompletedView(onDismiss: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.fillMaxWidth().padding(vertical = 48.dp),
    ) {
        Text("🎉", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            text      = "أحسنت! أكملت جميع السور المقررة",
            style     = MaterialTheme.typography.titleMedium.copy(
                fontWeight    = FontWeight.Bold,
                textDirection = TextDirection.Rtl,
            ),
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onDismiss) { Text("العودة للرئيسية") }
    }
}
