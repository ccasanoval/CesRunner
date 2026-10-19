package com.cesoft.cesrunner.tracking

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackingServiceTest {

    @Test
    fun `interval of 0 minutes means the minimum period of 30 seconds`() {
        assertEquals(30_000L, TrackingService.intervalMillis(0))
    }

    @Test
    fun `interval converts minutes to milliseconds`() {
        assertEquals(60_000L, TrackingService.intervalMillis(1))
        assertEquals(5 * 60_000L, TrackingService.intervalMillis(5))
    }

    @Test
    fun `interval is limited to 10 minutes`() {
        assertEquals(10 * 60_000L, TrackingService.intervalMillis(15))
    }

    @Test
    fun `distance is limited between 0 and 1000 meters`() {
        assertEquals(0f, TrackingService.distanceMeters(-5), 0f)
        assertEquals(50f, TrackingService.distanceMeters(50), 0f)
        assertEquals(1000f, TrackingService.distanceMeters(5000), 0f)
    }
}
