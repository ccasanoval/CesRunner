package com.cesoft.cesrunner.domain.usecase

import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.ID_NULL
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import com.cesoft.cesrunner.domain.entity.TrackProgress
import com.cesoft.cesrunner.domain.repository.RepositoryContract
import com.cesoft.cesrunner.domain.usecase.ai.FilterTracksUC
import com.cesoft.cesrunner.domain.usecase.ai.GetNearTracksUC
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.plus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UseCasesTest {

    private val repository: RepositoryContract = mockk(relaxed = true)

    @Test
    fun `DeleteCurrentTrackUC resets the current track id`() = runTest {
        DeleteCurrentTrackUC(repository)()
        coVerify { repository.saveCurrentTrack(ID_NULL) }
    }

    @Test
    fun `GetNearTracksUC uses 100 meters by default`() = runTest {
        GetNearTracksUC(repository)(1.0, 2.0)
        coVerify { repository.getNearTracks(1.0, 2.0, 100) }
    }

    @Test
    fun `GetNearTracksUC passes the requested distance`() = runTest {
        GetNearTracksUC(repository)(1.0, 2.0, 500)
        coVerify { repository.getNearTracks(1.0, 2.0, 500) }
    }

    @Test
    fun `FilterTracksUC passes name and distance`() = runTest {
        FilterTracksUC(repository)(name = "Canarias", distance = 5000)
        coVerify { repository.filterTracks("Canarias", 5000) }
    }

    @Test
    fun `ReadCurrentTrackFlowUC emits the track of the current id`() = runTest {
        val track = TrackDto(id = 3, name = "run")
        coEvery { repository.readCurrentTrackIdFlow() } returns Result.success(flowOf(3L))
        coEvery { repository.readTrackFlow(3L) } returns Result.success(flowOf(track))

        val res = ReadCurrentTrackFlowUC(repository)()

        assertTrue(res.isSuccess)
        assertEquals(track, res.getOrThrow().first())
    }

    @Test
    fun `ReadCurrentTrackFlowUC skips null ids`() = runTest {
        val track = TrackDto(id = 5)
        coEvery { repository.readCurrentTrackIdFlow() } returns Result.success(flowOf(null, 5L))
        coEvery { repository.readTrackFlow(5L) } returns Result.success(flowOf(track))

        val values = ReadCurrentTrackFlowUC(repository)().getOrThrow().toList()

        assertEquals(listOf(track), values)
        coVerify(exactly = 0) { repository.readTrackFlow(ID_NULL) }
    }

    @Test
    fun `ReadCurrentTrackFlowUC switches to the new track when the current id changes`() = runTest {
        val currentId = MutableStateFlow<Long?>(3L)
        val track3 = TrackDto(id = 3)
        coEvery { repository.readCurrentTrackIdFlow() } returns Result.success(currentId)
        // Database flows never complete
        coEvery { repository.readTrackFlow(3L) } returns Result.success(MutableStateFlow(track3))
        coEvery { repository.readTrackFlow(ID_NULL) } returns Result.success(MutableStateFlow(null))
        val values = mutableListOf<TrackDto?>()

        val job = ReadCurrentTrackFlowUC(repository)().getOrThrow()
            .onEach { values.add(it) }
            .launchIn(backgroundScope + UnconfinedTestDispatcher(testScheduler))
        currentId.value = ID_NULL

        assertEquals(listOf(track3, null), values)
        job.cancel()
    }

    @Test
    fun `ReadCurrentTrackFlowUC propagates the repository failure`() = runTest {
        val error = AppError.DataBaseError(Exception("db"))
        coEvery { repository.readCurrentTrackIdFlow() } returns Result.failure(error)

        val res = ReadCurrentTrackFlowUC(repository)()

        assertTrue(res.isFailure)
        assertSame(error, res.exceptionOrNull())
    }

    @Test
    fun `AddTrackPointUC stores the last point with the accumulated distance`() = runTest {
        val point = TrackPointDto.Empty.copy(latitude = 1.0, longitude = 2.0, time = 50)
        val progress = TrackProgress(trackId = 4, timeIni = 10, distance = 123.9, lastPoint = point, pointCount = 3)

        AddTrackPointUC(repository)(progress)

        coVerify { repository.addTrackPoint(id = 4, point = point, distance = 123, timeIni = 10) }
    }

    @Test
    fun `AddTrackPointUC does nothing without points`() = runTest {
        val progress = TrackProgress(trackId = 4, timeIni = 10, distance = 0.0, lastPoint = null, pointCount = 0)

        assertTrue(AddTrackPointUC(repository)(progress).isSuccess)

        coVerify(exactly = 0) { repository.addTrackPoint(any(), any(), any(), any()) }
    }
}
