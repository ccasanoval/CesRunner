package com.cesoft.cesrunner.domain.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrackProgressTest {

    private fun point(lat: Double, time: Long) =
        TrackPointDto.Empty.copy(latitude = lat, longitude = -3.0, time = time)

    private val empty = TrackProgress.from(TrackDto(id = 1, timeIni = 5))

    @Test
    fun `from a new track starts with no distance and no points`() {
        assertEquals(TrackProgress(trackId = 1, timeIni = 5, distance = 0.0, lastPoint = null, pointCount = 0), empty)
    }

    @Test
    fun `from an existing track computes distance, start time and last point`() {
        val points = listOf(point(40.000, 100), point(40.001, 200))
        val progress = TrackProgress.from(TrackDto(id = 2, timeIni = 50, points = points))

        assertEquals(100L, progress.timeIni)
        assertEquals(111.19, progress.distance, 0.5)
        assertEquals(points.last(), progress.lastPoint)
        assertEquals(2, progress.pointCount)
    }

    @Test
    fun `the first point sets the start time and adds no distance`() {
        val progress = empty.add(point(40.0, 1000))!!

        assertEquals(1000L, progress.timeIni)
        assertEquals(0.0, progress.distance, 0.0)
        assertEquals(1, progress.pointCount)
    }

    @Test
    fun `next points accumulate distance and keep the start time`() {
        val progress = empty
            .add(point(40.000, 1000))!!
            .add(point(40.001, 2000))!!
            .add(point(40.002, 3000))!!

        assertEquals(1000L, progress.timeIni)
        assertEquals(222.39, progress.distance, 0.5)
        assertEquals(3, progress.pointCount)
        assertEquals(3000L, progress.lastPoint?.time)
    }

    @Test
    fun `a point in the same place as the last one is ignored`() {
        val progress = empty.add(point(40.0, 1000))!!
        assertNull(progress.add(point(40.0, 2000)))
    }

    @Test
    fun `incremental distance matches the distance of the whole track`() {
        val points = (0..20).map { point(40.0 + it * 0.0005, it * 1000L) }
        var progress = empty
        points.forEach { progress = progress.add(it)!! }

        assertEquals(TrackDto.calcDistance(points), progress.distance, 0.0001)
    }
}
