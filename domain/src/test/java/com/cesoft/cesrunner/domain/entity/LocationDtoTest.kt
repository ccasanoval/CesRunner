package com.cesoft.cesrunner.domain.entity

import org.junit.Assert.assertEquals
import org.junit.Test

class LocationDtoTest {

    @Test
    fun `distance to the same location is zero`() {
        val location = LocationDto(40.4168, -3.7038)
        assertEquals(0.0, location.distanceTo(location), 0.0)
    }

    @Test
    fun `distance is symmetric`() {
        val madrid = LocationDto(40.4168, -3.7038)
        val barcelona = LocationDto(41.3874, 2.1686)
        assertEquals(madrid.distanceTo(barcelona), barcelona.distanceTo(madrid), 0.0001)
    }

    @Test
    fun `distance between Madrid and Barcelona is about 505 km`() {
        val madrid = LocationDto(40.4168, -3.7038)
        val barcelona = LocationDto(41.3874, 2.1686)
        assertEquals(505_000.0, madrid.distanceTo(barcelona), 3_000.0)
    }

    @Test
    fun `one degree of latitude is about 111 km`() {
        val a = LocationDto(0.0, 0.0)
        val b = LocationDto(1.0, 0.0)
        assertEquals(111_195.0, a.distanceTo(b), 10.0)
    }
}
