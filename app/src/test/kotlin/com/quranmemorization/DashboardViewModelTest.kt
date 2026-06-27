package com.quranmemorization

import com.quranmemorization.data.local.entity.QuranSchedule
import com.quranmemorization.domain.model.DailyTask
import com.quranmemorization.domain.model.OverallProgress
import com.quranmemorization.domain.repository.QuranRepository
import com.quranmemorization.presentation.dashboard.DashboardUiState
import com.quranmemorization.presentation.dashboard.DashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * DashboardViewModelTest — unit tests for the ViewModel's state transitions.
 *
 * Uses a [FakeQuranRepository] so no Room database is required.
 * The [StandardTestDispatcher] ensures coroutines run deterministically.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeQuranRepository
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo  = FakeQuranRepository()
        viewModel = DashboardViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() {
        // Before any coroutines run the state should be Loading
        assertTrue(viewModel.uiState.value is DashboardUiState.Loading)
    }

    @Test
    fun `after init resolves dayNumber state becomes Success`() = runTest {
        advanceUntilIdle()   // let initialize() coroutine complete

        val state = viewModel.uiState.value
        assertTrue("Expected Success but got $state", state is DashboardUiState.Success)

        val success = state as DashboardUiState.Success
        assertEquals(1, success.todayDayNumber)
        assertEquals("اليوم ١", success.dailyTask.portionTitle)
    }

    @Test
    fun `markMemorizationComplete updates task in Success state`() = runTest {
        advanceUntilIdle()

        viewModel.onMarkMemorizationComplete(score = 90)
        advanceUntilIdle()

        assertTrue(fakeRepo.memorizationMarked)
        assertEquals(90, fakeRepo.lastScore)
    }

    @Test
    fun `markRecentReviewComplete delegates to repository`() = runTest {
        advanceUntilIdle()
        viewModel.onMarkRecentReviewComplete()
        advanceUntilIdle()
        assertTrue(fakeRepo.recentReviewMarked)
    }

    @Test
    fun `markDistantReviewComplete delegates to repository`() = runTest {
        advanceUntilIdle()
        viewModel.onMarkDistantReviewComplete()
        advanceUntilIdle()
        assertTrue(fakeRepo.distantReviewMarked)
    }

    @Test
    fun `onStartRecitation sets recitation sheet visible`() = runTest {
        advanceUntilIdle()
        viewModel.onStartRecitation("MEMORIZATION")
        assertTrue(viewModel.recitationSheet.value.isVisible)
        assertEquals("MEMORIZATION", viewModel.recitationSheet.value.sessionType)
    }

    @Test
    fun `onDismissRecitationSheet hides sheet`() = runTest {
        advanceUntilIdle()
        viewModel.onStartRecitation("MEMORIZATION")
        viewModel.onDismissRecitationSheet()
        assertTrue(!viewModel.recitationSheet.value.isVisible)
    }

    @Test
    fun `error from repository produces Error state`() = runTest {
        fakeRepo.shouldThrow = true
        val errorVm = DashboardViewModel(fakeRepo)
        advanceUntilIdle()
        assertTrue(errorVm.uiState.value is DashboardUiState.Error)
    }
}

// ── Fake Repository ────────────────────────────────────────────────────────────

/**
 * FakeQuranRepository — in-memory stub used by unit tests.
 * No Room, no Android framework code.
 */
class FakeQuranRepository : QuranRepository {

    var shouldThrow          = false
    var memorizationMarked   = false
    var recentReviewMarked   = false
    var distantReviewMarked  = false
    var lastScore            = 0

    private val fakeDayNumber = 1

    private val fakeTask = DailyTask(
        dayNumber               = fakeDayNumber,
        portionTitle            = "اليوم ١",
        pageStart               = 1,
        pageEnd                 = 2,
        juzNumber               = 1,
        isMemorizationComplete  = memorizationMarked,
        recentReviewItems       = emptyList(),
        isRecentReviewComplete  = false,
        distantReviewItems      = emptyList(),
        isDistantReviewComplete = false,
        recitationScore         = 0,
    )

    private val fakeProgress = OverallProgress(
        memorizedDays = 0,
        totalMistakes = 0,
        averageScore  = 0f,
    )

    override fun observeTodayTask(dayNumber: Int) = flowOf(fakeTask)

    override fun observeOverallProgress() = flowOf(fakeProgress)

    override suspend fun markMemorizationComplete(dayNumber: Int, score: Int) {
        memorizationMarked = true
        lastScore          = score
    }

    override suspend fun markRecentReviewComplete(dayNumber: Int) {
        recentReviewMarked = true
    }

    override suspend fun markDistantReviewComplete(dayNumber: Int) {
        distantReviewMarked = true
    }

    override suspend fun logMistake(mistake: com.quranmemorization.data.local.entity.MistakeLog) {}

    override fun observeMistakesForDay(dayNumber: Int) = flowOf(emptyList<com.quranmemorization.data.local.entity.MistakeLog>())

    override suspend fun initializeIfNeeded(): Int {
        if (shouldThrow) throw RuntimeException("خطأ تجريبي")
        return fakeDayNumber
    }
}
