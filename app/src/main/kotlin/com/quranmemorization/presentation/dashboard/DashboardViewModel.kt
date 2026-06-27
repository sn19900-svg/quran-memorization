package com.quranmemorization.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranmemorization.domain.model.DailyTask
import com.quranmemorization.domain.model.OverallProgress
import com.quranmemorization.domain.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

/**
 * DashboardUiState — sealed hierarchy representing every possible screen state.
 *
 * The UI renders a different composable for each state, making impossible states
 * unrepresentable and eliminating null-pointer risk in the UI layer.
 */
sealed interface DashboardUiState {
    /** Shown while the DB seeds and the first day number is resolved. */
    data object Loading : DashboardUiState

    /** Normal operating state — all data is available. */
    data class Success(
        val todayDayNumber: Int,
        val dailyTask:      DailyTask,
        val progress:       OverallProgress,
    ) : DashboardUiState

    /** Shown if Room throws or the schedule isn't seeded. */
    data class Error(val message: String) : DashboardUiState
}

/**
 * RecitationSheetState — controls the bottom sheet that launches the
 * interactive recitation / self-testing flow.
 */
data class RecitationSheetState(
    val isVisible:   Boolean = false,
    val dayNumber:   Int     = 0,
    val sessionType: String  = "MEMORIZATION",   // matches RecitationSession.SessionType
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

/**
 * DashboardViewModel — the single source of truth for the Dashboard screen.
 *
 * Responsibilities:
 *  1. On creation: seed the DB (if first launch) and resolve today's day number.
 *  2. Expose [uiState] as a cold-start-friendly [StateFlow] the UI collects.
 *  3. Handle user intents (mark complete, open recitation sheet).
 *
 * All heavy work runs on Dispatchers.IO via the repository; the ViewModel
 * only bridges the reactive streams to the UI layer.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: QuranRepository,
) : ViewModel() {

    // Mutable source — holds the resolved day number after initialization
    private val _todayDayNumber = MutableStateFlow(0)

    // Recitation bottom-sheet state
    private val _recitationSheet = MutableStateFlow(RecitationSheetState())
    val recitationSheet: StateFlow<RecitationSheetState> = _recitationSheet.asStateFlow()

    // Loading / error flag shown before the day number is resolved
    private val _isLoading = MutableStateFlow(true)
    private val _error     = MutableStateFlow<String?>(null)

    /**
     * [uiState] — the primary stream the Dashboard composable observes.
     *
     * Uses [flatMapLatest] so that whenever today's day number is resolved (or
     * changes after midnight), the downstream task + progress flows re-subscribe
     * automatically.
     */
    val uiState: StateFlow<DashboardUiState> = _todayDayNumber
        .flatMapLatest { dayNumber ->
            if (dayNumber == 0) return@flatMapLatest flowOf(DashboardUiState.Loading)

            // Combine daily task + overall progress into a single UI state emission
            kotlinx.coroutines.flow.combine(
                repository.observeTodayTask(dayNumber),
                repository.observeOverallProgress(),
            ) { task, progress ->
                if (task == null) {
                    DashboardUiState.Error("تعذّر تحميل مهمة اليوم. أعد تشغيل التطبيق.")
                } else {
                    DashboardUiState.Success(
                        todayDayNumber = dayNumber,
                        dailyTask      = task,
                        progress       = progress,
                    )
                }
            }
        }
        .stateIn(
            scope            = viewModelScope,
            started          = SharingStarted.WhileSubscribed(5_000),
            initialValue     = DashboardUiState.Loading,
        )

    init {
        initialize()
    }

    // ── Initialization ────────────────────────────────────────────────────────

    private fun initialize() {
        viewModelScope.launch {
            try {
                val dayNumber = repository.initializeIfNeeded()
                _todayDayNumber.value = dayNumber
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "خطأ غير متوقع"
                _isLoading.value = false
            }
        }
    }

    // ── User intents ──────────────────────────────────────────────────────────

    /**
     * Mark today's memorization portion as complete with a self-assessed score.
     * @param score  0–100 recitation quality score
     */
    fun onMarkMemorizationComplete(score: Int = 80) {
        val day = _todayDayNumber.value.takeIf { it > 0 } ?: return
        viewModelScope.launch {
            repository.markMemorizationComplete(day, score)
        }
    }

    /** Mark today's recent review ward (last 7 days' material) as complete. */
    fun onMarkRecentReviewComplete() {
        val day = _todayDayNumber.value.takeIf { it > 0 } ?: return
        viewModelScope.launch {
            repository.markRecentReviewComplete(day)
        }
    }

    /** Mark today's distant review ward as complete and advance the interval. */
    fun onMarkDistantReviewComplete() {
        val day = _todayDayNumber.value.takeIf { it > 0 } ?: return
        viewModelScope.launch {
            repository.markDistantReviewComplete(day)
        }
    }

    /** Open the recitation / self-testing bottom sheet for a given session type. */
    fun onStartRecitation(sessionType: String = "MEMORIZATION") {
        _recitationSheet.update {
            it.copy(
                isVisible   = true,
                dayNumber   = _todayDayNumber.value,
                sessionType = sessionType,
            )
        }
    }

    /** Dismiss the recitation bottom sheet. */
    fun onDismissRecitationSheet() {
        _recitationSheet.update { it.copy(isVisible = false) }
    }
}
