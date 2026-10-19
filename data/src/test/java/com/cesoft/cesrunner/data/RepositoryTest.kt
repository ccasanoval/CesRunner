package com.cesoft.cesrunner.data

import com.cesoft.cesrunner.data.groq.Groq
import com.cesoft.cesrunner.data.local.AppDatabase
import com.cesoft.cesrunner.data.local.dao.TrackDao
import com.cesoft.cesrunner.data.local.dao.TrackPointDao
import com.cesoft.cesrunner.data.local.entity.LocalLocationDto
import com.cesoft.cesrunner.data.local.entity.LocalTrackDto
import com.cesoft.cesrunner.data.local.entity.LocalTrackPointDto
import com.cesoft.cesrunner.data.location.LocationDataSource
import com.cesoft.cesrunner.data.prefs.PrefDataSource
import androidx.room.withTransaction
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.ApiKeysDto
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RepositoryTest {

    private val trackDao: TrackDao = mockk(relaxed = true)
    private val trackPointDao: TrackPointDao = mockk(relaxed = true)
    private val db: AppDatabase = mockk {
        every { trackDao() } returns trackDao
        every { trackPointDao() } returns trackPointDao
    }
    private val locationDataSource: LocationDataSource = mockk(relaxed = true)
    private val prefs: PrefDataSource = mockk(relaxed = true)
    private val repository = Repository(prefs, locationDataSource, db, mockk<Groq>())

    @Before
    fun setUp() {
        // Run the transaction block directly
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { db.withTransaction(any<suspend () -> Any>()) } coAnswers {
            secondArg<suspend () -> Any>().invoke()
        }
    }

    private val localTrack = LocalTrackDto(id = 3, name = "run", timeIni = 0, timeEnd = 60_000, distance = 250)
    private val localPoint = LocalTrackPointDto(
        id = 1, idTrack = 3, latitude = 40.0, longitude = -3.0, time = 0,
        accuracy = 1f, provider = "gps", altitude = 0.0, bearing = 0f, speed = 0f
    )

    @Test
    fun `createTrack returns the new id`() = runTest {
        coEvery { trackDao.create(any()) } returns 12L
        assertEquals(12L, repository.createTrack(TrackDto(name = "new")).getOrThrow())
    }

    @Test
    fun `createTrack fails when room does not return a valid id`() = runTest {
        coEvery { trackDao.create(any()) } returns 0L
        assertTrue(repository.createTrack(TrackDto()).isFailure)
    }

    @Test
    fun `createTrack fails when the database throws`() = runTest {
        val error = IllegalStateException("db")
        coEvery { trackDao.create(any()) } throws error
        assertSame(error, repository.createTrack(TrackDto()).exceptionOrNull())
    }

    @Test
    fun `readTrack returns the track with its points`() = runTest {
        coEvery { trackDao.getById(3) } returns localTrack
        coEvery { trackPointDao.getByTrackId(3) } returns listOf(localPoint)

        val track = repository.readTrack(3).getOrThrow()

        assertEquals(3L, track.id)
        assertEquals(listOf(localPoint.toModel()), track.points)
    }

    @Test
    fun `readTrack fails with NotFound when the track does not exist`() = runTest {
        coEvery { trackDao.getById(3) } returns null
        assertSame(AppError.NotFound, repository.readTrack(3).exceptionOrNull())
    }

    @Test
    fun `readLastTrack fails with NotFound when there are no tracks`() = runTest {
        coEvery { trackDao.getLast() } returns null
        assertSame(AppError.NotFound, repository.readLastTrack().exceptionOrNull())
    }

    @Test
    fun `readAllTracks does not load the points`() = runTest {
        coEvery { trackDao.getAll() } returns listOf(localTrack)

        val tracks = repository.readAllTracks().getOrThrow()

        assertEquals(1, tracks.size)
        assertTrue(tracks[0].points.isEmpty())
    }

    @Test
    fun `deleteTrack removes the points and then the track`() = runTest {
        assertTrue(repository.deleteTrack(3).isSuccess)
        coVerifyOrder {
            trackPointDao.deleteByTrackId(3)
            trackDao.deleteById(3)
        }
    }

    @Test
    fun `filterTracks fails with NotFound when nothing matches`() = runTest {
        coEvery { trackDao.filter(null, "x", null) } returns listOf()
        assertSame(AppError.NotFound, repository.filterTracks("x", null).exceptionOrNull())
    }

    @Test
    fun `filterTracks returns the tracks with their points`() = runTest {
        coEvery { trackDao.filter(null, "run", 250) } returns listOf(localTrack)
        coEvery { trackPointDao.getByTrackId(3) } returns listOf(localPoint)

        val tracks = repository.filterTracks("run", 250).getOrThrow()

        assertEquals(1, tracks[0].points.size)
    }

    @Test
    fun `addTrackPoint stores the point and updates only the progress of the track`() = runTest {
        val point = TrackPointDto.Empty.copy(latitude = 1.0, longitude = 2.0, time = 900)

        assertTrue(repository.addTrackPoint(3, point, distance = 120, timeIni = 100).isSuccess)

        coVerifyOrder {
            trackPointDao.add(LocalTrackPointDto.fromModel(3, point))
            trackDao.updateProgress(3, 120, 100, 900)
        }
        coVerify(exactly = 0) { trackDao.update(any()) }
    }

    @Test
    fun `addTrackPoint fails when the database throws`() = runTest {
        coEvery { trackPointDao.add(any()) } throws IllegalStateException("db")
        assertTrue(repository.addTrackPoint(3, TrackPointDto.Empty, 0, 0).isFailure)
    }

    @Test
    fun `getNearTracks returns each track once when any of its points is near`() = runTest {
        coEvery { trackPointDao.getAllLocations() } returns listOf(
            LocalLocationDto(id = 1, idTrack = 3, latitude = 40.0000, longitude = -3.0),
            LocalLocationDto(id = 2, idTrack = 3, latitude = 40.0001, longitude = -3.0),
            LocalLocationDto(id = 3, idTrack = 4, latitude = 41.0000, longitude = -3.0),
        )
        coEvery { trackDao.getById(3) } returns localTrack

        val tracks = repository.getNearTracks(40.0, -3.0, distanceMax = 100).getOrThrow()

        assertEquals(listOf(3L), tracks.map { it.id })
        coVerify(exactly = 1) { trackDao.getById(any()) }
    }

    @Test
    fun `readVo2Max calculates the best value from the tracks when not stored`() = runTest {
        coEvery { prefs.readVo2Max(0.0) } returns 0.0
        coEvery { trackDao.getAll() } returns listOf(
            localTrack.copy(timeIni = 0, timeEnd = 25 * 60_000L, distance = 5000),  // 43.5
            localTrack.copy(timeIni = 0, timeEnd = 30 * 60_000L, distance = 5000),  // 36.8
        )

        assertEquals(43.5, repository.readVo2Max(), 0.0001)
        coVerify { prefs.saveVo2Max(43.5) }
    }

    @Test
    fun `saveVo2Max only stores new records`() = runTest {
        coEvery { prefs.readVo2Max(0.0) } returns 40.0

        assertTrue(repository.saveVo2Max(39.0).isSuccess)
        coVerify(exactly = 0) { prefs.saveVo2Max(any()) }

        repository.saveVo2Max(41.0)
        coVerify { prefs.saveVo2Max(41.0) }
    }

    @Test
    fun `api keys are delegated to the preferences`() = runTest {
        val keys = ApiKeysDto(groq = "g")
        coEvery { prefs.readApiKeys() } returns Result.success(keys)

        assertEquals(keys, repository.readApiKeys().getOrThrow())
        repository.saveApiKeys(keys)
        coVerify { prefs.saveApiKeys(keys) }
    }

    @Test
    fun `getLastKnownLocation returns null when there is no location`() {
        every { locationDataSource.getLastKnownLocation() } returns null
        assertEquals(null, repository.getLastKnownLocation())
    }
}
