package com.cesoft.cesrunner.data.local.entity

import com.cesoft.cesrunner.domain.ID_NULL
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalEntitiesTest {

    private val point = TrackPointDto(
        id = 11,
        latitude = 40.1,
        longitude = -3.2,
        time = 1_700_000_000_000,
        accuracy = 4.5f,
        provider = "gps",
        altitude = 650.0,
        bearing = 90f,
        speed = 3.2f,
    )

    @Test
    fun `LocalTrackDto fromModel keeps the id of an existing track`() {
        val local = LocalTrackDto.fromModel(TrackDto(id = 42, name = "run", distance = 1000))
        assertEquals(42L, local.id)
        assertEquals("run", local.name)
        assertEquals(1000, local.distance)
    }

    @Test
    fun `LocalTrackDto fromModel uses 0 for new tracks so room generates the id`() {
        assertEquals(0L, LocalTrackDto.fromModel(TrackDto(id = ID_NULL)).id)
        assertEquals(0L, LocalTrackDto.fromModel(TrackDto(id = -1)).id)
    }

    @Test
    fun `LocalTrackDto toModel maps fields and points`() {
        val local = LocalTrackDto(id = 5, name = "n", timeIni = 10, timeEnd = 20, distance = 300)
        val points = listOf(LocalTrackPointDto.fromModel(5, point))

        val track = local.toModel(points)

        assertEquals(TrackDto(5, "n", 10, 20, 300, listOf(point)), track)
    }

    @Test
    fun `LocalTrackDto toModelDto includes vo2max and location data`() {
        val local = LocalTrackDto(id = 5, name = "n", timeIni = 0, timeEnd = 25 * 60_000L, distance = 5000)

        val run = local.toModelDto(distanceToLocation = 80, latitude = 1.0, longitude = 2.0)

        assertEquals(5L, run.id)
        assertEquals(80, run.distanceToLocation)
        assertEquals(1.0, run.latitude, 0.0)
        assertEquals(2.0, run.longitude, 0.0)
        assertEquals(43.5, run.vo2Max, 0.0001)
    }

    @Test
    fun `LocalTrackPointDto round trip keeps all the data`() {
        val local = LocalTrackPointDto.fromModel(idTrack = 9, data = point)
        assertEquals(9L, local.idTrack)
        assertEquals(point, local.toModel())
    }
}
