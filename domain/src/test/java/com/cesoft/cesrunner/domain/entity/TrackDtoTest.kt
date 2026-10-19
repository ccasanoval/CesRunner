package com.cesoft.cesrunner.domain.entity

import com.cesoft.cesrunner.domain.ID_NULL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackDtoTest {

    private fun point(lat: Double, lng: Double, time: Long = 0) =
        TrackPointDto.Empty.copy(latitude = lat, longitude = lng, time = time)

    @Test
    fun `isCreated is false for empty track and true when it has an id`() {
        assertFalse(TrackDto.Empty.isCreated)
        assertFalse(TrackDto(id = ID_NULL).isCreated)
        assertTrue(TrackDto(id = 7).isCreated)
    }

    @Test
    fun `time is the difference between end and start`() {
        val track = TrackDto(timeIni = 1_000, timeEnd = 61_000)
        assertEquals(60_000L, track.time)
    }

    @Test
    fun `calcVo2Max uses speed in meters per minute`() {
        // 5000 m in 25 min -> 200 m/min -> 200 * 0.2 + 3.5 = 43.5
        val vo2Max = TrackDto.calcVo2Max(timeEnd = 25 * 60_000L, timeIni = 0, distance = 5000)
        assertEquals(43.5, vo2Max, 0.0001)
    }

    @Test
    fun `calcVo2Max instance method delegates on companion`() {
        val track = TrackDto(timeIni = 0, timeEnd = 25 * 60_000L, distance = 5000)
        assertEquals(43.5, track.calcVo2Max(), 0.0001)
    }

    @Test
    fun `calcVo2Max returns zero when the speed is not human`() {
        // 336 km in 21 min: a plane
        val vo2Max = TrackDto.calcVo2Max(timeEnd = 21 * 60_000L, timeIni = 0, distance = 336_050)
        assertEquals(0.0, vo2Max, 0.0)
    }

    @Test
    fun `calcVo2Max returns zero instead of NaN or Infinity when there is no time`() {
        assertEquals(0.0, TrackDto.calcVo2Max(timeEnd = 1000, timeIni = 1000, distance = 0), 0.0)
        assertEquals(0.0, TrackDto.calcVo2Max(timeEnd = 1000, timeIni = 1000, distance = 100), 0.0)
        assertEquals(0.0, TrackDto.calcVo2Max(timeEnd = 0, timeIni = 1000, distance = 100), 0.0)
    }

    @Test
    fun `calcDistance of no points or a single point is zero`() {
        assertEquals(0.0, TrackDto.calcDistance(listOf()), 0.0)
        assertEquals(0.0, TrackDto.calcDistance(listOf(point(40.0, -3.0))), 0.0)
    }

    @Test
    fun `calcDistance adds the distance between consecutive points`() {
        // Along a meridian, 0.001 degrees of latitude ~ 111.19 m
        val points = listOf(point(40.000, -3.0), point(40.001, -3.0), point(40.002, -3.0))
        assertEquals(222.39, TrackDto.calcDistance(points), 0.5)
    }
}
