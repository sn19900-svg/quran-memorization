package com.quranmemorization.domain.usecase

import com.quranmemorization.data.local.entity.MistakeLog
import com.quranmemorization.domain.model.DailyTask
import com.quranmemorization.domain.model.OverallProgress
import com.quranmemorization.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * GetTodayTaskUseCase — retrieves the reactive stream of today's DailyTask.
 *
 * Use cases are thin wrappers in MVVM+Clean Architecture; their value is:
 *  • Single-responsibility: one action per class.
 *  • Testability: mock the repository, test the use case in isolation.
 *  • The ViewModel depends on use cases, not the repository directly.
 */
class GetTodayTaskUseCase @Inject constructor(
    private val repository: QuranRepository,
) {
    operator fun invoke(dayNumber: Int): Flow<DailyTask?> =
        repository.observeTodayTask(dayNumber)
}

/** Retrieves the overall Quran memorization progress stream. */
class GetOverallProgressUseCase @Inject constructor(
    private val repository: QuranRepository,
) {
    operator fun invoke(): Flow<OverallProgress> =
        repository.observeOverallProgress()
}

/** Marks the memorization portion for a given day as complete. */
class MarkMemorizationCompleteUseCase @Inject constructor(
    private val repository: QuranRepository,
) {
    suspend operator fun invoke(dayNumber: Int, score: Int) =
        repository.markMemorizationComplete(dayNumber, score)
}

/** Marks the recent review ward as complete for today. */
class MarkRecentReviewCompleteUseCase @Inject constructor(
    private val repository: QuranRepository,
) {
    suspend operator fun invoke(dayNumber: Int) =
        repository.markRecentReviewComplete(dayNumber)
}

/** Marks the distant review ward as complete and advances the spaced-rep interval. */
class MarkDistantReviewCompleteUseCase @Inject constructor(
    private val repository: QuranRepository,
) {
    suspend operator fun invoke(dayNumber: Int) =
        repository.markDistantReviewComplete(dayNumber)
}

/** Logs a single recitation mistake event. */
class LogMistakeUseCase @Inject constructor(
    private val repository: QuranRepository,
) {
    suspend operator fun invoke(mistake: MistakeLog) =
        repository.logMistake(mistake)
}

/** Seeds the schedule and returns today's day number (1–365). */
class InitializeAppUseCase @Inject constructor(
    private val repository: QuranRepository,
) {
    suspend operator fun invoke(): Int =
        repository.initializeIfNeeded()
}
