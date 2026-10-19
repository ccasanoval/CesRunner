package com.cesoft.cesrunner.ui.details

import com.cesoft.cesrunner.MainDispatcherRule
import com.cesoft.cesrunner.MessageType
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.LocationDto
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.usecase.DeleteTrackUC
import com.cesoft.cesrunner.domain.usecase.GetLocationUC
import com.cesoft.cesrunner.domain.usecase.ReadTrackUC
import com.cesoft.cesrunner.domain.usecase.UpdateTrackUC
import com.cesoft.cesrunner.ui.details.mvi.TrackDetailsIntent
import com.cesoft.cesrunner.ui.details.mvi.TrackDetailsSideEffect
import com.cesoft.cesrunner.ui.details.mvi.TrackDetailsState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrackDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val readTrack: ReadTrackUC = mockk()
    private val updateTrack: UpdateTrackUC = mockk()
    private val getLocation: GetLocationUC = mockk()
    private val deleteTrack: DeleteTrackUC = mockk()
    private val location = LocationDto(40.0, -3.0)
    private val track = TrackDto(id = 4, name = "run")

    private fun createViewModel(trackId: Long = track.id) =
        TrackDetailsViewModel(readTrack, updateTrack, getLocation, deleteTrack, mainDispatcherRule.dispatcher)
            .also { it.trackId = trackId }

    private fun currentView(viewModel: TrackDetailsViewModel) = viewModel.state.value.view

    @Test
    fun `load shows the track and the user location`() = runTest(mainDispatcherRule.dispatcher) {
        every { getLocation() } returns location
        coEvery { readTrack(track.id) } returns Result.success(track)
        val viewModel = createViewModel()

        viewModel.execute(TrackDetailsIntent.Load)
        advanceUntilIdle()

        assertEquals(TrackDetailsState.Init(track, location), currentView(viewModel))
    }

    @Test
    fun `load with invalid id shows NotFound and then clears the error`() = runTest(mainDispatcherRule.dispatcher) {
        every { getLocation() } returns null
        val viewModel = createViewModel(trackId = -1)

        viewModel.execute(TrackDetailsIntent.Load)
        advanceTimeBy(1)

        assertEquals(TrackDetailsState.Init(TrackDto.Empty, null, AppError.NotFound), currentView(viewModel))
        advanceUntilIdle()
        assertEquals(TrackDetailsState.Init(TrackDto.Empty, null), currentView(viewModel))
        coVerify(exactly = 0) { readTrack(any()) }
    }

    @Test
    fun `save name updates the track and shows the saved message`() = runTest(mainDispatcherRule.dispatcher) {
        every { getLocation() } returns location
        coEvery { readTrack(track.id) } returns Result.success(track)
        coEvery { updateTrack(any()) } returns Result.success(Unit)
        val viewModel = createViewModel()
        viewModel.execute(TrackDetailsIntent.Load)
        advanceUntilIdle()

        viewModel.execute(TrackDetailsIntent.SaveName("new name"))
        advanceTimeBy(1)

        val renamed = track.copy(name = "new name")
        coVerify { updateTrack(renamed) }
        assertEquals(TrackDetailsState.Init(renamed, location, null, MessageType.Saved), currentView(viewModel))
        advanceUntilIdle()
        assertEquals(TrackDetailsState.Init(renamed, location), currentView(viewModel))
    }

    @Test
    fun `save name failure keeps the old name and shows the error`() = runTest(mainDispatcherRule.dispatcher) {
        val dbError = IllegalStateException("db")
        every { getLocation() } returns location
        coEvery { readTrack(track.id) } returns Result.success(track)
        coEvery { updateTrack(any()) } returns Result.failure(dbError)
        val viewModel = createViewModel()
        viewModel.execute(TrackDetailsIntent.Load)
        advanceUntilIdle()

        viewModel.execute(TrackDetailsIntent.SaveName("new name"))
        advanceTimeBy(1)

        assertEquals(
            TrackDetailsState.Init(track, location, AppError.DataBaseError(dbError)),
            currentView(viewModel)
        )
    }

    @Test
    fun `delete closes the page when it succeeds`() = runTest(mainDispatcherRule.dispatcher) {
        every { getLocation() } returns location
        coEvery { readTrack(track.id) } returns Result.success(track)
        coEvery { deleteTrack(track.id) } returns Result.success(Unit)
        val viewModel = createViewModel()
        viewModel.execute(TrackDetailsIntent.Load)
        advanceUntilIdle()

        viewModel.execute(TrackDetailsIntent.Delete)
        advanceUntilIdle()

        coVerify { deleteTrack(track.id) }
        assertEquals(listOf(TrackDetailsSideEffect.Close), viewModel.state.value.sideEffects.toList())
    }
}
