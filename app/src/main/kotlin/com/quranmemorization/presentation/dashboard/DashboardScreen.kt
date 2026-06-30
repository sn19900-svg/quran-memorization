package com.quranmemorization.presentation.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quranmemorization.presentation.components.MemorizationTaskCard
import com.quranmemorization.presentation.components.ProgressHeader
import com.quranmemorization.presentation.components.ReviewWardCard
import com.quranmemorization.presentation.recitation.VoiceRecitationScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState       by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState    by viewModel.recitationSheet.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val bottomSheet    = rememberModalBottomSheetState()

    Scaffold(
        modifier        = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar          = {
            TopAppBar(
                title  = {
                    Text(
                        text      = "مُذكِّري  •  حفظ القرآن",
                        style     = MaterialTheme.typography.titleLarge.copy(
                            fontWeight    = FontWeight.Bold,
                            textDirection = TextDirection.Rtl,
                        ),
                        modifier  = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                    )
                },
                colors        = TopAppBarDefaults.topAppBarColors(
                    containerColor       = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            if (uiState is DashboardUiState.Success) {
                ExtendedFloatingActionButton(
                    onClick            = { viewModel.onStartRecitation("MEMORIZATION") },
                    icon               = { Icon(Icons.Default.Mic, contentDescription = null) },
                    text               = { Text("تسميع") },
                    containerColor     = MaterialTheme.colorScheme.primary,
                    contentColor       = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->

        AnimatedContent(
            targetState   = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label         = "dashboard_state",
            modifier      = Modifier.padding(innerPadding),
        ) { state ->
            when (state) {
                is DashboardUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(56.dp),
                                color    = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text  = "جارٍ تحميل الجدول…",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            )
                        }
                    }
                }
                is DashboardUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp),
                        ) {
                            Icon(
                                imageVector        = Icons.Default.AutoStories,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.error,
                                modifier           = Modifier.size(64.dp),
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text      = state.message,
                                style     = MaterialTheme.typography.bodyLarge.copy(
                                    color         = MaterialTheme.colorScheme.error,
                                    textDirection = TextDirection.Rtl,
                                    textAlign     = TextAlign.Center,
                                ),
                                modifier  = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
                is DashboardUiState.Success -> {
                    DashboardContent(state = state, viewModel = viewModel)
                }
            }
        }
    }

    if (sheetState.isVisible) {
        ModalBottomSheet(
            onDismissRequest  = { viewModel.onDismissRecitationSheet() },
            sheetState        = bottomSheet,
            containerColor    = MaterialTheme.colorScheme.surface,
        ) {
            VoiceRecitationScreen(
                dayNumber   = sheetState.dayNumber,
                sessionType = sheetState.sessionType,
                onDismiss   = { viewModel.onDismissRecitationSheet() },
            )
        }
    }
}

@Composable
private fun DashboardContent(
    state:     DashboardUiState.Success,
    viewModel: DashboardViewModel,
) {
    val task     = state.dailyTask
    val progress = state.progress

    LazyColumn(
        modifier            = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "progress_header") {
            ProgressHeader(dayNumber = state.todayDayNumber, progress = progress)
        }

        item(key = "greeting") {
            Text(
                text  = motivationalArabicGreeting(state.todayDayNumber),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color         = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDirection = TextDirection.Rtl,
                    textAlign     = TextAlign.End,
                ),
                modifier  = Modifier.fillMaxWidth(),
            )
        }

        item(key = "memorization_card") {
            MemorizationTaskCard(
                task          = task,
                onMarkDone    = { viewModel.onMarkMemorizationComplete() },
                onStartRecite = { viewModel.onStartRecitation("MEMORIZATION") },
            )
        }

        item(key = "recent_review") {
            ReviewWardCard(
                title      = "المراجعة القريبة  (آخر ٧ أيام)",
                items      = task.recentReviewItems,
                isComplete = task.isRecentReviewComplete,
                onMarkDone = { viewModel.onMarkRecentReviewComplete() },
            )
        }

        item(key = "distant_review") {
            ReviewWardCard(
                title      = "المراجعة البعيدة",
                items      = task.distantReviewItems,
                isComplete = task.isDistantReviewComplete,
                onMarkDone = { viewModel.onMarkDistantReviewComplete() },
            )
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

private fun motivationalArabicGreeting(dayNumber: Int): String = when {
    dayNumber == 1   -> "مرحباً بك في أول يوم من رحلتك مع كتاب الله 🌟"
    dayNumber <= 7   -> "ما شاء الله، أسبوع كامل من المثابرة!"
    dayNumber <= 30  -> "الاستمرار هو سر النجاح — واصل مسيرتك 💪"
    dayNumber <= 100 -> "مئة يوم تقترب — أنت في المسار الصحيح"
    dayNumber <= 200 -> "تجاوزت نصف الرحلة — بارك الله في همّتك"
    dayNumber <= 300 -> "اقتربت من الغاية — ولا تضعف ولا تحزن"
    dayNumber < 365  -> "آخر أيام الرحلة — اللّهم بلّغنا الختم"
    else             -> "بارك الله فيك — أتممت حفظ القرآن الكريم 🎉"
}
