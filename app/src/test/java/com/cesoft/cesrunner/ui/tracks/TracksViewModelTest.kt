package com.cesoft.cesrunner.ui.tracks

import com.cesoft.cesrunner.MainDispatcherRule
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.usecase.DeleteTrackUC
import com.cesoft.cesrunner.domain.usecase.ReadAllTracksUC
import com.cesoft.cesrunner.ui.tracks.mvi.TracksIntent
import com.cesoft.cesrunner.ui.tracks.mvi.TracksSideEffect
import com.cesoft.cesrunner.ui.tracks.mvi.TracksState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TracksViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val readAllTracks: ReadAllTracksUC = mockk()
    private val deleteTrack: DeleteTrackUC = mockk()
    private val tracks = listOf(TrackDto(id = 2, name = "b"), TrackDto(id = 1, name = "a"))

    private fun createViewModel() =
        TracksViewModel(readAllTracks, deleteTrack, mainDispatcherRule.dispatcher)

    @Test
    fun `initial state is loading`() {
        assertEquals(TracksState.Loading, createViewModel().state.value.view)
    }

    @Test
    fun `load shows the tracks`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { readAllTracks() } returns Result.success(tracks)
        val viewModel = createViewModel()

        viewModel.execute(TracksIntent.Load)
        advanceUntilIdle()

        assertEquals(TracksState.Init(tracks, null), viewModel.state.value.view)
    }

    @Test
    fun `load with NotFound shows an empty list without error`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { readAllTracks() } returns Result.failure(AppError.NotFound)
        val viewModel = createViewModel()

        viewModel.execute(TracksIntent.Load)
        advanceUntilIdle()

        val state = viewModel.state.value.view as TracksState.Init
        assertEquals(listOf<TrackDto>(), state.tracks)
        assertNull(state.error)
    }

    @Test
    fun `load with other failure shows the error`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { readAllTracks() } returns Result.failure(IllegalStateException("boom"))
        val viewModel = createViewModel()

        viewModel.execute(TracksIntent.Load)
        advanceUntilIdle()

        val state = viewModel.state.value.view as TracksState.Init
        assertEquals(AppError.UnknownError("boom"), state.error)
    }

    @Test
    fun `delete removes the track and reloads the list`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { deleteTrack(2) } returns Result.success(Unit)
        coEvery { readAllTracks() } returns Result.success(tracks.drop(1))
        val viewModel = createViewModel()

        viewModel.execute(TracksIntent.Delete(2))
        advanceUntilIdle()

        coVerify { deleteTrack(2) }
        assertEquals(TracksState.Init(tracks.drop(1), null), viewModel.state.value.view)
    }

    @Test
    fun `details emits the side effect and goes back to loading`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        viewModel.execute(TracksIntent.Details(7))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(TracksState.Loading, state.view)
        assertEquals(listOf(TracksSideEffect.Details(7)), state.sideEffects.toList())
    }
}
