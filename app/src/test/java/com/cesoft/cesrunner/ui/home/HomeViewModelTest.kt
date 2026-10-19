package com.cesoft.cesrunner.ui.home

import com.cesoft.cesrunner.MainDispatcherRule
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.usecase.ReadCurrentTrackFlowUC
import com.cesoft.cesrunner.domain.usecase.ReadCurrentTrackUC
import com.cesoft.cesrunner.domain.usecase.ReadVo2MaxUC
import com.cesoft.cesrunner.domain.usecase.RequestLocationUpdatesUC
import com.cesoft.cesrunner.tracking.TrackingServiceFac
import com.cesoft.cesrunner.ui.home.mvi.HomeIntent
import com.cesoft.cesrunner.ui.home.mvi.HomeSideEffect
import com.cesoft.cesrunner.ui.home.mvi.HomeState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val trackingServiceFac: TrackingServiceFac = mockk(relaxed = true)
    private val readCurrentTrack: ReadCurrentTrackUC = mockk()
    private val readCurrentTrackFlow: ReadCurrentTrackFlowUC = mockk()
    private val requestLocationUpdates: RequestLocationUpdatesUC = mockk()
    private val readVo2Max: ReadVo2MaxUC = mockk()

    private fun createViewModel() = HomeViewModel(
        trackingServiceFac, readCurrentTrack, readCurrentTrackFlow,
        requestLocationUpdates, readVo2Max, mainDispatcherRule.dispatcher
    )

    @Before
    fun setUp() {
        every { requestLocationUpdates(any(), any()) } returns emptyFlow()
        coEvery { readVo2Max() } returns 42.0
        coEvery { readCurrentTrackFlow() } returns Result.success(flowOf(null))
    }

    @Test
    fun `load shows the best vo2max`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { readCurrentTrack() } returns Result.failure(AppError.NotFound)
        val viewModel = createViewModel()

        viewModel.execute(HomeIntent.Load)
        advanceUntilIdle()

        val state = viewModel.state.value.view as HomeState.Init
        assertEquals(42.0, state.vo2Max, 0.0)
        assertNull(state.error)
    }

    @Test
    fun `load restarts the tracking service when there is a tracking in progress`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { readCurrentTrack() } returns Result.success(TrackDto(id = 7))
        val viewModel = createViewModel()

        viewModel.execute(HomeIntent.Load)
        advanceUntilIdle()

        verify { trackingServiceFac.start() }
    }

    @Test
    fun `load does not start the tracking service when not tracking`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { readCurrentTrack() } returns Result.failure(AppError.NotFound)
        val viewModel = createViewModel()

        viewModel.execute(HomeIntent.Load)
        advanceUntilIdle()

        verify(exactly = 0) { trackingServiceFac.start() }
    }

    @Test
    fun `load shows a database error when the current track can't be read`() = runTest(mainDispatcherRule.dispatcher) {
        val error = IllegalStateException("db")
        coEvery { readCurrentTrack() } returns Result.failure(AppError.NotFound)
        coEvery { readCurrentTrackFlow() } returns Result.failure(error)
        val viewModel = createViewModel()

        viewModel.execute(HomeIntent.Load)
        advanceUntilIdle()

        assertEquals(AppError.DataBaseError(error), (viewModel.state.value.view as HomeState.Init).error)
    }

    @Test
    fun `a location error does not crash the screen`() = runTest(mainDispatcherRule.dispatcher) {
        every { requestLocationUpdates(any(), any()) } returns kotlinx.coroutines.flow.flow { throw SecurityException() }
        coEvery { readCurrentTrack() } returns Result.failure(AppError.NotFound)
        val viewModel = createViewModel()

        viewModel.execute(HomeIntent.Load)
        advanceUntilIdle()
        val state = viewModel.state.value.view as HomeState.Init
        val job = backgroundScope.launch { state.locationFlow?.collect {} }
        advanceUntilIdle()

        assertNull(state.locationFlow?.value)
        job.cancel()
    }

    @Test
    fun `close emits the close side effect`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        viewModel.execute(HomeIntent.Close)
        advanceUntilIdle()

        assertEquals(listOf(HomeSideEffect.Close), viewModel.state.value.sideEffects.toList())
    }
}
